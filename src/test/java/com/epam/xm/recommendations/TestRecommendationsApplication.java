package com.epam.xm.recommendations;

import org.springframework.boot.SpringApplication;

class TestRecommendationsApplication {

    static void main(String[] args) {
        SpringApplication.from(RecommendationsApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
