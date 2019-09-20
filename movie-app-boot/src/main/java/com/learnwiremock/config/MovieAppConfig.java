package com.learnwiremock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MovieAppConfig {

    @Bean
    public WebClient webClient(){

        return WebClient.create("http://localhost:8088");
    }
}
