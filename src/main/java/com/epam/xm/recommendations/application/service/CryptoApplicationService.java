package com.epam.xm.recommendations.application.service;

import com.epam.xm.recommendations.domain.*;
import com.epam.xm.recommendations.infrastructure.error.CryptoNotFoundException;
import com.epam.xm.recommendations.infrastructure.error.UnsupportedCryptoException;
import com.epam.xm.recommendations.infrastructure.persistence.PriceEntity;
import com.epam.xm.recommendations.infrastructure.persistence.PriceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
/**
 * Application service orchestrating persistence access, domain analytics, and caching.
 * <p>
 * Read-only transactions are used to hint the persistence provider about the workload
 * and to avoid accidental writes. Cache annotations reduce database pressure for
 * frequently requested symbols and precomputed ranges.
 */
public class CryptoApplicationService {

    private final PriceRepository priceRepository;
    private final CryptoAnalysisService analysisService;
    private final SymbolValidator symbolValidator;

    /**
     * Creates the application service.
     *
     * @param priceRepository repository for accessing time series
     * @param analysisService domain service for computing statistics
     * @param symbolValidator validator for supported tickers
     */
    public CryptoApplicationService(PriceRepository priceRepository, 
                                   CryptoAnalysisService analysisService, 
                                   SymbolValidator symbolValidator) {
        this.priceRepository = priceRepository;
        this.analysisService = analysisService;
        this.symbolValidator = symbolValidator;
    }

    /**
     * Returns statistics for a single symbol.
     *
     * @param symbol coin ticker
     * @return computed statistics
     * @throws com.epam.xm.recommendations.infrastructure.error.UnsupportedCryptoException when the symbol is not supported
     * @throws com.epam.xm.recommendations.infrastructure.error.CryptoNotFoundException   when no data exists for the symbol
     */
    @Cacheable(value = "crypto-stats", key = "#symbol")
    public CryptoStats getStats(String symbol) {
        validateSymbol(symbol);
        List<PriceEntity> entities = priceRepository.findAllBySymbol(symbol);
        if (entities.isEmpty()) {
            throw new CryptoNotFoundException("No data found for symbol: " + symbol);
        }
        return analysisService.calculateStats(symbol, mapToPricePoints(entities));
    }

        /**
     * Returns all symbols present in storage, ranked by descending normalized range.
     *
     * @return list of stats sorted by volatility proxy
     */
    @Cacheable("crypto-ranges")
    public List<CryptoStats> getAllSortedStats() {
        List<PriceEntity> allEntities = priceRepository.findAll();
        Map<String, List<PriceEntity>> grouped = allEntities.stream()
                .collect(Collectors.groupingBy(PriceEntity::getSymbol));

        return grouped.entrySet().stream()
                .map(entry -> analysisService.calculateStats(entry.getKey(), mapToPricePoints(entry.getValue())))
                .sorted(Comparator.comparing(CryptoStats::normalizedRange).reversed())
                .toList();
    }

    /**
         * Finds the coin with the highest normalized range for a given day (UTC).
         *
         * @param date target day in UTC
         * @return optional stats for the most volatile coin on that day
         */
        public Optional<CryptoStats> getHighestRangeForDate(LocalDate date) {
        OffsetDateTime start = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = date.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        List<PriceEntity> allEntities = priceRepository.findAllByPriceTimestampBetween(start, end);
        Map<String, List<PriceEntity>> grouped = allEntities.stream()
                .collect(Collectors.groupingBy(PriceEntity::getSymbol));

        return grouped.entrySet().stream()
                .map(entry -> analysisService.calculateStats(entry.getKey(), mapToPricePoints(entry.getValue())))
                .max(Comparator.comparing(CryptoStats::normalizedRange));
    }

    /**
         * Verifies that the provided symbol is supported.
         *
         * @param symbol coin ticker
         * @throws com.epam.xm.recommendations.infrastructure.error.UnsupportedCryptoException if unsupported
         */
        private void validateSymbol(String symbol) {
        if (!symbolValidator.isSupported(symbol)) {
            throw new UnsupportedCryptoException("Symbol " + symbol + " is not supported");
        }
    }

    /**
         * Maps persistence entities to immutable domain points.
         *
         * @param entities list of JPA entities
         * @return list of {@link PricePoint}
         */
        private List<PricePoint> mapToPricePoints(List<PriceEntity> entities) {
        return entities.stream()
                .map(e -> new PricePoint(e.getPriceTimestamp().toInstant(), e.getSymbol(), e.getPrice()))
                .toList();
    }
}
