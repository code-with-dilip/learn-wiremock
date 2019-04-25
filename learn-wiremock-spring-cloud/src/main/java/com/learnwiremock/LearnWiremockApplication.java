package com.learnwiremock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class LearnWiremockApplication {

	@Bean
	public WebClient webclient(){
		return WebClient.create();
	}

	public static void main(String[] args) {
		SpringApplication.run(LearnWiremockApplication.class, args);
	}

}
