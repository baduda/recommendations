package com.epam.xm.recommendations.infrastructure.error;

import java.io.Serial;

public abstract class BaseCryptoException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected BaseCryptoException(String message) {
        super(message);
    }

    protected BaseCryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
