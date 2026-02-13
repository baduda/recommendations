package com.epam.xm.recommendations.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<PriceEntity, Long> {

    /**
     * Finds the oldest record for a given symbol.
     */
    Optional<PriceEntity> findFirstBySymbolOrderByPriceTimestampAsc(String symbol);

    /**
     * Finds the newest record for a given symbol.
     */
    Optional<PriceEntity> findFirstBySymbolOrderByPriceTimestampDesc(String symbol);

    /**
     * Returns the minimum price for a given symbol and time interval.
     */
    @Query("SELECT MIN(p.price) FROM PriceEntity p WHERE p.symbol = :symbol AND p.priceTimestamp BETWEEN :start AND :end")
    Optional<BigDecimal> findMinPrice(@Param("symbol") String symbol, 
                                      @Param("start") OffsetDateTime start, 
                                      @Param("end") OffsetDateTime end);

    /**
     * Returns the maximum price for a given symbol and time interval.
     */
    @Query("SELECT MAX(p.price) FROM PriceEntity p WHERE p.symbol = :symbol AND p.priceTimestamp BETWEEN :start AND :end")
    Optional<BigDecimal> findMaxPrice(@Param("symbol") String symbol, 
                                      @Param("start") OffsetDateTime start, 
                                      @Param("end") OffsetDateTime end);

    @Query("SELECT DISTINCT p.symbol FROM PriceEntity p")
    List<String> findAllSymbols();

    List<PriceEntity> findAllBySymbol(String symbol);

    List<PriceEntity> findAllBySymbolAndPriceTimestampBetween(String symbol, OffsetDateTime start, OffsetDateTime end);
}
