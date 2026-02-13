package com.epam.xm.recommendations.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.xm.recommendations.BaseIntegrationTest;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class CsvImportServiceIntTest extends BaseIntegrationTest {

    @Autowired private CsvImportService csvImportService;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void shouldImportCsvFilesAndHandleDuplicates() {
        // Add a line with invalid data to test skipping
        var errorFile = Path.of("src/main/resources/prices/error_test.csv");
        try {
            Files.writeString(
                    errorFile,
                    "timestamp,symbol,price\ninvalid,ERR,100\n1641016800000,BTC,40000\n");

            // When: Running import for the first time
            csvImportService.importCsvFiles();

            // Then: Data should be in the database
            var count1 =
                    jdbcTemplate.queryForObject(
                            "SELECT count(*) FROM crypto_prices", Integer.class);
            assertThat(count1).isNotNull().isGreaterThan(0);

            // When: Running import for the second time
            csvImportService.importCsvFiles();

            // Then: Count should remain the same (due to ON CONFLICT DO NOTHING)
            var count2 =
                    jdbcTemplate.queryForObject(
                            "SELECT count(*) FROM crypto_prices", Integer.class);
            assertThat(count2).isEqualTo(count1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                Files.deleteIfExists(errorFile);
            } catch (Exception ignored) {
            }
        }
    }
}
