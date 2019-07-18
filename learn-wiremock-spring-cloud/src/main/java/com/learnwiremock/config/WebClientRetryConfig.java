package com.learnwiremock.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.retry.Retry;

import java.time.Duration;

@Slf4j
public class WebClientRetryConfig {

    static long intialBackOff = 3;
    static long maxBackOff = 15;


    public static Retry<?> fixedRetry = Retry.anyOf(WebClientResponseException .class)
            .fixedBackoff(Duration.ofSeconds(1))
            .retryMax(3)
                            .doOnRetry((exception) -> {
        log.info("The exception is : " + exception);

    });

    public static Retry<?> exponentialRetry = Retry.anyOf(WebClientResponseException.class)
            .exponentialBackoff(Duration.ofSeconds(intialBackOff), Duration.ofSeconds(maxBackOff))
            .retryMax(3)
            .doOnRetry((exception) -> {
                log.error("The exception is : " + exception);
            });
}
