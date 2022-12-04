package com.nightsky.cryptonate.factory.exception;

/**
 *
 * @author Chris
 */
public class SecretKeyCreationException extends RuntimeException {

    public SecretKeyCreationException() {
        super();
    }

    public SecretKeyCreationException(String message) {
        super(message);
    }

    public SecretKeyCreationException(Throwable t) {
        super(t);
    }

    public SecretKeyCreationException(String message, Throwable t) {
        super(message, t);
    }

}
