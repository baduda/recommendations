package com.epam.xm.recommendations.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
/**
 * JPA repository for price time series.
 *
 * <p>Index strategy: a composite index on (symbol, price_timestamp DESC) is defined at the table
 * level to efficiently support the typical access patterns used by the domain/application layer: -
 * fetching oldest/newest quotes per symbol (order-by + limit 1) - range scans for a symbol between
 * two timestamps The DESC order on the timestamp aligns with queries requesting newest-first while
 * still serving ASC scans via index-only traversal in PostgreSQL.
 */
public interface PriceRepository extends JpaRepository<PriceEntity, Long> {

    /**
     * Finds the oldest record for a given symbol using the index on (symbol, price_timestamp).
     *
     * @param symbol coin ticker
     * @return optional oldest record
     */
    Optional<PriceEntity> findFirstBySymbolOrderByPriceTimestampAsc(String symbol);

    /**
     * Finds the newest record for a given symbol using the index ordering.
     *
     * @param symbol coin ticker
     * @return optional newest record
     */
    Optional<PriceEntity> findFirstBySymbolOrderByPriceTimestampDesc(String symbol);

    /**
     * Returns the minimum price for a given symbol and time interval.
     *
     * @param symbol coin ticker
     * @param start inclusive start of interval (UTC)
     * @param end inclusive end of interval (UTC)
     * @return optional minimum price
     */
    @Query(
            "SELECT MIN(p.price) FROM PriceEntity p WHERE p.symbol = :symbol AND p.priceTimestamp BETWEEN :start AND :end")
    Optional<BigDecimal> findMinPrice(
            @Param("symbol") String symbol,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);

    /**
     * Returns the maximum price for a given symbol and time interval.
     *
     * @param symbol coin ticker
     * @param start inclusive start of interval (UTC)
     * @param end inclusive end of interval (UTC)
     * @return optional maximum price
     */
    @Query(
            "SELECT MAX(p.price) FROM PriceEntity p WHERE p.symbol = :symbol AND p.priceTimestamp BETWEEN :start AND :end")
    Optional<BigDecimal> findMaxPrice(
            @Param("symbol") String symbol,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);

    /**
     * @return distinct list of all symbols present in storage
     */
    @Query("SELECT DISTINCT p.symbol FROM PriceEntity p")
    List<String> findAllSymbols();

    /**
     * @param symbol coin ticker
     * @return all quotes for the symbol
     */
    List<PriceEntity> findAllBySymbol(String symbol);

    /**
     * @param start inclusive start (UTC)
     * @param end inclusive end (UTC)
     * @return all quotes in the time window across symbols
     */
    List<PriceEntity> findAllByPriceTimestampBetween(OffsetDateTime start, OffsetDateTime end);

    /**
     * @param symbol coin ticker
     * @param start inclusive start (UTC)
     * @param end inclusive end (UTC)
     * @return all quotes for the symbol in the time window
     */
    List<PriceEntity> findAllBySymbolAndPriceTimestampBetween(
            String symbol, OffsetDateTime start, OffsetDateTime end);
}
