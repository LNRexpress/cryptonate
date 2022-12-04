package com.nightsky.cryptonate.factory;

import static com.nightsky.cryptonate.factory.SecretKeyFactory.createAesKey;
import static com.nightsky.cryptonate.factory.VersionedSecretKeyFactory.mockedVersionedSecretKey;
import com.nightsky.keycache.VersionedSecretKey;
import com.nightsky.keycache.VersionedSecretKeyCache;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Chris
 */
public class VersionedSecretKeyCacheFactory {

    public static VersionedSecretKeyCache mockedVersionedSecretKeyCache() {
        VersionedSecretKey key = mockedVersionedSecretKey(createAesKey());
        int keyVersion = key.getVersion();

        VersionedSecretKeyCache secretKeyCache = mock(VersionedSecretKeyCache.class);
        when(secretKeyCache.getKey(eq(SecretKeyFactory.KEY_NAME))).thenReturn(key);
        when(secretKeyCache.getKey(eq(SecretKeyFactory.KEY_NAME), eq(keyVersion))).thenReturn(key);
        return secretKeyCache;
    }

    public static VersionedSecretKeyCache mockedVersionedSecretKeyCache(VersionedSecretKey key) {
        int keyVersion = key.getVersion();

        VersionedSecretKeyCache secretKeyCache = mock(VersionedSecretKeyCache.class);
        when(secretKeyCache.getKey(eq(SecretKeyFactory.KEY_NAME))).thenReturn(key);
        when(secretKeyCache.getKey(eq(SecretKeyFactory.KEY_NAME), eq(keyVersion))).thenReturn(key);
        return secretKeyCache;
    }

}
