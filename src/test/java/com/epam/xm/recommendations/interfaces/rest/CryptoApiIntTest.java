package com.epam.xm.recommendations.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.xm.recommendations.BaseIntegrationTest;
import com.epam.xm.recommendations.infrastructure.persistence.PriceEntity;
import com.epam.xm.recommendations.infrastructure.persistence.PriceRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CryptoApiIntTest extends BaseIntegrationTest {

    @Autowired private PriceRepository priceRepository;

    @BeforeEach
    void setup() {
        priceRepository.deleteAll();

        priceRepository.save(
                new PriceEntity(
                        "BTC",
                        new BigDecimal("40000"),
                        OffsetDateTime.of(2022, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC)));
        priceRepository.save(
                new PriceEntity(
                        "BTC",
                        new BigDecimal("45000"),
                        OffsetDateTime.of(2022, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)));
        priceRepository.save(
                new PriceEntity(
                        "BTC",
                        new BigDecimal("35000"),
                        OffsetDateTime.of(2022, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC)));

        priceRepository.save(
                new PriceEntity(
                        "ETH",
                        new BigDecimal("2000"),
                        OffsetDateTime.of(2022, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC)));
        priceRepository.save(
                new PriceEntity(
                        "ETH",
                        new BigDecimal("2500"),
                        OffsetDateTime.of(2022, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)));
    }

    @Test
    void shouldReturnStatsForBtc() throws Exception {
        mockMvc.perform(get("/api/v1/crypto/stats/BTC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("BTC"))
                .andExpect(jsonPath("$.minPrice").value(35000.0))
                .andExpect(jsonPath("$.maxPrice").value(45000.0));
    }

    @Test
    void shouldReturnSortedRange() throws Exception {
        // BTC range: (45-35)/35 = 10/35 = 0.2857
        // ETH range: (2.5-2)/2 = 0.5/2 = 0.25
        mockMvc.perform(get("/api/v1/crypto/sorted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("BTC"))
                .andExpect(jsonPath("$[1].symbol").value("ETH"));
    }

    @Test
    void shouldReturnHighestRangeForDate() throws Exception {
        mockMvc.perform(get("/api/v1/crypto/highest-range").param("date", "2022-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("BTC"));
    }
}
