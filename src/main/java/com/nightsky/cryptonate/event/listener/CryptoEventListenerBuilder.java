package com.nightsky.cryptonate.event.listener;

import com.nightsky.keycache.VersionedSecretKeyCache;
import java.util.Map;
import java.util.Random;
import org.bouncycastle.crypto.fips.FipsSecureRandom;

/**
 *
 * @author Chris
 */
public class CryptoEventListenerBuilder {

    private final CryptoEventListener target;

    public CryptoEventListenerBuilder() {
        this.target = new CryptoEventListener();
    }

    public CryptoEventListenerBuilder withKeyCodes(Map<String, Integer> keyCodes) {
        target.setKeyCodes(keyCodes);
        return this;
    }

    public CryptoEventListenerBuilder withKeyNames(Map<String, String> keyNames) {
        target.setKeyNames(keyNames);
        return this;
    }

    public CryptoEventListenerBuilder withEncryptionKeyName(String encryptionKeyName) {
        target.setEncryptionKeyName(encryptionKeyName);
        return this;
    }

    public CryptoEventListenerBuilder withRNG(Random rng) {
        target.setRng(rng);
        return this;
    }

    public CryptoEventListenerBuilder withVersionedSecretKeyCache(VersionedSecretKeyCache cache) {
        target.setVersionedSecretKeyCache(cache);
        return this;
    }

    public CryptoEventListener build() {
        Random rng = target.getRng();

        if ( rng == null )
            throw new RuntimeException("Random number generator not configured");

        if ( !(rng instanceof FipsSecureRandom) )
            throw new RuntimeException("Random number generator is not secure and compliant");

        return target;
    }

}
