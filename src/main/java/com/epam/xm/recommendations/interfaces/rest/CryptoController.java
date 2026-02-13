package com.epam.xm.recommendations.interfaces.rest;

import com.epam.xm.recommendations.application.dto.CryptoRangeDto;
import com.epam.xm.recommendations.application.dto.CryptoStatsDto;
import com.epam.xm.recommendations.application.mapper.CryptoMapper;
import com.epam.xm.recommendations.application.service.CryptoApplicationService;
import com.epam.xm.recommendations.infrastructure.error.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/crypto")
@Tag(name = "Crypto Stats API", description = "Эндпоинты для получения аналитики по криптовалютам")
@Validated
public class CryptoController {

    private final CryptoApplicationService cryptoService;
    private final CryptoMapper cryptoMapper;

    public CryptoController(CryptoApplicationService cryptoService, CryptoMapper cryptoMapper) {
        this.cryptoService = cryptoService;
        this.cryptoMapper = cryptoMapper;
    }

    @Operation(
            summary = "Получить статистику по конкретной монете",
            description = "Возвращает oldest, newest, min и max цены за весь период",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "404", description = "Данные по монете не найдены", content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "400", description = "Тикер не поддерживается", content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @GetMapping("/stats/{symbol}")
    public ResponseEntity<CryptoStatsDto> getStats(
            @Parameter(description = "Тикер монеты (напр. BTC)", example = "BTC")
            @PathVariable @Pattern(regexp = "^[A-Z]{3,10}$", message = "Symbol must be 3-10 uppercase letters") String symbol) {
        return ResponseEntity.ok(cryptoMapper.toDto(cryptoService.getStats(symbol)));
    }

    @Operation(
            summary = "Получить список всех монет, отсортированный по волатильности",
            description = "Сортировка по убыванию normalized range (max-min)/min",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ")
            }
    )
    @GetMapping("/sorted")
    public List<CryptoRangeDto> getSortedRange() {
        return cryptoService.getAllSortedStats().stream()
                .map(cryptoMapper::toRangeDto)
                .toList();
    }

    @Operation(
            summary = "Монета с самым высоким range за день",
            description = "Возвращает одну монету с самым высоким normalized range за указанные сутки",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный ответ"),
                    @ApiResponse(responseCode = "404", description = "Нет данных за указанную дату", content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @GetMapping("/highest-range")
    public ResponseEntity<CryptoRangeDto> getHighestRange(
            @Parameter(description = "Дата в формате yyyy-MM-dd", example = "2022-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return cryptoService.getHighestRangeForDate(date)
                .map(cryptoMapper::toRangeDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
