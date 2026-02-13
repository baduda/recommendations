package com.epam.xm.recommendations.domain;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Service
public class CsvImportService {
    private static final Logger log = LoggerFactory.getLogger(CsvImportService.class);

    private final JdbcTemplate jdbcTemplate;
    private final String etlDirectory;
    private final int batchSize;
    private final CsvMapper csvMapper;
    private final CsvSchema csvSchema;

    public CsvImportService(
            JdbcTemplate jdbcTemplate,
            @Value("${app.etl.directory}") String etlDirectory,
            @Value("${app.etl.batch-size:1000}") int batchSize) {
        this.jdbcTemplate = jdbcTemplate;
        this.etlDirectory = etlDirectory;
        this.batchSize = batchSize;
        this.csvMapper = new CsvMapper();
        this.csvSchema = CsvSchema.emptySchema().withHeader();
    }

    @Scheduled(cron = "${app.etl.cron}")
    @SchedulerLock(name = "csvImportLock", lockAtLeastFor = "10s", lockAtMostFor = "10m")
    public void importCsvFiles() {
        log.info("Starting CSV import from directory: {}", etlDirectory);
        var rootPath = Path.of(etlDirectory);

        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            log.error("ETL directory does not exist or is not a directory: {}", etlDirectory);
            return;
        }

        try (var paths = Files.list(rootPath);
             var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            
            paths.filter(path -> path.toString().endsWith(".csv"))
                 .forEach(path -> executor.submit(() -> {
                     try {
                         processFile(path);
                     } catch (Exception e) {
                         log.error("Error processing file {}: {}", path.getFileName(), e.getMessage(), e);
                     }
                 }));
            
        } catch (IOException e) {
            log.error("Error listing files in directory: {}", etlDirectory, e);
        }
        log.info("CSV import task submitted to virtual threads.");
    }

    private void processFile(Path path) {
        log.info("Processing file: {}", path.getFileName());
        int totalRows = 0;
        int insertedRows = 0;
        int skippedRows = 0;

        try (var is = Files.newInputStream(path)) {
            MappingIterator<Map<?, ?>> it = csvMapper
                    .readerFor(Map.class)
                    .with(csvSchema)
                    .readValues(is);

            var batch = new ArrayList<Object[]>();

            while (it.hasNext()) {
                totalRows++;
                try {
                    var row = it.next();
                    var params = mapToParams(row);
                    batch.add(params);

                    if (batch.size() >= batchSize) {
                        insertedRows += executeBatch(batch);
                        batch.clear();
                    }
                } catch (Exception e) {
                    log.warn("Skipping damaged row in file {}: {}", path.getFileName(), e.getMessage());
                    skippedRows++;
                }
            }

            if (!batch.isEmpty()) {
                insertedRows += executeBatch(batch);
            }

            log.info("Finished processing {}: Total rows: {}, Inserted/Updated: {}, Skipped: {}", 
                    path.getFileName(), totalRows, insertedRows, skippedRows);

        } catch (IOException e) {
            log.error("Failed to process file: {}", path, e);
        }
    }

    private Object[] mapToParams(Map<?, ?> row) {
        var timestamp = Long.parseLong(row.get("timestamp").toString());
        var symbol = row.get("symbol").toString();
        var price = new BigDecimal(row.get("price").toString());

        var dateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);

        return new Object[]{symbol, price, dateTime};
    }

    private int executeBatch(List<Object[]> batch) {
        var sql = """
                INSERT INTO crypto_prices (symbol, price, price_timestamp)
                VALUES (?, ?, ?)
                ON CONFLICT (symbol, price_timestamp) DO NOTHING
                """;
        var result = jdbcTemplate.batchUpdate(sql, batch);
        var count = 0;
        for (var res : result) {
            if (res > 0 || res == -2) { // -2 is SUCCESS_NO_INFO
                count++;
            }
        }
        return count;
    }
}
