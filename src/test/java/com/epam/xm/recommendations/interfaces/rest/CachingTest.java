package com.epam.xm.recommendations.interfaces.rest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.epam.xm.recommendations.application.service.CryptoApplicationService;
import com.epam.xm.recommendations.domain.CryptoAnalysisService;
import com.epam.xm.recommendations.domain.CryptoStats;
import com.epam.xm.recommendations.domain.SymbolValidator;
import com.epam.xm.recommendations.infrastructure.config.RateLimitingFilter;
import com.epam.xm.recommendations.infrastructure.persistence.PriceRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@EnableCaching
@Transactional
class CachingTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @MockitoBean private RateLimitingFilter rateLimitingFilter;

    @TestConfiguration
    static class CachingConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("crypto-stats", "crypto-ranges");
        }
    }

    @Autowired private CryptoApplicationService cryptoApplicationService;

    @MockitoBean private PriceRepository priceRepository;

    @MockitoBean private CryptoAnalysisService analysisService;

    @MockitoBean private SymbolValidator symbolValidator;

    @Autowired private CacheManager cacheManager;

    @Test
    void shouldCacheStats() {
        assertNotNull(cacheManager, "CacheManager should be present");
        assertNotNull(cryptoApplicationService, "CryptoApplicationService should be present");
        String symbol = "BTC";
        CryptoStats stats =
                new CryptoStats(
                        symbol,
                        BigDecimal.ONE,
                        BigDecimal.TEN,
                        BigDecimal.ZERO,
                        BigDecimal.TEN,
                        BigDecimal.ONE);

        when(symbolValidator.isSupported(symbol)).thenReturn(true);
        com.epam.xm.recommendations.infrastructure.persistence.PriceEntity entity =
                new com.epam.xm.recommendations.infrastructure.persistence.PriceEntity(
                        symbol, BigDecimal.TEN, java.time.OffsetDateTime.now());

        when(priceRepository.findAllBySymbol(symbol)).thenReturn(java.util.List.of(entity));
        when(analysisService.calculateStats(eq(symbol), any())).thenReturn(stats);

        // Clear cache before test if it exists
        var cache = cacheManager.getCache("crypto-stats");
        if (cache != null) {
            cache.clear();
        }

        // First call - should call repository and service
        cryptoApplicationService.getStats(symbol);

        // Second call - should return from cache
        cryptoApplicationService.getStats(symbol);

        verify(analysisService, times(1)).calculateStats(eq(symbol), any());
        assertNotNull(cache.get(symbol));
    }
}
