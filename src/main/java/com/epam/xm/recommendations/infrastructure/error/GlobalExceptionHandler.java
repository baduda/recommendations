package com.epam.xm.recommendations.infrastructure.error;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/**
 * Centralized translation of exceptions into RFC 7807-style API errors.
 *
 * <p>Keeps controller code lean and ensures consistent error shape across endpoints.
 */
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CryptoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponse(
            responseCode = "404",
            description = "Crypto not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ApiError handleNotFound(CryptoNotFoundException ex, HttpServletRequest request) {
        LOGGER.warn("Resource not found: {} at path: {}", ex.getMessage(), request.getRequestURI());
        return new ApiError(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(UnsupportedCryptoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(
            responseCode = "400",
            description = "Unsupported crypto symbol",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ApiError handleUnsupported(UnsupportedCryptoException ex, HttpServletRequest request) {
        LOGGER.warn("Unsupported crypto: {} at path: {}", ex.getMessage(), request.getRequestURI());
        return new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(InvalidDataException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    @ApiResponse(
            responseCode = "422",
            description = "Invalid data provided",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ApiError handleInvalidData(InvalidDataException ex, HttpServletRequest request) {
        LOGGER.warn("Invalid data: {} at path: {}", ex.getMessage(), request.getRequestURI());
        return new ApiError(
                Instant.now(),
                422,
                "UNPROCESSABLE_ENTITY",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(
            responseCode = "400",
            description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ApiError handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.joining(", "));
        LOGGER.warn("Validation failed: {} at path: {}", message, request.getRequestURI());
        return new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                message,
                request.getRequestURI());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ApiError handleRateLimit(RateLimitExceededException ex, HttpServletRequest request) {
        LOGGER.warn("Rate limit exceeded for path: {}", request.getRequestURI());
        return new ApiError(
                Instant.now(),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "TOO_MANY_REQUESTS",
                ex.getMessage(),
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ApiError handleGeneralError(Exception ex, HttpServletRequest request) {
        LOGGER.error("Unexpected error occurred at path: {}", request.getRequestURI(), ex);
        return new ApiError(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request.getRequestURI());
    }
}
