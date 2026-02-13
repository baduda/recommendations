package com.epam.xm.recommendations.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Cryptocurrency statistics for the entire period")
public record CryptoStatsDto(
    @Schema(description = "Coin ticker", example = "BTC")
    String symbol,
    @Schema(description = "Oldest price", example = "35000.00")
    BigDecimal oldestPrice,
    @Schema(description = "Newest price", example = "45000.00")
    BigDecimal newestPrice,
    @Schema(description = "Minimum price", example = "30000.00")
    BigDecimal minPrice,
    @Schema(description = "Maximum price", example = "50000.00")
    BigDecimal maxPrice,
    @Schema(description = "Normalized range (max-min)/min", example = "0.6667")
    BigDecimal normalizedRange
) {}
