package com.learnwiremock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.retry.RetryExhaustedException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.learnwiremock.constants.WireMockConstants.USER_URL;
import static org.junit.Assert.assertEquals;

public class UserServiceExceptionsWireMockRuleTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private UserServiceExceptions userServiceExceptions;
    WebClient webClient;
    ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        final String baseUrl = String.format("http://localhost:%s", wireMockRule.port());
        webClient = WebClient.create();
        userServiceExceptions = new UserServiceExceptions(baseUrl, webClient);
    }

    @Test(expected = WebClientResponseException.class)
    public void getUserByNameExceptionHandling_WithFixedDelay_approach1() {


        //given
        String name = "scooby";
        stubFor(WireMock.get(urlPathEqualTo(USER_URL))
                .withQueryParam("name", equalTo(name))
                .willReturn(WireMock.serverError().withBody("Exception Occurred")));

        //when
        userServiceExceptions.getUserByNameExceptionHandling_WithFixedDelay_approach1(name);

    }

    @Test(expected = WebClientResponseException.class)
    public void getUserByNameExceptionHandling_WithExponentialDelay_approach2() {


        //given
        String name = "scooby";
        stubFor(WireMock.get(urlPathEqualTo(USER_URL))
                .withQueryParam("name", equalTo(name))
                .willReturn(WireMock.serverError().withBody("Exception Occurred")));

        //when
        userServiceExceptions.getUserByNameExceptionHandling_WithExponentialDelay_approach2(name);

    }

    @Test(expected = RuntimeException.class)
    public void getUserByNameExceptionHandling_WithExponentialDelay_approach3() {


        //given
        String name = "scooby";
        stubFor(WireMock.get(urlPathEqualTo(USER_URL))
                .withQueryParam("name", equalTo(name))
                .willReturn(WireMock.serverError().withBody("Exception Occurred")));

        //when
        userServiceExceptions.getUserByNameExceptionHandling_FixedDelay_approach3(name);
    }

    @Test(expected = RuntimeException.class)
    public void getUserByNameExceptionHandling_ExponentialDelay_approach4() {


        //given
        String name = "scooby";
        stubFor(WireMock.get(urlPathEqualTo(USER_URL))
                .withQueryParam("name", equalTo(name))
                .willReturn(WireMock.serverError().withBody("Exception Occurred")));

        //when
        userServiceExceptions.getUserByNameExceptionHandling_ExponentialDelay_approach4(name);
    }
}
