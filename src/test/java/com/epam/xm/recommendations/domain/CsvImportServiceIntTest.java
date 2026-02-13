package com.epam.xm.recommendations.domain;

import com.epam.xm.recommendations.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.flyway.enabled=true",
    "app.etl.cron=0 0 0 * * *"
})
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class CsvImportServiceIntTest {

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldImportCsvFilesAndHandleDuplicates() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS crypto_prices (id BIGSERIAL PRIMARY KEY, symbol VARCHAR(10), price NUMERIC, price_timestamp TIMESTAMP WITH TIME ZONE, created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(), UNIQUE(symbol, price_timestamp))");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS shedlock (name VARCHAR(64), lock_until TIMESTAMP, locked_at TIMESTAMP, locked_by VARCHAR(255), PRIMARY KEY (name))");

        // Add a line with invalid data to test skipping
        var errorFile = Path.of("src/main/resources/prices/error_test.csv");
        try {
            Files.writeString(errorFile, "timestamp,symbol,price\ninvalid,ERR,100\n1641016800000,BTC,40000\n");
            
            // When: Running import for the first time
            csvImportService.importCsvFiles();

            // Then: Data should be in the database
            var count1 = jdbcTemplate.queryForObject("SELECT count(*) FROM crypto_prices", Integer.class);
            assertThat(count1).isNotNull().isGreaterThan(0);

            // When: Running import for the second time
            csvImportService.importCsvFiles();

            // Then: Count should remain the same (due to ON CONFLICT DO NOTHING)
            var count2 = jdbcTemplate.queryForObject("SELECT count(*) FROM crypto_prices", Integer.class);
            assertThat(count2).isEqualTo(count1);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try { Files.deleteIfExists(errorFile); } catch (Exception ignored) {}
        }
    }
}
