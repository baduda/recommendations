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
@Tag(name = "Crypto Stats API", description = "Endpoints for cryptocurrency analytics")
@Validated
public class CryptoController {

    private final CryptoApplicationService cryptoService;
    private final CryptoMapper cryptoMapper;

    public CryptoController(CryptoApplicationService cryptoService, CryptoMapper cryptoMapper) {
        this.cryptoService = cryptoService;
        this.cryptoMapper = cryptoMapper;
    }

    @Operation(
            summary = "Get statistics for a specific coin",
            description = "Returns oldest, newest, min, and max prices for the entire period",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful response"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or unsupported ticker", content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "404", description = "Coin data not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "429", description = "Rate limit exceeded", content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @GetMapping("/stats/{symbol}")
    public ResponseEntity<CryptoStatsDto> getStats(
            @Parameter(description = "Coin ticker (e.g., BTC)", example = "BTC")
            @PathVariable @Pattern(regexp = "^[A-Z]{3,10}$", message = "Symbol must be 3-10 uppercase letters") String symbol) {
        return ResponseEntity.ok(cryptoMapper.toDto(cryptoService.getStats(symbol)));
    }

    @Operation(
            summary = "Get all coins sorted by volatility",
            description = "Sorts by descending normalized range (max-min)/min",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful response"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "404", description = "Data not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "429", description = "Rate limit exceeded", content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @GetMapping("/sorted")
    public List<CryptoRangeDto> getSortedRange() {
        return cryptoService.getAllSortedStats().stream()
                .map(cryptoMapper::toRangeDto)
                .toList();
    }

    @Operation(
            summary = "Coin with the highest range for a specific day",
            description = "Returns a single coin with the highest normalized range for the specified date",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful response"),
                    @ApiResponse(responseCode = "400", description = "Invalid date format", content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "404", description = "No data for the specified date", content = @Content(schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "429", description = "Rate limit exceeded", content = @Content(schema = @Schema(implementation = ApiError.class)))
            }
    )
    @GetMapping("/highest-range")
    public ResponseEntity<CryptoRangeDto> getHighestRange(
            @Parameter(description = "Date in yyyy-MM-dd format", example = "2022-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return cryptoService.getHighestRangeForDate(date)
                .map(cryptoMapper::toRangeDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
