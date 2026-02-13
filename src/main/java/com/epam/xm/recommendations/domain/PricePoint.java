package com.epam.xm.recommendations.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A single market observation point.
 *
 * @param timestamp UTC instant of the quote
 * @param symbol coin ticker, cannot be blank
 * @param price strictly positive monetary value represented as {@link java.math.BigDecimal}
 */
public record PricePoint(Instant timestamp, String symbol, BigDecimal price) {
    public PricePoint {
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be empty");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }
}
