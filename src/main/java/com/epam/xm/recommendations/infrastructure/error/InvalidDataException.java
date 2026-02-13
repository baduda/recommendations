package com.epam.xm.recommendations.infrastructure.error;

import java.io.Serial;

public class InvalidDataException extends BaseCryptoException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidDataException(String message) {
        super(message);
    }
}
