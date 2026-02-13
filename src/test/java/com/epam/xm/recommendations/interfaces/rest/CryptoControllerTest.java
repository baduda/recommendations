package com.epam.xm.recommendations.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.xm.recommendations.application.mapper.CryptoMapper;
import com.epam.xm.recommendations.application.service.CryptoApplicationService;
import com.epam.xm.recommendations.domain.CryptoStats;
import com.epam.xm.recommendations.infrastructure.error.CryptoNotFoundException;
import com.epam.xm.recommendations.infrastructure.error.UnsupportedCryptoException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
public class CryptoControllerTest {

    @Autowired private org.springframework.web.context.WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean private CryptoApplicationService cryptoService;

    @MockitoBean private CryptoMapper cryptoMapper;

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        this.mockMvc =
                org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(
                                context)
                        .build();
    }

    @Test
    void getStats_ShouldReturnOk_WhenValidSymbol() throws Exception {
        var stats =
                new CryptoStats(
                        "BTC",
                        new BigDecimal("30000"),
                        new BigDecimal("40000"),
                        new BigDecimal("30000"),
                        new BigDecimal("45000"),
                        new BigDecimal("0.5"));

        given(cryptoService.getStats("BTC")).willReturn(stats);
        given(cryptoMapper.toDto(any()))
                .willReturn(
                        new com.epam.xm.recommendations.application.dto.CryptoStatsDto(
                                "BTC",
                                new BigDecimal("30000"),
                                new BigDecimal("40000"),
                                new BigDecimal("30000"),
                                new BigDecimal("45000"),
                                new BigDecimal("0.5")));

        mockMvc.perform(get("/api/v1/crypto/stats/BTC")).andExpect(status().isOk());
    }

    @Test
    void getStats_ShouldReturnNotFound_WhenSymbolDataMissing() throws Exception {
        given(cryptoService.getStats("BTC")).willThrow(new CryptoNotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/crypto/stats/BTC")).andExpect(status().isNotFound());
    }

    @Test
    void getStats_ShouldReturnBadRequest_WhenSymbolUnsupported() throws Exception {
        given(cryptoService.getStats("INVALID"))
                .willThrow(new UnsupportedCryptoException("Unsupported"));

        mockMvc.perform(get("/api/v1/crypto/stats/INVALID")).andExpect(status().isBadRequest());
    }

    @Test
    void getSorted_ShouldReturnList() throws Exception {
        given(cryptoService.getAllSortedStats()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/crypto/sorted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getHighestRange_ShouldReturnOk() throws Exception {
        var stats =
                new CryptoStats(
                        "BTC",
                        new BigDecimal("30000"),
                        new BigDecimal("40000"),
                        new BigDecimal("30000"),
                        new BigDecimal("45000"),
                        new BigDecimal("0.5"));

        given(cryptoService.getHighestRangeForDate(any(LocalDate.class))).willReturn(stats);
        // Use any() for DTO mapping if cryptoMapper is a MockitoBean
        given(cryptoMapper.toRangeDto(any()))
                .willReturn(
                        new com.epam.xm.recommendations.application.dto.CryptoRangeDto(
                                "BTC", new BigDecimal("0.5")));

        mockMvc.perform(get("/api/v1/crypto/highest-range").param("date", "2022-01-01"))
                .andExpect(status().isOk());
    }
}
