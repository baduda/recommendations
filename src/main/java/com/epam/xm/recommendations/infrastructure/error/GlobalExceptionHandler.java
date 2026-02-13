package com.epam.xm.recommendations.infrastructure.error;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CryptoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponse(responseCode = "404", description = "Crypto not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ApiError handleNotFound(CryptoNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {} at path: {}", ex.getMessage(), request.getRequestURI());
        return new ApiError(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(UnsupportedCryptoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "400", description = "Unsupported crypto symbol",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ApiError handleUnsupported(UnsupportedCryptoException ex, HttpServletRequest request) {
        log.warn("Unsupported crypto: {} at path: {}", ex.getMessage(), request.getRequestURI());
        return new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidDataException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    @ApiResponse(responseCode = "422", description = "Invalid data provided",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ApiError handleInvalidData(InvalidDataException ex, HttpServletRequest request) {
        log.warn("Invalid data: {} at path: {}", ex.getMessage(), request.getRequestURI());
        return new ApiError(
                Instant.now(),
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "UNPROCESSABLE_ENTITY",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ApiError handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {} at path: {}", message, request.getRequestURI());
        return new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                message,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ApiError handleGeneralError(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred at path: {}", request.getRequestURI(), ex);
        return new ApiError(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request.getRequestURI()
        );
    }
}
