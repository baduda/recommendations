package com.epam.xm.recommendations.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record PricePoint(Instant timestamp, String symbol, BigDecimal price) {
    public PricePoint {
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be empty");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }
}
