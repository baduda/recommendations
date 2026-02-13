package com.epam.xm.recommendations.domain;

import com.epam.xm.recommendations.infrastructure.config.AppImportProperties;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "PMD.CyclomaticComplexity"})
public class CsvImportService {
    /**
     * ETL service importing CSV price data into the database.
     *
     * <p>The import is executed on Java Virtual Threads (Project Loom) via {@code
     * Executors.newVirtualThreadPerTaskExecutor()}. Virtual threads are chosen instead of a fixed
     * platform-thread pool because file I/O and JDBC operations are predominantly blocking. Virtual
     * threads allow us to scale the number of concurrent file processing tasks without tying up OS
     * threads, improving throughput with minimal complexity and excellent observability. Batching
     * is used to reduce JDBC round-trips.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvImportService.class);

    private final JdbcTemplate jdbcTemplate;
    private final AppImportProperties importProperties;
    private final int batchSize;
    private final CsvMapper csvMapper;
    private final CsvSchema csvSchema;

    public CsvImportService(
            JdbcTemplate jdbcTemplate,
            AppImportProperties importProperties,
            @Value("${app.etl.batch-size:1000}") int batchSize) {
        this.jdbcTemplate = jdbcTemplate;
        this.importProperties = importProperties;
        this.batchSize = batchSize;
        this.csvMapper = new CsvMapper();
        this.csvSchema = CsvSchema.emptySchema().withHeader();
    }

    @PostConstruct
    public void validateDirectory() {
        Path rootPath = Path.of(importProperties.directory());
        checkPathExists(rootPath);
        checkIsDirectory(rootPath);
        checkIsReadable(rootPath);
        checkHasCsvFiles(rootPath);
        LOGGER.info("Import directory validated: {}", rootPath);
    }

    private void checkHasCsvFiles(Path path) {
        try (Stream<Path> files = Files.list(path)) {
            boolean hasCsv = files.anyMatch(p -> p.toString().endsWith(".csv"));
            if (!hasCsv) {
                throw new IllegalStateException("No CSV files found in import directory: " + path);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to check files in directory: " + path, e);
        }
    }

    private void checkPathExists(Path path) {
        if (!Files.exists(path)) {
            throw new IllegalStateException("Import directory does not exist: " + path);
        }
    }

    private void checkIsDirectory(Path path) {
        if (!Files.isDirectory(path)) {
            throw new IllegalStateException("Import path is not a directory: " + path);
        }
    }

    private void checkIsReadable(Path path) {
        if (!Files.isReadable(path)) {
            throw new IllegalStateException("Import directory is not readable: " + path);
        }
    }

    @Scheduled(cron = "${app.etl.cron}")
    @SchedulerLock(name = "csvImportLock", lockAtLeastFor = "10s", lockAtMostFor = "10m")
    @CacheEvict(
            value = {"crypto-stats", "crypto-ranges"},
            allEntries = true)
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void importCsvFiles() {
        Path rootPath = Path.of(importProperties.directory());
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            LOGGER.error("ETL directory missing: {}", rootPath);
            return;
        }

        List<Path> files = discoverCsvFiles(rootPath);
        if (files.isEmpty()) {
            LOGGER.warn("No CSV files found in {}", rootPath);
            return;
        }

        submitFilesToVirtualThreads(files);
    }

    private List<Path> discoverCsvFiles(Path rootPath) {
        try (Stream<Path> pathsList = Files.list(rootPath)) {
            return pathsList.filter(path -> path.toString().endsWith(".csv")).toList();
        } catch (IOException e) {
            LOGGER.error("Error listing files in directory: {}", importProperties.directory(), e);
            return List.of();
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void submitFilesToVirtualThreads(List<Path> files) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            files.forEach(
                    path ->
                            executor.submit(
                                    () -> {
                                        try {
                                            processFile(path);
                                        } catch (RuntimeException e) {
                                            LOGGER.error(
                                                    "Error processing file {}: {}",
                                                    path.getFileName(),
                                                    e.getMessage(),
                                                    e);
                                        }
                                    }));
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void processFile(Path path) {
        LOGGER.info("Processing file: {}", path.getFileName());
        long startTime = System.currentTimeMillis();

        try (var is = Files.newInputStream(path);
                MappingIterator<Map<?, ?>> it =
                        csvMapper.readerFor(Map.class).with(csvSchema).readValues(is)) {

            processRows(it, path);

            long duration = System.currentTimeMillis() - startTime;
            LOGGER.info("File {} imported in {} ms", path.getFileName(), duration);

        } catch (IOException e) {
            LOGGER.error("Failed to process file: {}", path, e);
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void processRows(MappingIterator<Map<?, ?>> it, Path path) {
        int totalRows = 0;
        int insertedRows = 0;
        int skippedRows = 0;
        var batch = new ArrayList<Object[]>();

        while (it.hasNext()) {
            totalRows++;
            try {
                var row = it.next();
                batch.add(mapToParams(row));

                if (batch.size() >= batchSize) {
                    insertedRows += executeBatch(batch);
                    batch.clear();
                }
            } catch (RuntimeException e) {
                LOGGER.warn(
                        "Skipping damaged row in file {}: {}", path.getFileName(), e.getMessage());
                skippedRows++;
            }
        }

        if (!batch.isEmpty()) {
            insertedRows += executeBatch(batch);
        }

        LOGGER.info(
                "Finished processing {}: Total rows: {}, Inserted/Updated: {}, Skipped: {}",
                path.getFileName(),
                totalRows,
                insertedRows,
                skippedRows);
    }

    /**
     * Maps a parsed CSV row into JDBC parameters.
     *
     * @param row parsed CSV row
     * @return array of JDBC parameters [symbol, price, timestamp]
     * @throws NumberFormatException if timestamp or price are not parseable
     */
    private Object[] mapToParams(Map<?, ?> row) {
        var timestamp = Long.parseLong(row.get("timestamp").toString());
        var symbol = row.get("symbol").toString();
        var price = new BigDecimal(row.get("price").toString());

        var dateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);

        return new Object[] {symbol, price, dateTime};
    }

    /**
     * Executes batch upsert (with ON CONFLICT DO NOTHING) to persist rows.
     *
     * <p>The unique constraint on (symbol, price_timestamp) prevents duplicates; the multi-column
     * index on (symbol, price_timestamp DESC) accelerates both upsert conflict checks and later
     * analytical queries (oldest/newest and ranges).
     *
     * @param batch list of parameter arrays built by {@link #mapToParams(Map)}
     * @return number of successfully inserted rows
     */
    private int executeBatch(List<Object[]> batch) {
        var sql =
                """
                INSERT INTO crypto_prices (symbol, price, price_timestamp)
                VALUES (?, ?, ?)
                ON CONFLICT (symbol, price_timestamp) DO NOTHING
                """;
        var result = jdbcTemplate.batchUpdate(sql, batch);
        var count = 0;
        for (var res : result) {
            if (res > 0 || res == -2) {
                count++;
            }
        }
        return count;
    }
}
