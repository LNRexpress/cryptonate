package com.nightsky.cryptonate.factory;

import com.nightsky.cryptonate.factory.exception.SecretKeyCreationException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 *
 * @author Chris
 */
public class SecretKeyFactory {

    public static final String AES_ALGORITHM_NAME = "AES";

    public static final String KEY_NAME = "test-key";

    public static SecretKey createAesKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(AES_ALGORITHM_NAME);
            kg.init(128);
            return kg.generateKey();
        } catch (Exception e) {
            throw new SecretKeyCreationException(e);
        }
    }

}
