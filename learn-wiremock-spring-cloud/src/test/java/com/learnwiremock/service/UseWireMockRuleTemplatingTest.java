package com.learnwiremock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.learnwiremock.domain.User;
import com.learnwiremock.helper.TestHelper;
import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.learnwiremock.constants.WireMockConstants.USER_URL;
import static org.junit.Assert.assertEquals;

public class UseWireMockRuleTemplatingTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private UserService userService;
    WebClient webClient;
    ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        final String baseUrl = String.format("http://localhost:%s", wireMockRule.port());
        webClient = WebClient.create();
        userService = new UserService(baseUrl, webClient);
    }

    @Test
    public void getUserByRequestParam(){

        //given
        stubFor(WireMock.get(urlPathEqualTo(USER_URL))
                .withQueryParam("name", equalTo("dilip"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response.json"))));

        //when
        String name="dilip";
        User user = userService.getUserByName(name);

        //then
        assertEquals(name, user.getName());
    }

    @Test
    public void getUserByRequestParam_DifferentValue(){


        //given
        String name="scooby";
        stubFor(WireMock.get(urlPathEqualTo(USER_URL))
                .withQueryParam("name", equalTo(name))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response-scooby.json"))));

        //when

        User user = userService.getUserByName(name);

        //then
        assertEquals(name, user.getName());
    }
}
