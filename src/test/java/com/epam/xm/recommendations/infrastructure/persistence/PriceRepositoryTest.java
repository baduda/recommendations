package com.epam.xm.recommendations.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.xm.recommendations.BaseIntegrationTest;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class PriceRepositoryTest extends BaseIntegrationTest {

    @Autowired private PriceRepository priceRepository;

    @BeforeEach
    void setUp() {
        priceRepository.deleteAll();
    }

    @Test
    void testSaveAndFind() {
        var now = OffsetDateTime.now();
        var entity = new PriceEntity("BTC", new BigDecimal("50000.00000000"), now);
        var saved = priceRepository.save(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        var found = priceRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSymbol()).isEqualTo("BTC");
        assertThat(found.get().getPrice()).isEqualByComparingTo("50000");
    }

    @Test
    void testFindFirstAndLastBySymbol() {
        var symbol = "ETH";
        var now = OffsetDateTime.now();

        priceRepository.saveAll(
                List.of(
                        new PriceEntity(symbol, new BigDecimal("2000"), now.minusHours(2)),
                        new PriceEntity(symbol, new BigDecimal("2100"), now.minusHours(1)),
                        new PriceEntity(symbol, new BigDecimal("2050"), now)));

        var oldest = priceRepository.findFirstBySymbolOrderByPriceTimestampAsc(symbol);
        var newest = priceRepository.findFirstBySymbolOrderByPriceTimestampDesc(symbol);

        assertThat(oldest).isPresent();
        assertThat(oldest.get().getPrice()).isEqualByComparingTo("2000");

        assertThat(newest).isPresent();
        assertThat(newest.get().getPrice()).isEqualByComparingTo("2050");
    }

    @Test
    void testMinMaxPrices() {
        var symbol = "LTC";
        var now = OffsetDateTime.now();

        priceRepository.saveAll(
                List.of(
                        new PriceEntity(symbol, new BigDecimal("100"), now.minusHours(5)),
                        new PriceEntity(symbol, new BigDecimal("150"), now.minusHours(4)),
                        new PriceEntity(symbol, new BigDecimal("120"), now.minusHours(3)),
                        new PriceEntity(symbol, new BigDecimal("80"), now.minusHours(2)),
                        new PriceEntity(symbol, new BigDecimal("110"), now.minusHours(1))));

        var start = now.minusHours(4).minusMinutes(1);
        var end = now.minusHours(2).plusMinutes(1);

        var min = priceRepository.findMinPrice(symbol, start, end);
        var max = priceRepository.findMaxPrice(symbol, start, end);

        assertThat(min)
                .isPresent()
                .hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo("80"));
        assertThat(max)
                .isPresent()
                .hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo("150"));
    }
}
