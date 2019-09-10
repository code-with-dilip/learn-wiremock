package com.learnwiremock.service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.learnwiremock.constants.MovieAppConstants.ADD_MOVIE_V1;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(WireMockExtension.class)
public class MoviesRestClientFaultTest {

    MoviesRestClient moviesRestClient = null;
    WebClient webClient;

    @InjectServer
    WireMockServer wireMockServer;

    TcpClient tcpClient = TcpClient.create()
            .doOnConnected(connection ->
                    connection.addHandlerLast(new ReadTimeoutHandler(5))
                            .addHandlerLast(new WriteTimeoutHandler(5)));

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
/*        webClient =  WebClient.builder().clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl(baseUrl).build();*/
        moviesRestClient = new MoviesRestClient(webClient);

    }

    @Test
    void getAllMovies_internal_server_Error() {

        //given
        stubFor(get(WireMock.anyUrl())
                .willReturn(serverError()));
        //whenx
        /*List<Movie> movieList = moviesRestClient.retrieveAllMovies();*/

        //then
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveAllMovies());
    }

    @Test
    void getAllMovies_503_error() {

        //given
        stubFor(get(WireMock.anyUrl())
                .willReturn(serverError()
                        .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .withBody("Service Unavailable")));
        //whenx
        /*List<Movie> movieList = moviesRestClient.retrieveAllMovies();*/

        //then
        MovieErrorResponse errorResponse = assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveAllMovies());

        assertEquals("Service Unavailable", errorResponse.getMessage());
    }

    @Test
    void getAllMovies_fault_Response() {

        //given
        stubFor(get(WireMock.anyUrl())
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        //then
        MovieErrorResponse movieErrorResponse = assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveAllMovies());
        String expectedErrorMessage = "reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response";
        assertEquals(expectedErrorMessage, movieErrorResponse.getMessage());

    }

    @Test
    void getAllMovies_randomDataThenClose() {

        //given
        stubFor(get(WireMock.anyUrl())
                .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        //then
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveAllMovies());

    }

    @Test
    void getAllMovies_connection_reset_by_peer() {

        //given
        stubFor(get(WireMock.anyUrl())
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        //then
        assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveAllMovies());

    }

}
