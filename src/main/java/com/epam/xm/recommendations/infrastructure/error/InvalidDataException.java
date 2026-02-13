package com.epam.xm.recommendations.infrastructure.error;

import java.io.Serial;

/** Thrown when provided input data fails domain validation (e.g., non-positive price). */
public class InvalidDataException extends BaseCryptoException {
    @Serial private static final long serialVersionUID = 1L;

    /**
     * @param message descriptive validation failure
     */
    public InvalidDataException(String message) {
        super(message);
    }

    /**
     * @param message message
     * @param cause underlying cause (parsing/format/validation library)
     */
    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
