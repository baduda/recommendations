package com.epam.xm.recommendations.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Статистика по криптовалюте за весь период")
public record CryptoStatsDto(
    @Schema(description = "Тикер монеты", example = "BTC")
    String symbol,
    @Schema(description = "Самая старая цена", example = "35000.00")
    BigDecimal oldestPrice,
    @Schema(description = "Самая новая цена", example = "45000.00")
    BigDecimal newestPrice,
    @Schema(description = "Минимальная цена", example = "30000.00")
    BigDecimal minPrice,
    @Schema(description = "Максимальная цена", example = "50000.00")
    BigDecimal maxPrice,
    @Schema(description = "Нормализованный диапазон (max-min)/min", example = "0.6667")
    BigDecimal normalizedRange
) {}
