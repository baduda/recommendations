package com.epam.xm.recommendations.infrastructure.error;

import java.io.Serial;

/**
 * Thrown when no price data exists for the requested symbol.
 */
public class CryptoNotFoundException extends BaseCryptoException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param message details about the missing symbol
     */
    public CryptoNotFoundException(String message) {
        super(message);
    }

    /**
     * @param message message
     * @param cause   underlying storage or mapping error
     */
    public CryptoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
