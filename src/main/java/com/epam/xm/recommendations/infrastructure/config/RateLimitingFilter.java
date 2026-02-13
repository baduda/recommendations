package com.epam.xm.recommendations.infrastructure.config;

import com.epam.xm.recommendations.infrastructure.error.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
/**
 * Servlet filter enforcing per-IP rate limiting via Bucket4j.
 *
 * <p>The filter uses an in-memory map of token buckets keyed by client IP. On exhaustion, it
 * delegates to {@link org.springframework.web.servlet.HandlerExceptionResolver} to produce a
 * consistent RFC 7807 response.
 */
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final int capacity;
    private final int tokensPerMinute;

    @Autowired
    public RateLimitingFilter(
            @Qualifier("handlerExceptionResolver")
                    HandlerExceptionResolver handlerExceptionResolver,
            @Value("${app.rate-limit.capacity:10}") int capacity,
            @Value("${app.rate-limit.tokens-per-minute:10}") int tokensPerMinute) {
        super();
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.capacity = capacity;
        this.tokensPerMinute = tokensPerMinute;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        /**
         * Consumes one token per request. If the bucket is empty, a 429 error is returned.
         *
         * @param request incoming HTTP request
         * @param response HTTP response
         * @param filterChain remaining filter chain
         * @throws ServletException on servlet errors
         * @throws IOException on I/O errors
         */
        String ip = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(ip, this::newBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            LOGGER.warn("Rate limit exceeded for IP: {}", ip);
            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new RateLimitExceededException("Rate limit exceeded. Try again later."));
        }
    }

    private Bucket newBucket(String ip) {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(capacity)
                                .refillIntervally(tokensPerMinute, Duration.ofMinutes(1))
                                .build())
                .build();
    }
}
