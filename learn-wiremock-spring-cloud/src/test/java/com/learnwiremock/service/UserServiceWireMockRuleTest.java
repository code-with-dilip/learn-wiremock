package com.learnwiremock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.learnwiremock.domain.User;
import com.learnwiremock.helper.TestHelper;
import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import wiremock.net.minidev.json.writer.JsonReader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.learnwiremock.constants.WireMockConstants.ALL_USERS_URL;
import static com.learnwiremock.constants.WireMockConstants.USER_ID_PATH_PARAM;
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

        stubFor(any(anyUrl()).willReturn(aResponse().proxiedFrom("http://localhost:8081")));
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

    //TODO - Multiple Path Params

    //TODO - Request Params

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
    @Ignore
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

    @Test
    public void addUser_WithMultipleMappings_DuplicateCheck() throws IOException {

        //Given
        stubFor(WireMock.post(urlPathEqualTo(USER_URL))
                .withRequestBody(equalTo(TestHelper.readFromPath("user_request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response.json"))));

        stubFor(WireMock.get(urlPathEqualTo(USER_URL+"/123"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response.json"))));

        User user = objectMapper.readValue(TestHelper.readFromPath("user_request_withid.json"),User.class);

        //When
        User addedUser = userService.addUser(user);
        assertEquals(12345,addedUser.getId().intValue());
    }

    @Test
    public void addUser_WithDynamicValues() throws IOException {

        //Given
        stubFor(WireMock.post(urlPathEqualTo(USER_URL))
                .withRequestBody(matchingJsonPath("id", equalTo(null)))
                .withRequestBody(matchingJsonPath("name",equalTo("dilip")))
                .withRequestBody(matchingJsonPath("age",equalTo("32")))
                .withRequestBody(matchingJsonPath("uniqueId"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response.json"))));

        stubFor(WireMock.get(urlMatching(USER_URL+"/.*"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response.json"))));

        User user = objectMapper.readValue(TestHelper.readFromPath("user_request.json"),User.class);

        //When
        User addedUser = userService.addUser(user);
        assertEquals(12345,addedUser.getId().intValue());
    }

    @Test
    public void getUserById(){

        //given
        stubFor(WireMock.get(urlMatching(USER_URL+"/.*"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response.json"))));

        //when
        User user = userService.getUserById(123);
        User user1 = userService.getUserById(456);
        //then
        assertEquals(12345,user.getId().intValue());
        assertEquals(12345,user1.getId().intValue());
    }

    @Test
    public void getUserById_with_priority(){

        //given
        stubFor(WireMock.get(urlMatching(USER_URL+"/12345"))
                .atPriority(1)
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response-1.json"))));

        stubFor(WireMock.get(urlMatching(USER_URL+"/.*"))
                .atPriority(2)
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response.json"))));

        //when
        User user = userService.getUserById(12345);
        User user1 = userService.getUserById(34234);
        //then
        assertEquals(45678,user.getId().intValue());
        assertEquals(12345,user1.getId().intValue());
    }


    @Test
    public void deleteUser_with_verification(){

        //given
        stubFor(delete(urlMatching(USER_URL+"/.*"))
                .willReturn(ok())
        );

        //when
        userService.deleteUser(123);

        //then
        verify(deleteRequestedFor(urlMatching(USER_URL+"/.*")));


    }

    @Test
    public void deleteMultipleUser_with_verification(){

        //given
        stubFor(delete(urlMatching(USER_URL+"/.*"))
                .willReturn(ok())
        );

        //when
        userService.deleteUsers(Arrays.asList(1,2));

        //then
        verify(exactly(2), deleteRequestedFor(urlMatching(USER_URL+"/.*")));


    }

    @Test
    public void requestJournal(){

        //given
        stubFor(WireMock.get(urlMatching(USER_URL+"/12345"))
                .atPriority(1)
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response-1.json"))));

        stubFor(WireMock.get(urlMatching(USER_URL+"/.*"))
                .atPriority(2)
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_response.json"))));

        //when
        User user = userService.getUserById(12345);
        User user1 = userService.getUserById(34234);

        final List<ServeEvent> allServeEvents = WireMock.getAllServeEvents();

        //then
        assertEquals(45678,user.getId().intValue());
        assertEquals(12345,user1.getId().intValue());
    }


    @Test
    public void updateUser() throws IOException {

        stubFor(WireMock.put(urlMatching(USER_URL+"/.*"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(TestHelper.readFromPath("user_update_response.json"))));


        User user = objectMapper.readValue(TestHelper.readFromPath("user_update_request.json"),User.class);
        User updatedUser = userService.updateUser(user);
        assertEquals(33,updatedUser.getAge().intValue());

    }

    @Test(expected = WebClientResponseException.class)
    public void updateUser_faultSimulation_serverError() throws IOException {

        stubFor(WireMock.put(urlMatching(USER_URL+"/.*"))
                .willReturn(serverError())); // returns server error with no body.


        User user = objectMapper.readValue(TestHelper.readFromPath("user_update_request.json"),User.class);
        User updatedUser = userService.updateUser(user);

    }

    @Test(expected = Exception.class)
    public void updateUser_withFault() throws IOException {

        stubFor(WireMock.put(urlMatching(USER_URL+"/.*"))
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE))); // returns server error with no body.


        User user = objectMapper.readValue(TestHelper.readFromPath("user_update_request.json"),User.class);
        User updatedUser = userService.updateUser(user);

    }

    @Test(expected = Exception.class)
    public void updateUser_withFault_connectionreset() throws IOException {

        stubFor(WireMock.put(urlMatching(USER_URL+"/.*"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))); // returns server error with no body.


        User user = objectMapper.readValue(TestHelper.readFromPath("user_update_request.json"),User.class);
        User updatedUser = userService.updateUser(user);

    }


    @Test(expected = WebClientResponseException.class)
    public void updateUser_faultSimulation_badRequest() throws IOException {

        stubFor(WireMock.put(urlMatching(USER_URL+"/.*"))
                .willReturn(badRequest()));


        User user = objectMapper.readValue(TestHelper.readFromPath("user_update_request.json"),User.class);
        User updatedUser = userService.updateUser(user);

    }



}
