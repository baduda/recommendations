package com.epam.xm.recommendations.infrastructure.config;

import com.epam.xm.recommendations.domain.SetBasedSymbolValidator;
import com.epam.xm.recommendations.domain.SymbolValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableCaching
public class CryptoConfig {

    @Bean
    public SymbolValidator symbolValidator(@Value("${app.etl.directory}") String etlDirectory) throws IOException {
        Path rootPath = Path.of(etlDirectory);
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            // Fallback for tests or if directory is missing
            return new SetBasedSymbolValidator(Set.of("BTC", "ETH", "LTC", "XRP", "DOGE"));
        }

        try (Stream<Path> paths = Files.list(rootPath)) {
            Set<String> symbols = paths
                    .filter(path -> path.toString().endsWith("_values.csv"))
                    .map(path -> path.getFileName().toString().replace("_values.csv", ""))
                    .collect(Collectors.toSet());
            
            if (symbols.isEmpty()) {
                return new SetBasedSymbolValidator(Set.of("BTC", "ETH", "LTC", "XRP", "DOGE"));
            }
            
            return new SetBasedSymbolValidator(symbols);
        }
    }
}
