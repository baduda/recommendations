package com.epam.xm.recommendations.domain;

import java.math.BigDecimal;

public record CryptoStats(
        String symbol,
        BigDecimal oldestPrice,
        BigDecimal newestPrice,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal normalizedRange
) {
    public CryptoStats {
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be empty");
        }
        // normalizedRange = (max - min) / min
    }
}
