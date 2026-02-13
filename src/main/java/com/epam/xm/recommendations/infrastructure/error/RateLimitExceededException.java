package com.epam.xm.recommendations.infrastructure.error;

import java.io.Serial;
import java.io.Serializable;

public class RateLimitExceededException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
