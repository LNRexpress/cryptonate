package com.nightsky.cryptonate.event.listener;

import com.nightsky.cryptonate.annotation.Encrypted;
import com.nightsky.cryptonate.event.Context;
import com.nightsky.cryptonate.event.HibernatePreInsertEvent;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import javax.persistence.Id;
import org.apache.commons.lang3.RandomStringUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import org.hibernate.event.spi.PreInsertEvent;
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
public class CryptoEventListenerTest {

    private CryptoEventListener subject;

    @Before
    public void setUp() {
        subject = new CryptoEventListener();
    }

    @Test
    public void shouldCreateAadFromIdField() {
        Long id = 12345L;
        AadWithIdEntity entity = new AadWithIdEntity(id);
        Context context = mock(Context.class);

        Field encryptedField = null;
        for (Field field : entity.getClass().getDeclaredFields()) {
            if ( field.isAnnotationPresent(Encrypted.class) ) {
                encryptedField = field;
                break;
            }
        }

        assertThat(encryptedField).isNotNull();

        PreInsertEvent event = mock(PreInsertEvent.class);
        HibernatePreInsertEvent eventAdapter = new HibernatePreInsertEvent(event);

        when(context.getEvent()).thenReturn(eventAdapter);
        when(context.getEntity()).thenReturn(entity);

        try {
            byte[] result = subject.aadFor(context, encryptedField);
            assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(String.format("id=%d", id));
        } catch (Exception e) {
            fail("Test failed", e);
        }
    }

    @Test
    public void shouldCreateAadFromIdAndUuidFields() {
        Long id = 12345L;
        String uuid = RandomStringUtils.randomAlphabetic(32);
        AadWithIdAndUuidEntity entity = new AadWithIdAndUuidEntity(id, uuid);
        Context context = mock(Context.class);

        Field encryptedField = null;
        for (Field field : entity.getClass().getDeclaredFields()) {
            if ( field.isAnnotationPresent(Encrypted.class) ) {
                encryptedField = field;
                break;
            }
        }

        assertThat(encryptedField).isNotNull();

        PreInsertEvent event = mock(PreInsertEvent.class);
        HibernatePreInsertEvent eventAdapter = new HibernatePreInsertEvent(event);

        when(context.getEvent()).thenReturn(eventAdapter);
        when(context.getEntity()).thenReturn(entity);

        try {
            byte[] result = subject.aadFor(context, encryptedField);
            assertThat(new String(result, StandardCharsets.UTF_8)).isEqualTo(String.format("id=%d,uuid=%s", id, uuid));
        } catch (Exception e) {
            fail("Test failed", e);
        }
    }

    public class AadWithIdEntity implements Serializable {

        @Id
        private Long id;

        @Encrypted(aadFieldNames = { "id" })
        private String encryptedField;

        public AadWithIdEntity() {  }

        public AadWithIdEntity(Long id) {
            this.id = id;
        }

        /**
         * @return the id
         */
        public Long getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(Long id) {
            this.id = id;
        }

        /**
         * @return the encryptedField
         */
        public String getEncryptedField() {
            return encryptedField;
        }

        /**
         * @param encryptedField the encryptedField to set
         */
        public void setEncryptedField(String encryptedField) {
            this.encryptedField = encryptedField;
        }

    }

    public class AadWithIdAndUuidEntity implements Serializable {

        @Id
        private Long id;

        private String uuid;

        @Encrypted(aadFieldNames = { "id", "uuid" })
        private String encryptedField;

        public AadWithIdAndUuidEntity() {  }

        public AadWithIdAndUuidEntity(Long id) {
            this.id = id;
            this.uuid = RandomStringUtils.randomAlphabetic(32);
        }

        public AadWithIdAndUuidEntity(Long id, String uuid) {
            this.id = id;
            this.uuid = uuid;
        }

        /**
         * @return the id
         */
        public Long getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(Long id) {
            this.id = id;
        }

        /**
         * @return the uuid
         */
        public String getUuid() {
            return uuid;
        }

        /**
         * @param uuid the uuid to set
         */
        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        /**
         * @return the encryptedField
         */
        public String getEncryptedField() {
            return encryptedField;
        }

        /**
         * @param encryptedField the encryptedField to set
         */
        public void setEncryptedField(String encryptedField) {
            this.encryptedField = encryptedField;
        }

    }

}
