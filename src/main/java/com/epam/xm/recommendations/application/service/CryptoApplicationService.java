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
public class CryptoApplicationService {

    private final PriceRepository priceRepository;
    private final CryptoAnalysisService analysisService;
    private final SymbolValidator symbolValidator;

    public CryptoApplicationService(PriceRepository priceRepository, 
                                   CryptoAnalysisService analysisService, 
                                   SymbolValidator symbolValidator) {
        this.priceRepository = priceRepository;
        this.analysisService = analysisService;
        this.symbolValidator = symbolValidator;
    }

    @Cacheable(value = "crypto-stats", key = "#symbol")
    public CryptoStats getStats(String symbol) {
        validateSymbol(symbol);
        List<PriceEntity> entities = priceRepository.findAllBySymbol(symbol);
        if (entities.isEmpty()) {
            throw new CryptoNotFoundException("No data found for symbol: " + symbol);
        }
        return analysisService.calculateStats(symbol, mapToPricePoints(entities));
    }

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

    private void validateSymbol(String symbol) {
        if (!symbolValidator.isSupported(symbol)) {
            throw new UnsupportedCryptoException("Symbol " + symbol + " is not supported");
        }
    }

    private List<PricePoint> mapToPricePoints(List<PriceEntity> entities) {
        return entities.stream()
                .map(e -> new PricePoint(e.getPriceTimestamp().toInstant(), e.getSymbol(), e.getPrice()))
                .toList();
    }
}
