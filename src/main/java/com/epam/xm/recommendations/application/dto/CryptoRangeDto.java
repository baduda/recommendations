package com.epam.xm.recommendations.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Информация о волатильности монеты")
public record CryptoRangeDto(
    @Schema(description = "Тикер монеты", example = "BTC")
    String symbol,
    @Schema(description = "Нормализованный диапазон (max-min)/min", example = "0.6667")
    BigDecimal normalizedRange
) {}
