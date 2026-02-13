package com.epam.xm.recommendations.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RateLimitingTest {

    @Autowired
    private org.springframework.web.context.WebApplicationContext context;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        this.mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new com.epam.xm.recommendations.infrastructure.config.RateLimitingFilter(
                        context.getBean("handlerExceptionResolver", org.springframework.web.servlet.HandlerExceptionResolver.class),
                        10, 10
                ))
                .build();
    }

    @Test
    void shouldReturn429WhenLimitExceeded() throws Exception {
        // First 10 requests should succeed (assuming they are supported symbols or at least return some response)
        // Note: we don't mock anything, so it might return 400 or 404, which is still 2xx/4xx and consumed by rate limiter.
        
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/v1/crypto/stats/BTC"))
                    .andExpect(status().is(org.hamcrest.Matchers.oneOf(200, 404, 400)));
        }

        mockMvc.perform(get("/api/v1/crypto/stats/BTC"))
                .andExpect(status().isTooManyRequests());
    }
}
