package com.epam.xm.recommendations.domain;

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
        csvImportService = new CsvImportService(jdbcTemplate, tempDir.toString(), 100);
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
        csvImportService = new CsvImportService(jdbcTemplate, "/non-existent-path", 100);
        csvImportService.importCsvFiles();
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void shouldHandleNotDirectory() throws IOException {
        Path file = tempDir.resolve("not-a-dir.txt");
        Files.createFile(file);
        csvImportService = new CsvImportService(jdbcTemplate, file.toString(), 100);
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
