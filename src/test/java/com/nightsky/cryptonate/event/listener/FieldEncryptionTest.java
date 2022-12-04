package com.nightsky.cryptonate.event.listener;

import com.github.javafaker.Faker;
import com.nightsky.cryptonate.factory.SecretKeyFactory;
import static com.nightsky.cryptonate.factory.VersionedSecretKeyCacheFactory.mockedVersionedSecretKeyCache;
import com.nightsky.cryptonate.model.SimpleEntity;
import com.nightsky.keycache.VersionedSecretKeyCache;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import static org.assertj.core.api.Assertions.assertThat;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Chris
 */
@RunWith(JUnit4.class)
public class FieldEncryptionTest {

    private final Faker faker;

    private EntityMetamodel metamodel;

    private EntityPersister persister;

    private CryptoEventListener subject;

    public FieldEncryptionTest() {
        faker = new Faker();
    }

    @Before
    public void setUp() {
        // Mock Hibernate EntityMetamodel:
        metamodel = mock(EntityMetamodel.class);
        when(metamodel.getPropertyNames()).thenReturn(new String[] {
            "emailAddress", "followers", "streetNumber", "amount", "price"
        });

        // Mock Hibernate EntityPersister:
        persister = mock(EntityPersister.class);
        when(persister.getEntityMetamodel()).thenReturn(metamodel);

        // Create the dependencies of the CryptoEventListener class:
        Random rng = new SecureRandom();
        VersionedSecretKeyCache secretKeyCache = mockedVersionedSecretKeyCache();
        Map<String, Integer> keyCodes = Collections.singletonMap(SecretKeyFactory.KEY_NAME, 1);
        Map<String, String> keyNames = Collections.singletonMap("1", SecretKeyFactory.KEY_NAME);

        // Build the CryptoEventListener:
        subject = CryptoEventListener.builder()
            .withEncryptionKeyName(SecretKeyFactory.KEY_NAME)
            .withKeyCodes(keyCodes)
            .withKeyNames(keyNames)
            .withRNG(rng)
            .withVersionedSecretKeyCache(secretKeyCache)
                .build();
    }

    @Test
    public void shouldEncryptFieldsOnPreInsert() {
        // The entity whose fields will be encrypted
        SimpleEntity entity = SimpleEntity.builder()
            .withId(faker.number().randomNumber())
            .withEmailAddress(faker.internet().emailAddress())
            .withFollowers(faker.number().randomNumber())
            .withStreetNumber(faker.number().numberBetween(1, Integer.MAX_VALUE))
            .withAmount(Double.valueOf(faker.number().randomDouble(2, 10, 1000)).floatValue())
            .withPrice(faker.number().randomDouble(2, 10, 1000))
                .build();

        // Mock the Hibernate pre-insert event:
        PreInsertEvent event = mock(PreInsertEvent.class);
        when(event.getEntity()).thenReturn(entity);
        when(event.getId()).thenReturn(entity.getId());
        when(event.getState()).thenReturn(new Object[] {
            entity.getEmailAddress(),
            entity.getFollowers(),
            entity.getStreetNumber(),
            entity.getAmount(),
            entity.getPrice()
        });
        when(event.getPersister()).thenReturn(persister);

        // Encrypt the entity fields
        subject.onPreInsert(event);

        // Check the result
        assertThat(event.getState()[0]).isNotNull();
        assertThat(event.getState()[0]).isInstanceOf(String.class);
        assertThat((String)event.getState()[0]).isNotEqualTo(entity.getEmailAddress());
        assertThat((String)event.getState()[0]).isBase64();

        assertThat(event.getState()[1]).isNotNull();
        assertThat(event.getState()[1]).isInstanceOf(String.class);
        assertThat((String)event.getState()[1]).isNotEqualTo(entity.getFollowers());
        assertThat((String)event.getState()[1]).isBase64();

        assertThat(event.getState()[2]).isNotNull();
        assertThat(event.getState()[2]).isInstanceOf(String.class);
        assertThat((String)event.getState()[2]).isNotEqualTo(entity.getStreetNumber());
        assertThat((String)event.getState()[2]).isBase64();

        assertThat(event.getState()[3]).isNotNull();
        assertThat(event.getState()[3]).isInstanceOf(String.class);
        assertThat((String)event.getState()[3]).isNotEqualTo(entity.getAmount());
        assertThat((String)event.getState()[3]).isBase64();

        assertThat(event.getState()[4]).isNotNull();
        assertThat(event.getState()[4]).isInstanceOf(String.class);
        assertThat((String)event.getState()[4]).isNotEqualTo(entity.getPrice());
        assertThat((String)event.getState()[4]).isBase64();
    }

    @Test
    public void shouldEncryptFieldsOnPreUpdate() {
        // The entity whose fields will be encrypted
        SimpleEntity entity = SimpleEntity.builder()
            .withId(faker.number().randomNumber())
            .withEmailAddress(faker.internet().emailAddress())
            .withFollowers(faker.number().randomNumber())
            .withStreetNumber(faker.number().numberBetween(1, Integer.MAX_VALUE))
            .withAmount(Double.valueOf(faker.number().randomDouble(2, 10, 1000)).floatValue())
            .withPrice(faker.number().randomDouble(2, 10, 1000))
                .build();

        // Mock the Hibernate pre-update event:
        PreUpdateEvent event = mock(PreUpdateEvent.class);
        when(event.getEntity()).thenReturn(entity);
        when(event.getId()).thenReturn(entity.getId());
        when(event.getState()).thenReturn(new Object[] {
            entity.getEmailAddress(),
            entity.getFollowers(),
            entity.getStreetNumber(),
            entity.getAmount(),
            entity.getPrice()
        });
        when(event.getPersister()).thenReturn(persister);

        // Encrypt the entity fields
        subject.onPreUpdate(event);

        // Check the result
        assertThat(event.getState()[0]).isNotNull();
        assertThat(event.getState()[0]).isInstanceOf(String.class);
        assertThat((String)event.getState()[0]).isNotEqualTo(entity.getEmailAddress());
        assertThat((String)event.getState()[0]).isBase64();

        assertThat(event.getState()[1]).isNotNull();
        assertThat(event.getState()[1]).isInstanceOf(String.class);
        assertThat((String)event.getState()[1]).isNotEqualTo(entity.getFollowers());
        assertThat((String)event.getState()[1]).isBase64();

        assertThat(event.getState()[2]).isNotNull();
        assertThat(event.getState()[2]).isInstanceOf(String.class);
        assertThat((String)event.getState()[2]).isNotEqualTo(entity.getStreetNumber());
        assertThat((String)event.getState()[2]).isBase64();

        assertThat(event.getState()[3]).isNotNull();
        assertThat(event.getState()[3]).isInstanceOf(String.class);
        assertThat((String)event.getState()[3]).isNotEqualTo(entity.getAmount());
        assertThat((String)event.getState()[3]).isBase64();

        assertThat(event.getState()[4]).isNotNull();
        assertThat(event.getState()[4]).isInstanceOf(String.class);
        assertThat((String)event.getState()[4]).isNotEqualTo(entity.getPrice());
        assertThat((String)event.getState()[4]).isBase64();
    }

}
