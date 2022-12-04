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
import org.assertj.core.data.Offset;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreLoadEvent;
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
public class FieldDecryptionTest {

    private final Faker faker;

    private EntityMetamodel metamodel;

    private EntityPersister persister;

    private CryptoEventListener subject;

    private SimpleEntity entity;

    private Object [] encryptedFieldValues;

    public FieldDecryptionTest() {
        faker = new Faker();
    }

    @Before
    public void setUp() {
        // Mock Hibernate EntityMetamodel:
        metamodel = mock(EntityMetamodel.class);
        when(metamodel.getPropertyNames()).thenReturn(new String[] {
            "emailAddress", "price", "streetNumber", "amount", "followers"
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

        // Create an entity and encrypt its fields:
        entity = SimpleEntity.builder()
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
            entity.getPrice(),
            entity.getStreetNumber(),
            entity.getAmount(),
            entity.getFollowers()
        });
        when(event.getPersister()).thenReturn(persister);

        // Encrypt the entity's fields:
        subject.onPreInsert(event);

        // Retrieve the encrypted field value:
        encryptedFieldValues = event.getState();
        assertThat((String)encryptedFieldValues[0]).isBase64();
        assertThat((String)encryptedFieldValues[1]).isBase64();
        assertThat((String)encryptedFieldValues[2]).isBase64();
        assertThat((String)encryptedFieldValues[3]).isBase64();
        assertThat((String)encryptedFieldValues[4]).isBase64();
    }

    @Test
    public void shouldDecryptFieldsOnPreLoad() {
        // Mock the Hibernate pre-load event:
        PreLoadEvent event = mock(PreLoadEvent.class);
        when(event.getEntity()).thenReturn(entity);
        when(event.getId()).thenReturn(entity.getId());
        when(event.getState()).thenReturn(new Object[] {
            encryptedFieldValues[0],
            encryptedFieldValues[1],
            encryptedFieldValues[2],
            encryptedFieldValues[3],
            encryptedFieldValues[4]
        });
        when(event.getPersister()).thenReturn(persister);

        // Decrypt the entity fields:
        subject.onPreLoad(event);

        // Check the result:
        assertThat(event.getState()[0]).isNotNull();
        assertThat(event.getState()[0]).isInstanceOf(String.class);
        assertThat((String)event.getState()[0]).isEqualTo(entity.getEmailAddress());

        assertThat(event.getState()[1]).isNotNull();
        assertThat(event.getState()[1]).isInstanceOf(Double.class);
        assertThat((Double)event.getState()[1]).isCloseTo(entity.getPrice(), Offset.offset(0.0001));

        assertThat(event.getState()[2]).isNotNull();
        assertThat(event.getState()[2]).isInstanceOf(Integer.class);
        assertThat((Integer)event.getState()[2]).isEqualTo(entity.getStreetNumber());

        assertThat(event.getState()[3]).isNotNull();
        assertThat(event.getState()[3]).isInstanceOf(Float.class);
        assertThat((Float)event.getState()[3]).isCloseTo(entity.getAmount(), Offset.offset(0.0001f));

        assertThat(event.getState()[4]).isNotNull();
        assertThat(event.getState()[4]).isInstanceOf(Long.class);
        assertThat((Long)event.getState()[4]).isEqualTo(entity.getFollowers());
    }

}
