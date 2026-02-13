package com.epam.xm.recommendations.infrastructure.error;

import java.io.Serial;

public class UnsupportedCryptoException extends BaseCryptoException {
    @Serial
    private static final long serialVersionUID = 1L;

    public UnsupportedCryptoException(String message) {
        super(message);
    }

    public UnsupportedCryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
