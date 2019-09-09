package com.learnwiremock.service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.learnwiremock.dto.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(WireMockExtension.class)
public class MoviesRestClientFaultTest {

    MoviesRestClient moviesRestClient = null;
    WebClient webClient;

    @InjectServer
    WireMockServer wireMockServer;

    @ConfigureWireMock
    Options options = wireMockConfig()
            .port(8088)
            .notifier(new ConsoleNotifier(true))
            .extensions(new ResponseTemplateTransformer(true));

    @BeforeEach
    void setUp() {
        //int port = 8081;
        int port = wireMockServer.port();
        final String baseUrl = String.format("http://localhost:%s/", port);
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);

    }

    @Test
    void getAllMovies() {

        //given
        stubFor(get(WireMock.anyUrl())
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())
                        .withBodyFile("all-movies.json")));
        //whenx
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.println("movieList : " + movieList);

        //then
        assertTrue(!movieList.isEmpty());
    }
}
