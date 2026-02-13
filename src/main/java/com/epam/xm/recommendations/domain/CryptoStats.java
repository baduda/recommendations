package com.epam.xm.recommendations.domain;

import java.math.BigDecimal;

/**
 * Immutable snapshot of computed statistics for a cryptocurrency symbol.
 *
 * <p>All monetary values are represented as {@link java.math.BigDecimal} to avoid precision loss.
 * The field {@code normalizedRange} follows the formula (max - min) / min and is rounded to scale 4
 * with HALF_UP at calculation time to provide stable ordering for sorting while keeping payload
 * small.
 */
public record CryptoStats(
        String symbol,
        BigDecimal oldestPrice,
        BigDecimal newestPrice,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal normalizedRange) {
    /**
     * Validates invariant fields.
     *
     * @throws IllegalArgumentException if {@code symbol} is blank
     */
    public CryptoStats {
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be empty");
        }
        // normalizedRange = (max - min) / min
    }
}
