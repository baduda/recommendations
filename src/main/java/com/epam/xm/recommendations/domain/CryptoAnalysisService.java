package com.epam.xm.recommendations.domain;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class CryptoAnalysisService {

    public CryptoStats calculateStats(String symbol, List<PricePoint> pricePoints) {
        if (pricePoints.isEmpty()) {
            throw new IllegalArgumentException("Price points list cannot be empty");
        }

        // Verify that all points belong to the same symbol (optional but useful)
        for (var pp : pricePoints) {
            if (!pp.symbol().equals(symbol)) {
                throw new IllegalArgumentException("Price point symbol mismatch: expected " + symbol + " but found " + pp.symbol());
            }
        }

        var oldest = pricePoints.stream()
                .min(Comparator.comparing(PricePoint::timestamp))
                .orElseThrow();

        var newest = pricePoints.stream()
                .max(Comparator.comparing(PricePoint::timestamp))
                .orElseThrow();

        var minPrice = pricePoints.stream()
                .map(PricePoint::price)
                .min(BigDecimal::compareTo)
                .orElseThrow();

        var maxPrice = pricePoints.stream()
                .map(PricePoint::price)
                .max(BigDecimal::compareTo)
                .orElseThrow();

        // (max - min) / min
        var normalizedRange = maxPrice.subtract(minPrice)
                .divide(minPrice, 4, RoundingMode.HALF_UP);

        return new CryptoStats(
                symbol,
                oldest.price(),
                newest.price(),
                minPrice,
                maxPrice,
                normalizedRange
        );
    }
}
