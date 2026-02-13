package com.epam.xm.recommendations.infrastructure.error;

import java.io.Serial;

public class CryptoNotFoundException extends BaseCryptoException {
    @Serial
    private static final long serialVersionUID = 1L;

    public CryptoNotFoundException(String message) {
        super(message);
    }

    public CryptoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
