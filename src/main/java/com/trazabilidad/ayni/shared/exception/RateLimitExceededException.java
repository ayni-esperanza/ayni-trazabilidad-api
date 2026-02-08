package com.trazabilidad.ayni.shared.exception;

/**
 * Excepción lanzada cuando se excede el límite de rate limiting
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
