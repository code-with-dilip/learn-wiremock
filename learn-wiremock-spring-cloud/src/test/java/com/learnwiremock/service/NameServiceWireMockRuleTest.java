package com.learnwiremock.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.apache.http.HttpHeaders;
import org.junit.*;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

public class NameServiceWireMockRuleTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private NameService nameService;

    WebClient webClient;

    String inputPayload = "{" +
            "  \"name\": \"Dilip\"}";
    String expectedPayLoad = "{" +
            "  \"name\": \"Dilip\"}";


    @Before
    public void setUp(){
        final String baseUrl = String.format("http://localhost:%s", wireMockRule.port());
        System.out.println("baseUrl : " + baseUrl);
        webClient= WebClient.create();
        nameService = new NameService(baseUrl,webClient);

        stubFor(WireMock.get(urlPathEqualTo("/getName"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("Dilip")));

        stubFor(WireMock.get(urlPathEqualTo("/getName"))
                .withQueryParam("name",equalTo("Dilip1"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("Dilip")));


        stubFor(WireMock.post("/postName")
                .withRequestBody(equalToJson(expectedPayLoad))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("Dilip")));
    }

    @Test
    public void getName(){

        String name = nameService.getName();
        Assert.assertEquals("Dilip", name);
    }

    @Test
    public void getNameWithRequestParam(){

        String name = nameService.getNameWithRequestParam("Dilip");
        Assert.assertEquals("Dilip", name);
    }

    @Test
    public void getNameWithRequestParam_does_not_match(){

        String name = nameService.getNameWithRequestParam("Dilip2"); // not a name match
        Assert.assertEquals("Dilip", name);
    }

    @Test
    public void postName(){

        String name = nameService.postName(inputPayload);
        Assert.assertEquals("Dilip", name);


    }

}
