package com.epam.xm.recommendations;

import org.springframework.boot.SpringApplication;

public class TestRecommendationsApplication {

    public static void main(String[] args) {
        SpringApplication.from(RecommendationsApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
