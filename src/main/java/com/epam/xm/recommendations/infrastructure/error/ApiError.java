package com.epam.xm.recommendations.infrastructure.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Standard error response format (RFC 7807)")
public record ApiError(
    @Schema(description = "Timestamp of the error", example = "2026-02-13T16:38:00Z")
    Instant timestamp,
    @Schema(description = "HTTP status code", example = "404")
    int status,
    @Schema(description = "Business error code", example = "NOT_FOUND")
    String code,
    @Schema(description = "Detailed error message", example = "Crypto symbol BTC not found")
    String message,
    @Schema(description = "Request path", example = "/api/v1/crypto/stats/BTC")
    String path
) {}
