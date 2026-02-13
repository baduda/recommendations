package com.epam.xm.recommendations.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "app.import")
@Validated
public record AppImportProperties(
    @NotBlank
    String directory
) {}
