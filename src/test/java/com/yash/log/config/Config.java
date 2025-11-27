package com.yash.log.config;

import com.yash.log.controller.AIFixesController;
import com.yash.log.controller.UserController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
@Configuration
public class Config {
    @Bean
    public WebTestClient webTestClient(UserController controller) {

        return WebTestClient.bindToController(controller).build();
    }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
