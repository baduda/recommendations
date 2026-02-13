package com.epam.xm.recommendations.infrastructure.error;

import java.io.Serial;

/**
 * Thrown when a request references a symbol that is not supported by configuration.
 */
public class UnsupportedCryptoException extends BaseCryptoException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param message human-readable explanation including the symbol
     */
    public UnsupportedCryptoException(String message) {
        super(message);
    }

    /**
     * @param message message
     * @param cause   underlying cause
     */
    public UnsupportedCryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
