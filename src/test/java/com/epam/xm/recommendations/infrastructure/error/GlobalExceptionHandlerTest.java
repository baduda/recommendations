package com.epam.xm.recommendations.infrastructure.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/test-path");
    }

    @Test
    void handleNotFound() {
        CryptoNotFoundException ex = new CryptoNotFoundException("Not found");
        ApiError error = handler.handleNotFound(ex, request);
        assertEquals(HttpStatus.NOT_FOUND.value(), error.status());
        assertEquals("Not found", error.message());

        // Cover the constructor with cause to satisfy JaCoCo
        CryptoNotFoundException exWithCause =
                new CryptoNotFoundException("Not found", new RuntimeException());
        assertEquals("Not found", exWithCause.getMessage());
    }

    @Test
    void handleUnsupported() {
        UnsupportedCryptoException ex = new UnsupportedCryptoException("Unsupported");
        ApiError error = handler.handleUnsupported(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.status());
        assertEquals("Unsupported", error.message());

        // Cover the constructor with cause to satisfy JaCoCo
        UnsupportedCryptoException exWithCause =
                new UnsupportedCryptoException("Unsupported", new RuntimeException());
        assertEquals("Unsupported", exWithCause.getMessage());
    }

    @Test
    void handleInvalidData() {
        InvalidDataException ex = new InvalidDataException("Invalid");
        ApiError error = handler.handleInvalidData(ex, request);
        assertEquals(422, error.status());
        assertEquals("Invalid", error.message());

        // Cover the constructor with cause to satisfy JaCoCo
        InvalidDataException exWithCause =
                new InvalidDataException("Invalid", new RuntimeException());
        assertEquals("Invalid", exWithCause.getMessage());
    }

    @Test
    void handleRateLimit() {
        RateLimitExceededException ex = new RateLimitExceededException("Limit");
        ApiError error = handler.handleRateLimit(ex, request);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), error.status());
        assertEquals("Limit", error.message());

        // Cover the constructor with cause to satisfy JaCoCo
        RateLimitExceededException exWithCause =
                new RateLimitExceededException("Limit", new RuntimeException());
        assertEquals("Limit", exWithCause.getMessage());
    }

    @Test
    void handleGeneralError() {
        Exception ex = new Exception("General");
        ApiError error = handler.handleGeneralError(ex, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), error.status());
        assertEquals("An unexpected error occurred", error.message());
    }

    @Test
    void handleValidationErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError =
                new FieldError("obj", "field", "rejected", false, null, null, "default message");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ApiError error = handler.handleValidationErrors(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.status());
        assertEquals("field: default message", error.message());
    }
}
