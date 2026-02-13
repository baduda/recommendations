package com.epam.xm.recommendations.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainLogicTest {

    private final CryptoAnalysisService analysisService = new CryptoAnalysisService();

    @Test
    void testCalculateStatsCorrectness() {
        var symbol = "BTC";
        var now = Instant.now();
        var points = List.of(
                new PricePoint(now.minusSeconds(100), symbol, new BigDecimal("40000")),
                new PricePoint(now.minusSeconds(50), symbol, new BigDecimal("42000")),
                new PricePoint(now, symbol, new BigDecimal("41000")),
                new PricePoint(now.plusSeconds(50), symbol, new BigDecimal("38000"))
        );

        var stats = analysisService.calculateStats(symbol, points);

        assertThat(stats.symbol()).isEqualTo(symbol);
        assertThat(stats.oldestPrice()).isEqualByComparingTo(new BigDecimal("40000"));
        assertThat(stats.newestPrice()).isEqualByComparingTo(new BigDecimal("38000"));
        assertThat(stats.minPrice()).isEqualByComparingTo(new BigDecimal("38000"));
        assertThat(stats.maxPrice()).isEqualByComparingTo(new BigDecimal("42000"));
        // (42000 - 38000) / 38000 = 4000 / 38000 = 0.10526... -> 0.1053 (RoundingMode.HALF_UP, 4 scale)
        assertThat(stats.normalizedRange()).isEqualByComparingTo(new BigDecimal("0.1053"));
    }

    @Test
    void testEmptyListThrowsException() {
        assertThatThrownBy(() -> analysisService.calculateStats("BTC", Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testZeroPriceThrowsException() {
        assertThatThrownBy(() -> new PricePoint(Instant.now(), "BTC", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testNegativePriceThrowsException() {
        assertThatThrownBy(() -> new PricePoint(Instant.now(), "BTC", new BigDecimal("-100")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testNormalizedRangeAccuracy() {
        var symbol = "ETH";
        var points = List.of(
                new PricePoint(Instant.now(), symbol, new BigDecimal("100")),
                new PricePoint(Instant.now(), symbol, new BigDecimal("110"))
        );
        var stats = analysisService.calculateStats(symbol, points);
        // (110 - 100) / 100 = 10 / 100 = 0.1000
        assertThat(stats.normalizedRange()).isEqualByComparingTo(new BigDecimal("0.1000"));
    }

    @Test
    void testSymbolValidation() {
        var validator = new SetBasedSymbolValidator(List.of("BTC", "ETH", "LTC"));
        assertThat(validator.isSupported("BTC")).isTrue();
        assertThat(validator.isSupported("ETH")).isTrue();
        assertThat(validator.isSupported("XRP")).isFalse();
    }
}
