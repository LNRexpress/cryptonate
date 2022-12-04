package com.nightsky.cryptonate.factory;

import com.nightsky.keycache.VersionedSecretKey;
import javax.crypto.SecretKey;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Chris
 */
public class VersionedSecretKeyFactory {

    public static VersionedSecretKey mockedVersionedSecretKey(SecretKey key) {
        VersionedSecretKey versionedKey = mock(VersionedSecretKey.class);
        when(versionedKey.getAlgorithm()).thenReturn(key.getAlgorithm());
        when(versionedKey.getEncoded()).thenReturn(key.getEncoded());
        when(versionedKey.getFormat()).thenReturn(key.getFormat());
        when(versionedKey.getVersion()).thenReturn(1);
        return versionedKey;
    }

}
