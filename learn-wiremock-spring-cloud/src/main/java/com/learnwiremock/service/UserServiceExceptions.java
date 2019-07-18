package com.learnwiremock.service;

import com.learnwiremock.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;
import reactor.retry.RetryContext;
import reactor.retry.RetryExhaustedException;
import com.learnwiremock.config.WebClientRetryConfig;

import java.net.URI;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


import static com.learnwiremock.constants.WireMockConstants.USER_URL;

@Slf4j
public class UserServiceExceptions {

    private String url;
    private WebClient webClient;

    private Queue<RetryContext<?>> retries = new ConcurrentLinkedQueue<>();

    public UserServiceExceptions(String _url, WebClient _webClient) {
        this.url = _url;
        this.webClient = _webClient;
    }


    public User getUserByNameExceptionHandling_WithFixedDelay_approach1(String name) {

        int startIndex = 1;
        int endIndex = 2;

        URI uri = UriComponentsBuilder.fromUriString(url + USER_URL)
                .queryParam("name", name)
                .buildAndExpand()
                .toUri();
        log.info("uri : " + uri);
        return webClient.get().uri(uri)
                .retrieve()
                .bodyToMono(User.class)
                .retryWhen(companion ->
                        companion
                                .doOnNext(s -> log.info("Exception is : " + s + " at : " + LocalTime.now()))
                                .filter(s -> ((WebClientResponseException) s).getStatusCode().is5xxServerError())
                                .zipWith(Flux.range(startIndex, endIndex), (error, index) -> {
                                    log.info("Error in Zip With : " + error);
                                    if (index < endIndex) return index;  // is retry limit exceeded
                                    else throw Exceptions.propagate(error); // This just propagates the error
                                })
                                .flatMap(index -> Mono.delay(Duration.ofMillis(3000))) // This adds the fixed delay between the calls.
                                .doOnNext(s -> log.info("retried at " + LocalTime.now())))
                .block();
    }

    public User getUserByNameExceptionHandling_WithExponentialDelay_approach2(String name) {

        int startIndex = 1;
        int endIndex = 4;

        URI uri = UriComponentsBuilder.fromUriString(url + USER_URL)
                .queryParam("name", name)
                .buildAndExpand()
                .toUri();
        log.info("uri : " + uri);
        return webClient.get().uri(uri)
                .retrieve()
                .bodyToMono(User.class)
                .retryWhen(companion ->
                        companion
                                .filter(s -> ((WebClientResponseException) s).getStatusCode().is5xxServerError())
                                .zipWith(Flux.range(startIndex, endIndex), (error, index) -> {
                                    if (index < endIndex) return index;  // is retry limit exceeded
                                    else throw Exceptions.propagate(error); // This just propagates the error
                                })
                                .flatMap(index -> Mono.delay(Duration.ofMillis(index * 1000)))) // This adds the exponential delay between the calls.
                .block();
    }


    public User getUserByNameExceptionHandling_FixedDelay_approach3(String name) {

        URI uri = UriComponentsBuilder.fromUriString(url + USER_URL)
                .queryParam("name", name)
                .buildAndExpand()
                .toUri();

        try {
            return webClient.get().uri(uri)
                    .retrieve()
                    .bodyToMono(User.class)
                    .retryWhen(WebClientRetryConfig.fixedRetry)
                    .block();
        } catch (RetryExhaustedException e) {
            log.error("RetryExhaustedException is : " + e);
            throw new RuntimeException(e);

        }

    }

    public User getUserByNameExceptionHandling_ExponentialDelay_approach4(String name) {


        URI uri = UriComponentsBuilder.fromUriString(url + USER_URL)
                .queryParam("name", name)
                .buildAndExpand()
                .toUri();

        try {
            return webClient.get().uri(uri)
                    .retrieve()
                    .bodyToMono(User.class)
                    .doOnError((e) -> {
                        log.error("Exception in the doOnError : " + e);
                    })
                    .retryWhen(WebClientRetryConfig.exponentialRetry)
                    .block();
        } catch (RetryExhaustedException e) {
            log.error("RetryExhaustedException is : " + e);
            throw new RuntimeException(e);

        }

    }
}
