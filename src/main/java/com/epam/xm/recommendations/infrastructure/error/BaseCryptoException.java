package com.epam.xm.recommendations.infrastructure.error;

import java.io.Serial;

/**
 * Base type for domain/application business exceptions.
 * <p>
 * Extends {@link RuntimeException} to avoid mandatory catch and encourages handling
 * at the boundary (see {@code GlobalExceptionHandler}).
 */
public abstract class BaseCryptoException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a human-readable message.
     *
     * @param message problem description intended for logging and API exposure
     */
    protected BaseCryptoException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and an underlying cause.
     *
     * @param message problem description
     * @param cause   underlying reason
     */
    protected BaseCryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
