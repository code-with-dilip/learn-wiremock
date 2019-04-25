package com.learnwiremock.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.apache.http.HttpHeaders;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWireMock(port = 0)
public class NameServiceSpringBootTest {

    @Autowired
    NameService nameService;

    @Autowired
    WireMockServer wireMockServer;

    String inputPayload = "{" +
            "  \"name\": \"Dilip\"}";

    @Before
    public void setUp(){
        final String baseUrl = String.format("http://localhost:%s", wireMockServer.port());
        ReflectionTestUtils.setField(nameService, "url",baseUrl );

        stubFor(WireMock.get("/getName")
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("Dilip")));

        stubFor(WireMock.post("/postName")
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
    public void postName(){


        String name = nameService.postName(inputPayload);
        Assert.assertEquals("Dilip", name);


    }
}
