package com.epam.xm.recommendations.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Performs core domain analytics on crypto price time series.
 *
 * <p>The service intentionally performs all arithmetic using {@link java.math.BigDecimal} to avoid
 * precision loss inherent to binary floating-point types. Market data often mixes very large and
 * very small magnitudes; rounding errors would compound when computing ratios (e.g., normalized
 * range), potentially changing ordering and downstream decisions. By fixing scale and using {@link
 * java.math.RoundingMode#HALF_UP} the results are stable and auditable.
 */
@Service
public class CryptoAnalysisService {

    /**
     * Calculates summary statistics for a symbol over the provided points.
     *
     * <p>The normalized range is defined as (max - min) / min and serves as a unit-less volatility
     * proxy that allows comparing coins with different absolute prices. Division is performed with
     * scale 4 and {@link java.math.RoundingMode#HALF_UP} to provide a compact representation for UI
     * sorting while preserving ordering consistency.
     *
     * @param symbol the coin ticker all points must belong to
     * @param pricePoints time-ordered or unordered list of price points; must not be empty
     * @return computed {@link CryptoStats} including oldest, newest, min, max and normalized range
     * @throws IllegalArgumentException if list is empty or contains points of another symbol
     */
    public CryptoStats calculateStats(String symbol, List<PricePoint> pricePoints) {
        if (pricePoints.isEmpty()) {
            throw new IllegalArgumentException("Price points list cannot be empty");
        }

        // Verify that all points belong to the same symbol (optional but useful)
        for (var pp : pricePoints) {
            if (!pp.symbol().equals(symbol)) {
                throw new IllegalArgumentException(
                        "Price point symbol mismatch: expected "
                                + symbol
                                + " but found "
                                + pp.symbol());
            }
        }

        var oldest =
                pricePoints.stream().min(Comparator.comparing(PricePoint::timestamp)).orElseThrow();

        var newest =
                pricePoints.stream().max(Comparator.comparing(PricePoint::timestamp)).orElseThrow();

        var minPrice =
                pricePoints.stream()
                        .map(PricePoint::price)
                        .min(BigDecimal::compareTo)
                        .orElseThrow();

        var maxPrice =
                pricePoints.stream()
                        .map(PricePoint::price)
                        .max(BigDecimal::compareTo)
                        .orElseThrow();

        // (max - min) / min
        var normalizedRange = maxPrice.subtract(minPrice).divide(minPrice, 4, RoundingMode.HALF_UP);

        return new CryptoStats(
                symbol, oldest.price(), newest.price(), minPrice, maxPrice, normalizedRange);
    }
}
