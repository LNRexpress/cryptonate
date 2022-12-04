package com.nightsky.cryptonate.event.listener;

import com.nightsky.cryptonate.annotation.Encrypted;
import com.nightsky.cryptonate.convert.ConverterSupport;
import com.nightsky.cryptonate.event.Context;
import com.nightsky.cryptonate.event.HibernatePreInsertEvent;
import com.nightsky.cryptonate.event.HibernatePreLoadEvent;
import com.nightsky.cryptonate.event.HibernatePreUpdateEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.persistence.Id;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import com.nightsky.keycache.VersionedSecretKey;
import com.nightsky.keycache.VersionedSecretKeyCache;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.NoSuchPaddingException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

/**
 * Encrypts model fields annotated with @Encrypted. I hate having to use a
 * Hibernate construct to do field encryption. I would prefer to use JPA's
 * <code>&#64;PrePersist</code>, <code>&#64;PreUpdate</code>, etc. in a custom
 * EntityListener, but Hibernate runs validations after JPA EntityListeners
 * observe the entities. Encrypting with JPA EntityListeners, therefore, can
 * cause Hibernate validations to fail. One nice advantage of leveraging the
 * Hibernate event listeners is that Hibernate makes the entity ID value
 * available during <code>PreInsert</code> events; so, the entity ID can be used
 * for AAD in AES/GCM. The ID value is not available when using JPA
 * EntityListeners and the <code>&#64;PreInsert</code> annotation.
 *
 * <h4>To Calculate the Needed Column Size of a Entity Field</h4>
 *
 * <ol>
 *   <li>Determine the maximum (unencrypted) size of the entity field. Assign this value to <code>maximum_field_size</code>.</li>
 *   <li>Substitute <code>maximum_field_size</code> in the following equation: <code>4 * ceil(((4/3) * (4 + 4 + 12 + (ceil((maximum_field_size*8)/256) * (256/8)) + (128 / 8))) / 4)</code></li>
 * </ol>
 *
 * <p>
 *   The result of the calculation above should be used for the value of the
 *   <code>length</code> attribute of your <code>@Column</code> annotation.
 * </p>
 *
 * <h4>Example</h4>
 *
 * <p style="padding-left: 2em;">
 *   Assume we have an entity with a field having a maximum unencrypted size of 256 characters.</br>
 *   Then, the database column length is calculated as follows:</br></br>
 *   <code>maximum_field_size = 256</code></br>
 *   <code>4 * ceil(((4/3) * (4 + 4 + 12 + (ceil((maximum_field_size*8)/256) * (256/8)) + (128 / 8))) / 4)</code></br>
 *   <code style="padding-left: 1em;">= 392</code></br></br>
 *   392 is the maximum length (in base64 characters) of the encrypted and base64-encoded entity field value
 * </p>
 *
 * <h4>References</h4>
 *
 * <ul>
 *   <li><a href="http://anshuiitk.blogspot.com/2010/11/hibernate-pre-database-opertaion-event.html">Hibernate Event Listeners</a></li>
 *   <li><a href="https://cloud.google.com/kms/docs/additional-authenticated-data">Additional Authenticated Data</a></li>
 * </ul>
 *
 * @author Chris
 */
public class CryptoEventListener implements PreLoadEventListener, PreInsertEventListener, PreUpdateEventListener {

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";

    private static final int GCM_IV_LENGTH = 12;   // 96 bits (12 bytes) IV length

    private static final int GCM_TAG_LENGTH = 16;  // 128 bits (16 bytes) auth tag length

    private static final int INT_SIZE = (Integer.SIZE / Byte.SIZE);

    private Map<String, Integer> keyCodes;

    private final Map<String, String> keyNames;

    private String encryptionKeyName;

    private VersionedSecretKeyCache versionedSecretKeyCache;

    private String securityProviderName;

    private final ConversionService conversionService;

    private Random rng;

    public CryptoEventListener() {
        this.keyNames = new HashMap<>();
        securityProviderName = null;
        conversionService = DefaultConversionService.getSharedInstance();
        ConverterSupport.addInternalConverters((DefaultConversionService)conversionService);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void onPreLoad(PreLoadEvent event) {
        Context c = Context.builder()
            .withEntity(event.getEntity())
            .withId(event.getId())
            .withEvent(new HibernatePreLoadEvent(event))
                .build();
        decryptFields(c);
    }

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        Context c = Context.builder()
            .withEntity(event.getEntity())
            .withId(event.getId())
            .withEvent(new HibernatePreInsertEvent(event))
                .build();
        encryptFields(c);
        return false;
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        Context c = Context.builder()
            .withEntity(event.getEntity())
            .withId(event.getId())
            .withEvent(new HibernatePreUpdateEvent(event))
                .build();
        encryptFields(c);
        return false;
    }

    private void decryptFields(Context context) {
        for (Field field : context.getEntity().getClass().getDeclaredFields()) {
            if ( field.isAnnotationPresent(Encrypted.class) ) {
                decryptField(context, field);
            }
        }
    }

    private void encryptFields(Context context) {
        for (Field field : context.getEntity().getClass().getDeclaredFields()) {
            if ( field.isAnnotationPresent(Encrypted.class) ) {
                encryptField(context, field);
            }
        }
    }

    private void decryptField(Context context, Field field) {
        try {
            Object fieldValue = getFieldValue(context, field);

            if ( fieldValue == null || fieldValue.toString() == null || fieldValue.toString().isEmpty() )
                return;

            String encodedEnvelope = (String) fieldValue;
            byte[] envelope = Base64.getDecoder().decode(encodedEnvelope);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherInput = new byte[envelope.length - INT_SIZE - INT_SIZE - iv.length];

            ByteBuffer buf = ByteBuffer.wrap(envelope);
            String keyCode = String.format("%d", buf.getInt());
            int keyVersion = buf.getInt();
            buf.get(iv);
            buf.get(cipherInput);

            String keyName = keyNames.get(keyCode);
            GCMParameterSpec parameters = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, iv);

            Cipher cipher = getCipher();
            cipher.init(Cipher.DECRYPT_MODE, versionedSecretKeyCache.getKey(keyName, keyVersion), parameters);

            byte[] aad = aadFor(context, field);
            if ( aad != null && aad.length > 0 )
                cipher.updateAAD(aad);

            byte[] decrypted = cipher.doFinal(cipherInput);

            // Convert the decrypted data to the actual type of the field
            Object convertedFieldValue = conversionService.convert(decrypted, field.getType());

            setField(context, field, convertedFieldValue);
        } catch (Exception e) {
            String msg = String.format("Failed to decrypt field: %s.%s", context.getEntity().getClass().getName(), field.getName());
            throw new RuntimeException(msg, e);
        }
    }

    private void encryptField(Context context, Field field) {
        try {
            Object fieldValue = getFieldValue(context, field);

            if ( fieldValue == null )
                return;

            byte[] rawFieldValue = conversionService.convert(fieldValue, byte[].class);

            if ( rawFieldValue == null || rawFieldValue.length == 0 )
                return;

            // Load the encryption key from the cache
            VersionedSecretKey secretKey = versionedSecretKeyCache.getKey(encryptionKeyName);
            int keyCode = keyCodes.get(encryptionKeyName);

            // Generate the IV
            byte [] iv = new byte[GCM_IV_LENGTH];
            rng.nextBytes(iv);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, iv);

            Cipher cipher = getCipher();
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] aad = aadFor(context, field);
            if ( aad != null && aad.length > 0 )
                cipher.updateAAD(aad);

            // NOTE: cipher output array contains the ciphered data and the auth tag (concatenated)
            byte[] cipherOutput = cipher.doFinal(rawFieldValue);

            // envelope size = 4 + 4 + iv_length + encrypted_data_length + auth_tag_length
            //               = 4 + 4 + 12 + encrypted_data.length + 16
            ByteBuffer buf = ByteBuffer.allocate(INT_SIZE + INT_SIZE + iv.length + cipherOutput.length)
                .putInt(keyCode)
                .putInt(secretKey.getVersion())
                .put(iv)
                .put(cipherOutput);
            byte[] cipherEnvelope = buf.array();

            String encodedCipherEnvelope = Base64.getEncoder().encodeToString(cipherEnvelope);

            setField(context, field, encodedCipherEnvelope);
        } catch (Exception e) {
            String msg = String.format("Failed to encrypt field: %s.%s", context.getEntity().getClass().getName(), field.getName());
            throw new RuntimeException(msg, e);
        }
    }

    private Object getFieldValue(Context context, Field field) {
        return getFieldValue(context, field.getName());
    }

    private Object getFieldValue(Context context, String fieldName) {
        String[] propertyNames = context.getEvent().getPersister().getEntityMetamodel().getPropertyNames();
        Object[] currentState = context.getEvent().getState();

        // If fieldName matches the name of the @Id field, return context.getId().
        // propertyNames and currentState do not contain the name and value of the @Id field
        for (Field field : context.getEntity().getClass().getDeclaredFields()) {
            if ( field.isAnnotationPresent(Id.class) && field.getName().equals(fieldName) ) {
                return context.getId();
            }
        }

        int index = ArrayUtils.indexOf(propertyNames, fieldName);
        if ( currentState != null && index >= 0 ) {
            return currentState[index];
        }

        return null;
    }

    private void setField(Context context, Field field, Object dbFieldValue) throws IllegalAccessException {
        String[] propertyNames = context.getEvent().getPersister().getEntityMetamodel().getPropertyNames();
        Object[] currentState = context.getEvent().getState();

        int index = ArrayUtils.indexOf(propertyNames, field.getName());
        if ( currentState != null && index >= 0 ) {
            currentState[index] = dbFieldValue;
        }
    }

    /**
     * Generates AAD for the encryption cipher.</br>
     *
     * <p style="font-weight: bold">
     *   WARNING: Changes to this method may cause existing encrypted database data to be unrecoverable!
     * </p>
     *
     * @param context
     * @param field
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public byte[] aadFor(Context context, Field field)
        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        Encrypted annotation = field.getAnnotation(Encrypted.class);
        String [] aadFieldNames = annotation.aadFieldNames();

        if ( aadFieldNames == null || aadFieldNames.length == 0 )
            return null;

        StringBuilder aadBuffer = new StringBuilder("");
        int i = 0;
        for (String aadFieldName : aadFieldNames) {
            Object fieldValue;

            if ( context.getEvent() instanceof HibernatePreLoadEvent ) {
                // Not all entity fields are populated; so, get the field values from the session state
                fieldValue = getFieldValue(context, aadFieldName);
            } else {
                // All of the entity fields should be populated; so, get the field values from the entity itself
                fieldValue = PropertyUtils.getProperty(context.getEntity(), aadFieldName);
            }

            if ( fieldValue == null || fieldValue.toString() == null || fieldValue.toString().isEmpty() )
                throw new RuntimeException("Field value cannot be null or empty");

            if ( i != 0 )
                aadBuffer.append(",");

            aadBuffer.append(aadFieldName);
            aadBuffer.append("=");
            aadBuffer.append(fieldValue.toString());

            ++i;
        }

        return aadBuffer.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Cipher getCipher()
        throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException
    {
        if ( securityProviderName == null )
            return Cipher.getInstance(CIPHER_ALGORITHM);
        else
            return Cipher.getInstance(CIPHER_ALGORITHM, securityProviderName);
    }

    private void buildKeyNameDictionary() {
        keyNames.clear();

        for (String keyName : keyCodes.keySet()) {
            Integer code = keyCodes.get(keyName);
            keyNames.put(String.format("%d", code), keyName);
        }
    }

    /**
     * @return the keyCodes
     */
    public Map<String, Integer> getKeyCodes() {
        return keyCodes;
    }

    /**
     * @param keyCodes the keyCodes to set
     */
    public void setKeyCodes(Map<String, Integer> keyCodes) {
        this.keyCodes = keyCodes;
        buildKeyNameDictionary();
    }

    /**
     * @return the encryptionKeyName
     */
    public String getEncryptionKeyName() {
        return encryptionKeyName;
    }

    /**
     * @param encryptionKeyName the encryptionKeyName to set
     */
    public void setEncryptionKeyName(String encryptionKeyName) {
        this.encryptionKeyName = encryptionKeyName;
    }

    /**
     * @return the versionedSecretKeyCache
     */
    public VersionedSecretKeyCache getVersionedSecretKeyCache() {
        return versionedSecretKeyCache;
    }

    /**
     * @param versionedSecretKeyCache the versionedSecretKeyCache to set
     */
    public void setVersionedSecretKeyCache(VersionedSecretKeyCache versionedSecretKeyCache) {
        this.versionedSecretKeyCache = versionedSecretKeyCache;
    }

    /**
     * @return the rng
     */
    public Random getRng() {
        return rng;
    }

    /**
     * @param rng the rng to set
     */
    public void setRng(Random rng) {
        this.rng = rng;
    }

    /**
     * @return the securityProviderName
     */
    public String getSecurityProviderName() {
        return securityProviderName;
    }

    /**
     * @param securityProviderName the securityProviderName to set
     */
    public void setSecurityProviderName(String securityProviderName) {
        this.securityProviderName = securityProviderName;
    }

    public static class Builder {

        private final CryptoEventListener target;

        public Builder() {
            this.target = new CryptoEventListener();
        }

        public Builder withKeyCodes(Map<String, Integer> keyCodes) {
            target.setKeyCodes(keyCodes);
            return this;
        }

        public Builder withEncryptionKeyName(String encryptionKeyName) {
            target.setEncryptionKeyName(encryptionKeyName);
            return this;
        }

        public Builder withRNG(Random rng) {
            target.setRng(rng);
            return this;
        }

        public Builder withVersionedSecretKeyCache(VersionedSecretKeyCache cache) {
            target.setVersionedSecretKeyCache(cache);
            return this;
        }

        public Builder withSecurityProviderName(String name) {
            target.setSecurityProviderName(name);
            return this;
        }

        public CryptoEventListener build() {
            Random rng = target.getRng();

            if ( rng == null )
                throw new RuntimeException("Random number generator not configured");

            if ( target.getKeyCodes().isEmpty() )
                throw new RuntimeException("No key codes have been defined");

            target.buildKeyNameDictionary();

            return target;
        }

    }

}
