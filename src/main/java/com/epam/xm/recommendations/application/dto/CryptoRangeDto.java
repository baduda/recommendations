package com.epam.xm.recommendations.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Cryptocurrency volatility information")
public record CryptoRangeDto(
    @Schema(description = "Coin ticker", example = "BTC")
    String symbol,
    @Schema(description = "Normalized range (max-min)/min", example = "0.6667")
    BigDecimal normalizedRange
) {}
