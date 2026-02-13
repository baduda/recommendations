package com.epam.xm.recommendations.infrastructure.persistence;

import com.epam.xm.recommendations.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.flyway.enabled=true",
    "spring.flyway.locations=classpath:db/migration"
})
@Transactional
@Import(TestcontainersConfiguration.class)
class PriceRepositoryTest {

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS crypto_prices (id BIGSERIAL PRIMARY KEY, symbol VARCHAR(10), price NUMERIC, price_timestamp TIMESTAMP WITH TIME ZONE, created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(), UNIQUE(symbol, price_timestamp))");
        priceRepository.deleteAll();
    }

    @Test
    void testSaveAndFind() {
        var now = OffsetDateTime.now();
        var entity = new PriceEntity("BTC", new BigDecimal("50000.00000000"), now);
        var saved = priceRepository.save(entity);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.createdAt()).isNotNull();

        var found = priceRepository.findById(saved.id());
        assertThat(found).isPresent();
        assertThat(found.get().symbol()).isEqualTo("BTC");
        assertThat(found.get().price()).isEqualByComparingTo("50000");
    }

    @Test
    void testFindFirstAndLastBySymbol() {
        var symbol = "ETH";
        var now = OffsetDateTime.now();

        priceRepository.saveAll(List.of(
            new PriceEntity(symbol, new BigDecimal("2000"), now.minusHours(2)),
            new PriceEntity(symbol, new BigDecimal("2100"), now.minusHours(1)),
            new PriceEntity(symbol, new BigDecimal("2050"), now)
        ));

        var oldest = priceRepository.findFirstBySymbolOrderByPriceTimestampAsc(symbol);
        var newest = priceRepository.findFirstBySymbolOrderByPriceTimestampDesc(symbol);

        assertThat(oldest).isPresent();
        assertThat(oldest.get().price()).isEqualByComparingTo("2000");

        assertThat(newest).isPresent();
        assertThat(newest.get().price()).isEqualByComparingTo("2050");
    }

    @Test
    void testMinMaxPrices() {
        var symbol = "LTC";
        var now = OffsetDateTime.now();

        priceRepository.saveAll(List.of(
            new PriceEntity(symbol, new BigDecimal("100"), now.minusHours(5)),
            new PriceEntity(symbol, new BigDecimal("150"), now.minusHours(4)),
            new PriceEntity(symbol, new BigDecimal("120"), now.minusHours(3)),
            new PriceEntity(symbol, new BigDecimal("80"), now.minusHours(2)),
            new PriceEntity(symbol, new BigDecimal("110"), now.minusHours(1))
        ));

        var start = now.minusHours(4).minusMinutes(1);
        var end = now.minusHours(2).plusMinutes(1);

        var min = priceRepository.findMinPrice(symbol, start, end);
        var max = priceRepository.findMaxPrice(symbol, start, end);

        assertThat(min).isPresent().hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo("80"));
        assertThat(max).isPresent().hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo("150"));
    }
}
