package com.epam.xm.recommendations.domain;

import com.epam.xm.recommendations.infrastructure.config.AppImportProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CsvImportServiceTest {

    private JdbcTemplate jdbcTemplate;
    private CsvImportService csvImportService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        AppImportProperties props = new AppImportProperties(tempDir.toString());
        csvImportService = new CsvImportService(jdbcTemplate, props, 100);
    }

    @Test
    void shouldImportCsvFiles() throws IOException {
        // Given
        Path csvFile = tempDir.resolve("BTC_values.csv");
        Files.writeString(csvFile, "timestamp,symbol,price\n1641009600000,BTC,46813.21\n1641013200000,BTC,46979.61");
        
        when(jdbcTemplate.batchUpdate(anyString(), anyList())).thenReturn(new int[]{1, 1});

        // When
        csvImportService.importCsvFiles();

        // Then
        // Wait a bit because it uses virtual threads
        verify(jdbcTemplate, timeout(2000).atLeastOnce()).batchUpdate(anyString(), anyList());
    }

    @Test
    void shouldHandleMissingDirectory() {
        AppImportProperties props = new AppImportProperties("/non-existent-path");
        csvImportService = new CsvImportService(jdbcTemplate, props, 100);
        csvImportService.importCsvFiles();
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void shouldHandleNotDirectory() throws IOException {
        Path file = tempDir.resolve("not-a-dir.txt");
        Files.createFile(file);
        AppImportProperties props = new AppImportProperties(file.toString());
        csvImportService = new CsvImportService(jdbcTemplate, props, 100);
        csvImportService.importCsvFiles();
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void shouldHandleEmptyBatch() throws IOException {
        // Given
        Path csvFile = tempDir.resolve("empty_batch.csv");
        Files.writeString(csvFile, "timestamp,symbol,price\n1641009600000,BTC,46813.21");
        
        when(jdbcTemplate.batchUpdate(anyString(), anyList())).thenReturn(new int[]{1});

        // When
        csvImportService.importCsvFiles();

        // Then
        verify(jdbcTemplate, timeout(2000).atLeastOnce()).batchUpdate(anyString(), anyList());
    }

    @Test
    void shouldHandleJdbcNoInfo() throws IOException {
        // Given
        Path csvFile = tempDir.resolve("no_info.csv");
        Files.writeString(csvFile, "timestamp,symbol,price\n1641009600000,BTC,46813.21");
        
        // -2 is SUCCESS_NO_INFO
        when(jdbcTemplate.batchUpdate(anyString(), anyList())).thenReturn(new int[]{-2});

        // When
        csvImportService.importCsvFiles();

        // Then
        verify(jdbcTemplate, timeout(2000).atLeastOnce()).batchUpdate(anyString(), anyList());
    }

    @Test
    void shouldSkipDamagedRows() throws IOException {
        // Given
        Path csvFile = tempDir.resolve("damaged.csv");
        Files.writeString(csvFile, "timestamp,symbol,price\nINVALID,BTC,46813.21\n1641013200000,BTC,46979.61");
        
        when(jdbcTemplate.batchUpdate(anyString(), anyList())).thenReturn(new int[]{1});

        // When
        csvImportService.importCsvFiles();

        // Then
        verify(jdbcTemplate, timeout(2000).atLeastOnce()).batchUpdate(anyString(), anyList());
    }
}
