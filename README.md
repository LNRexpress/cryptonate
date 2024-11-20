# cryptonate

## Description

The `cryptonate` library provides an application layer abstraction for encrypting domain model fields prior to database writes and decrypting domain model fields following database reads.

Encryption and decryption is performed using the AES algorithm in GCM mode, and users are able to define how AAD (additional authenticated data) is generated. Symmetric keys of various sizes are supported. Keys are retrieved from a JCA-compliant key store using the [keycache](https://github.com/LNRexpress/keycache) library. Key versioning is supported via the [keycache](https://github.com/LNRexpress/keycache) library as well.

## Motivation

Developers often use JPA `Converter`s or JPA life-cycle callback methods to perform domain model field encryption and decryption. While these options may be sufficient for simple use cases, they fall apart when needs are more complex. For example, when using Hibernate as your ORM framework, Hibernate validations are run *after* JPA `EntityListener`s; so, if you use an `EntityListener` to encrypt a domain model field, and the encryption cipher increases the size of your data beyond the maximum size allowed in your validations, inserts and updates will fail when your data reaches the Hibernate validation phase.

So, what can be done? The solution is to move domain model field encryption and decryption into the Hibernate layer so that validations are run *before* encryption and decryption occur. It is here, in the Hibernate event layer, that `cryptonate` performs its encryption and decryption operations.

Another drawback of performing domain model encryption and decryption using JPA `Converter`s or JPA `EntityListener`s is that during insert operations, entity IDs are not available to `Converter`s or `EntityListener`s. So, if you want to use the entity's ID as part of the encryption process (as part of the authentication data in AES-GCM, for example), you cannot do so (unless you generate your IDs in the application layer instead of the database layer).

How does `cryptonate` address this drawback? Thankfully, Hibernate makes an entity's ID available to event listeners prior to executing an `INSERT`. Since `cryptonate` operates in the Hibernate event layer, entity IDs are accessible by `cryptonate` during inserts and updates and can be used in entity field encryption and decryption.

## Requirements

* Java 8 or 11. Incompatible with Java 17.
* Apache Maven 3.6.3 or higher
* org.hibernate:hibernate-core, version 5.6.7.Final
* org.springframework:spring-core, version 5.3.18
* [keycache](https://github.com/LNRexpress/keycache), version 1.2.1

## Compilation

```
mvn clean package
```

## Test Execution

```
mvn clean test
```

## Installation

```
mvn clean install
```

## Usage

### Dependency Declaration (Apache Maven)

```
<dependency>
    <groupId>com.nightsky</groupId>
    <artifactId>cryptonate</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Configuration

#### Disable Translation of Hibernate Validation Constraints into the Database Schema

By default, Hibernate translates domain model validation constraints into the database schema. This feature must be disabled because encrypted columns will have lengths larger than the column's `Size.max` validation constraint. To do this, add the following to your `application.properties` file:

```
spring.jpa.properties.hibernate.validator.apply_to_ddl=false
```

#### Spring Boot Configuration

If you are using Spring Boot, you can easily enable and configure the `cryptonate` library by including the `cryptonate-spring-boot-starter` package in your project. This is the recommended method of using the `cryptonate` library as doing so automatically configures the library for use and does not require writing any configuration code.

Please see the [cryptonate-spring-boot-starter](https://github.com/LNRexpress/cryptonate-spring-boot-starter) and [cryptonate-spring-boot-autoconfigure](https://github.com/LNRexpress/cryptonate-spring-boot-autoconfigure) projects for usage specifics.

Be aware that the `cryptonate-spring-boot-starter`  and `cryptonate-spring-boot-autoconfigure` packages declare the Bouncy Castle FIPS-compliant library as one of their dependencies.

An example of how to use `cryptonate` and the `cryptonate-spring-boot-starter` package can be found in the [cryptonate-spring-boot-example](https://github.com/LNRexpress/cryptonate-spring-boot-example) project.

#### Direct Usage Configuration

##### Create a Random Number Generator

```
Random rng = new SecureRandom();
```

*Using `SecureRandom` is not recommended. It is being used here for the sake of simplicity.* You should consider using a random number generator like the `FipsSecureRandom` provided in the Bouncy Castle FIPS-compliant cryptography library.

##### Define an Encryption Key Name and Key Code Dictionary

```
String keyName = "sample-key-name";
Map<String, Integer> keyCodes = Collections.singletonMap(keyName, 1);
```

**keyName**
: The name, or alias, of the key that should be used to perform encryption.

**keyCodes**
: A `Map` that associates a key name with a *unique* numerical value. This number uniquely identifies the key to `cryptonate`. The key code, or ID, is stored along with the encrypted model data so that `cryptonate` knows which key to use when decoding encrypted data. If you change the name of the key that is used for encryption, be sure to maintain an entry for that key in this dictionary. If you do not, `cryptonate` will not be able to decrypt data that was encrypted with your old key(s).

##### Create a Versioned Secret Key Cache

Visit the [keycache](https://github.com/LNRexpress/keycache) page for configuration details.

##### Build a CryptoEventListener

```
CryptoEventListener cryptoEventListener = CryptoEventListener.builder()
    .withEncryptionKeyName(keyName)
    .withKeyCodes(keyCodes)
    .withRNG(rng)
    .withVersionedSecretKeyCache(versionedSecretKeyCache)
        .build();
```

##### Register the CryptoEventListener with Hibernate

```
SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);

registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(cryptoEventListener);
registry.getEventListenerGroup(EventType.PRE_UPDATE).appendListener(cryptoEventListener);
registry.getEventListenerGroup(EventType.PRE_LOAD).appendListener(cryptoEventListener);
```

### Annotate Entity Fields

#### Supported Field Types

Currently, the `cryptonate` library only supports the encryption and decryption of `String` fields.

#### Annotating Fields

In your domain model classes, annotate fields that you want to be encrypted with the `@Encrypted` annotation. Fields with the `@Encrypted` annotation will be detected by `cryptonate` and the values of those fields will be automatically encrypted before database writes and decrypted after database reads. The `aadFieldNames` attribute of the `@Encrypted` annotation can be used to specify which fields in your `class` should be used to generate AAD (additional authenticated data), which is used in the AES-GCM encryption algorithm. *AAD is optional, but strongly recommended*. Below is a simple example of how the `@Encrypted` annotation can be used in your domain model classes:

```
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import com.nightsky.cryptonate.annotation.Encrypted;

@Entity
@Table
public class SimpleEntity implements Serializable {

    @Id
    private Long id;

    @Column(length = 392, nullable = false)
    @Size(min = 5, max = 256)
    @Encrypted(aadFieldNames = { "id" })
    private String emailAddress;

    public SimpleEntity() {  }

    public SimpleEntity(Long id, String emailAddress) {
        this.id = id;
        this.emailAddress = emailAddress;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

}
```

### Database Column Sizing

The encryption process increases the size of the original data. Therefore, you must size your database columns appropriately. Below are the calculations needed for sizing the database columns for `String` fields.

1. Determine the maximum (unencrypted) size of the entity field. Assign this value to `maximum_field_size`.
2. Substitute `maximum_field_size` in the following equation: `4 * ceil(((4/3) * (4 + 4 + 12 + (ceil((maximum_field_size*8)/256) * (256/8)) + (128 / 8))) / 4)`

The result of the calculation above should be used for the value of the `length` attribute of your `@Column` annotation.

#### Example

Assume we have an entity with a field having a maximum unencrypted size of 256 characters. Then, the database column length is calculated as follows:

```
maximum_field_size = 256
4 * ceil(((4/3) * (4 + 4 + 12 + (ceil((maximum_field_size*8)/256) * (256/8)) + (128 / 8))) / 4) = 392
```

392 is the maximum length (in base64 characters) of the encrypted and base64-encoded entity field value.
