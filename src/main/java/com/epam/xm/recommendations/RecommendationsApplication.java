package com.epam.xm.recommendations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RecommendationsApplication {

    static void main(String[] args) {
        SpringApplication.run(RecommendationsApplication.class, args);
    }
}
