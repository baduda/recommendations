package com.epam.xm.recommendations.infrastructure.config;

import com.epam.xm.recommendations.domain.SetBasedSymbolValidator;
import com.epam.xm.recommendations.domain.SymbolValidator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Infrastructure configuration providing beans for symbol validation and caching.
 *
 * <p>The {@code symbolValidator} bean auto-discovers supported symbols from the ETL directory to
 * ensure the API accepts only those with available data. Falls back to a sensible set in tests.
 */
@Configuration
@EnableCaching
public class CryptoConfig {

    @Bean
    public SymbolValidator symbolValidator(AppImportProperties importProperties)
            throws IOException {
        /*
         * Builds a {@link SymbolValidator} by scanning available CSV files.
         *
         * @param importProperties import configuration
         * @return validator backed by discovered symbols
         * @throws IOException when the directory cannot be accessed
         */
        Path rootPath = Path.of(importProperties.directory());
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            // Fallback for tests or if directory is missing
            return new SetBasedSymbolValidator(Set.of("BTC", "ETH", "LTC", "XRP", "DOGE"));
        }

        try (Stream<Path> paths = Files.list(rootPath)) {
            Set<String> symbols =
                    paths.filter(path -> path.toString().endsWith("_values.csv"))
                            .map(path -> path.getFileName().toString().replace("_values.csv", ""))
                            .collect(Collectors.toSet());

            if (symbols.isEmpty()) {
                return new SetBasedSymbolValidator(Set.of("BTC", "ETH", "LTC", "XRP", "DOGE"));
            }

            return new SetBasedSymbolValidator(symbols);
        }
    }
}
