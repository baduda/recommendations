package com.epam.xm.recommendations.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class CryptoAnalysisServiceTest {

    private final CryptoAnalysisService service = new CryptoAnalysisService();

    @Test
    void shouldCalculateStatsCorrectly() {
        String symbol = "BTC";
        List<PricePoint> pricePoints =
                List.of(
                        new PricePoint(Instant.ofEpochMilli(1000), symbol, new BigDecimal("40000")),
                        new PricePoint(Instant.ofEpochMilli(2000), symbol, new BigDecimal("42000")),
                        new PricePoint(
                                Instant.ofEpochMilli(3000), symbol, new BigDecimal("38000")));

        CryptoStats stats = service.calculateStats(symbol, pricePoints);

        assertEquals(symbol, stats.symbol());
        assertEquals(new BigDecimal("40000"), stats.oldestPrice());
        assertEquals(new BigDecimal("38000"), stats.newestPrice());
        assertEquals(new BigDecimal("38000"), stats.minPrice());
        assertEquals(new BigDecimal("42000"), stats.maxPrice());

        // (42000 - 38000) / 38000 = 4000 / 38000 = 0.105263... -> 0.1053 (HALF_UP)
        BigDecimal expectedRange =
                new BigDecimal("4000").divide(new BigDecimal("38000"), 4, RoundingMode.HALF_UP);
        assertEquals(expectedRange, stats.normalizedRange());
    }

    @Test
    void shouldThrowExceptionWhenPricePointsEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateStats("BTC", Collections.emptyList()));
    }

    @Test
    void shouldThrowExceptionWhenSymbolMismatch() {
        List<PricePoint> pricePoints =
                List.of(new PricePoint(Instant.ofEpochMilli(1000), "ETH", new BigDecimal("40000")));
        assertThrows(
                IllegalArgumentException.class, () -> service.calculateStats("BTC", pricePoints));
    }
}
