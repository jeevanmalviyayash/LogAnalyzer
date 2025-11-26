package com.yash.log.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.reactive.server.WebTestClient;

@Configuration
public class config {

    @Bean
    public WebTestClient webTestClient(AIFixesController controller) {

        return WebTestClient.bindToController(controller).build();
    }

}
