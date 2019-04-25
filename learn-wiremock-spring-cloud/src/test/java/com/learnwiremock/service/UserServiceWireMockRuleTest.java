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
import wiremock.net.minidev.json.writer.JsonReader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.learnwiremock.constants.WireMockConstants.ALL_USERS_URL;
import static com.learnwiremock.constants.WireMockConstants.USER_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserServiceWireMockRuleTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private UserService userService;
    WebClient webClient;
    ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp()  {
        final String baseUrl = String.format("http://localhost:%s", wireMockRule.port());
        webClient= WebClient.create();
        userService = new UserService(baseUrl, webClient);
        wireMockStubs();
    }

    private void wireMockStubs() {
    }


    @Test
    public void getUsers(){

        //Given
        stubFor(WireMock.get(urlPathEqualTo(ALL_USERS_URL))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("multiple_users_response.json"))));

        //When
        List<User> userList = userService.getUsers();

        //then
        assertEquals(3, userList.size());
    }

    @Test
    public void addUser() throws IOException {

        //Given
        stubFor(WireMock.post(urlPathEqualTo(USER_URL))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response.json"))));
        User user = objectMapper.readValue(TestHelper.readFromPath("user_request.json"),User.class);

        //When
        User addedUser = userService.addUser(user);

        //Then
        assertEquals(12345,addedUser.getId().intValue());
    }

    @Test
    public void addUser_withMatchingBody() throws IOException {

        //Given
        stubFor(WireMock.post(urlPathEqualTo(USER_URL))
                .withRequestBody(equalTo(TestHelper.readFromPath("user_request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response.json"))));
        User user = objectMapper.readValue(TestHelper.readFromPath("user_request.json"),User.class);

        //When
        User addedUser = userService.addUser(user);

        //Then
        assertEquals(12345,addedUser.getId().intValue());
    }



}
