package com.epam.xm.recommendations.interfaces.rest;

import com.epam.xm.recommendations.application.service.CryptoApplicationService;
import com.epam.xm.recommendations.domain.CryptoAnalysisService;
import com.epam.xm.recommendations.domain.CryptoStats;
import com.epam.xm.recommendations.domain.SymbolValidator;
import com.epam.xm.recommendations.infrastructure.config.RateLimitingFilter;
import com.epam.xm.recommendations.infrastructure.persistence.PriceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@EnableCaching
class CachingTest {

    @MockitoBean
    private RateLimitingFilter rateLimitingFilter;

    @TestConfiguration
    static class CachingConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("crypto-stats", "crypto-ranges");
        }
    }

    @Autowired
    private CryptoApplicationService cryptoApplicationService;

    @MockitoBean
    private PriceRepository priceRepository;

    @MockitoBean
    private CryptoAnalysisService analysisService;

    @MockitoBean
    private SymbolValidator symbolValidator;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldCacheStats() {
        assertNotNull(cacheManager, "CacheManager should be present");
        assertNotNull(cryptoApplicationService, "CryptoApplicationService should be present");
        String symbol = "BTC";
        CryptoStats stats = new CryptoStats(symbol, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ONE);
        
        when(symbolValidator.isSupported(symbol)).thenReturn(true);
        com.epam.xm.recommendations.infrastructure.persistence.PriceEntity entity = 
            new com.epam.xm.recommendations.infrastructure.persistence.PriceEntity(symbol, BigDecimal.TEN, java.time.OffsetDateTime.now());
        
        when(priceRepository.findAllBySymbol(symbol)).thenReturn(java.util.List.of(entity));
        when(analysisService.calculateStats(eq(symbol), any())).thenReturn(stats);

        // Clear cache before test if it exists
        if (cacheManager.getCache("crypto-stats") != null) {
            cacheManager.getCache("crypto-stats").clear();
        }

        // First call - should call repository and service
        cryptoApplicationService.getStats(symbol);
        
        // Second call - should return from cache
        cryptoApplicationService.getStats(symbol);

        verify(analysisService, times(1)).calculateStats(eq(symbol), any());
        assertNotNull(cacheManager.getCache("crypto-stats").get(symbol));
    }
}
