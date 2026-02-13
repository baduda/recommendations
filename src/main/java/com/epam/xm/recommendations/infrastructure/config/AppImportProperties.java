package com.epam.xm.recommendations.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.import")
@Validated
public record AppImportProperties(@NotBlank String directory) {}
