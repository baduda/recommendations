package com.epam.xm.recommendations.infrastructure.error;

import java.io.Serial;
import java.io.Serializable;

/**
 * Thrown when a client exceeds the configured request rate limits.
 * Handled and translated into HTTP 429 by {@link GlobalExceptionHandler}.
 */
public class RateLimitExceededException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param message description including limit details
     */
    public RateLimitExceededException(String message) {
        super(message);
    }

    /**
     * @param message message
     * @param cause   underlying cause
     */
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
