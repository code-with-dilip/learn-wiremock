package com.learnwiremock.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.learnwiremock.constants.MovieAppConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(WireMockExtension.class)
class MoviesRestClientTest {

    MoviesRestClient moviesRestClient = null;
    WebClient webClient;

    @InjectServer
    WireMockServer wireMockServer;

    ObjectMapper objectMapper = new ObjectMapper();

    @ConfigureWireMock
    Options options = wireMockConfig()
            .port(8088)
            .notifier(new ConsoleNotifier(true));

    @BeforeEach
    void setUp() {
        //int port = 8081;
        int port = options.portNumber();
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
                        .withBodyFile("all-movies.json")));
        //when
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.println("movieList : " + movieList);

        //then
        assertTrue(!movieList.isEmpty());
    }

    @Test
    void getAllMovies_matchUrl() {

        //given
        stubFor(get(urlEqualTo(GET_ALL_MOVIES_V1))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("all-movies.json")));

        //when
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.println("movieList : " + movieList);

        //then
        assertTrue(!movieList.isEmpty());
    }


    @Test
    void retrieveMovieById() {

        //given
        stubFor(get(urlMatching("/movieservice/v1/movie/([0-9])"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movie.json")));

        //given
        Integer movieId = 1;

        //when
        Movie movie = moviesRestClient.retrieveMovieById(movieId);

        //then
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    void retrieveMovieById_NotFound() {
        //given
        Integer movieId = 100;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieById(movieId));

    }

    @Test
    void retrieveMovieByName_UrlEqualTo() {
        //given
        String movieName = "Avengers";
        stubFor(get(urlEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("avengers.json")));

        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(movieName);

        //then
        String expectedCastName = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(expectedCastName, movieList.get(0).getCast());
    }

    @Test
    void retrieveMovieByName() {
        //given
        String movieName = "Avengers";
        stubFor(get(urlPathEqualTo(MOVIE_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("avengers.json")));

        //when
        List<Movie> movieList = moviesRestClient.retrieveMovieByName(movieName);

        //then
        String expectedCastName = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(4, movieList.size());
        assertEquals(expectedCastName, movieList.get(0).getCast());
    }


    @Test
    void retrieveMovieByName_Not_Found() {
        //given
        String movieName = "ABC";

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieByName(movieName));
    }


    @Test
    void retrieveMovieByYear() {
        //given
        Integer year = 2012;
        stubFor(get(urlPathEqualTo(MOVIE_BY_YEAR_QUERY_PARAM_V1))
                .withQueryParam("year", equalTo(year.toString()))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("2012.json")));

        //when
        List<Movie> movieList = moviesRestClient.retreieveMovieByYear(year);

        //then
        assertEquals(2, movieList.size());

    }

    @Test
    void retrieveMovieByYear_Not_Found() {
        //given
        Integer year = 1950;
        stubFor(get(urlPathEqualTo(MOVIE_BY_YEAR_QUERY_PARAM_V1))
                .withQueryParam("year", equalTo(year.toString()))
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));


        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retreieveMovieByYear(year));

    }

    @Test
    void addNewMovie() throws JsonProcessingException {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, batmanBeginsCrew, LocalDate.of(2019, 06, 20));
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 4")))
                .willReturn(WireMock.aResponse()
                        .withStatus(201)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("2012.json")));


        //when
        Movie movie = moviesRestClient.addNewMovie(toyStory);

        //then
        assertTrue(movie.getMovie_id() != null);

    }

    @Test
    @DisplayName("Passing the Movie name and year as Null")
    void addNewMovie_InvlaidInput() {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, null, null, batmanBeginsCrew, LocalDate.of(2019, 06, 20));

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addNewMovie(toyStory));

    }

    @Test
    void updateMovie() {
        //given
        String darkNightRisesCrew = "Tom Hardy";
        Movie darkNightRises = new Movie(null, null, null, darkNightRisesCrew, null);
        Integer movieId = 3;
        stubFor(put(urlPathMatching("/movieservice/v1/movie/([0-9])"))
                .withRequestBody(matchingJsonPath("$.cast", equalTo(darkNightRisesCrew)))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("updatemovie.json")));


        //when
        Movie updatedMovie = moviesRestClient.updateMovie(movieId, darkNightRises);

        //then
        String updatedCastName = "Christian Bale, Heath Ledger , Michael Caine, Tom Hardy";
        assertTrue(updatedMovie.getCast().contains(darkNightRisesCrew));


    }

    @Test
    void updateMovie_Not_Found() {
        //given
        String darkNightRisesCrew = "Tom Hardy";
        Movie darkNightRises = new Movie(null, null, null, darkNightRisesCrew, null);
        Integer movieId = 100;
        stubFor(put(urlPathMatching("/movieservice/v1/movie/([0-9][0-9][0-9])"))
                .withRequestBody(matchingJsonPath("$.cast", equalTo(darkNightRisesCrew)))
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.updateMovie(movieId, darkNightRises));
    }

    @Test
    void deleteMovie() {

        //given
        stubFor(post(urlPathEqualTo(ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("Toy Story 4")))
                .willReturn(WireMock.aResponse()
                        .withStatus(201)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("toystory.json")));

        stubFor(delete(urlPathMatching("/movieservice/v1/movie/([0-9][0-9])"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBody("Movie Deleted Successfully")));

        String toyStoryCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, toyStoryCrew, LocalDate.of(2019, 06, 20));
        Movie movie = moviesRestClient.addNewMovie(toyStory);
        Integer movieId = movie.getMovie_id().intValue();

        //when
        String response = moviesRestClient.deleteMovieById(movieId);

        //then
        String expectedResponse = "Movie Deleted Successfully";
        assertEquals(expectedResponse, response);

    }

    @Test
    void deleteMovie_notFound() {

        //given
        stubFor(delete(urlPathMatching("/movieservice/v1/movie/([0-9][0-9][0-9])"))
                .willReturn(WireMock.aResponse()
                        .withStatus(400)));

        Integer movieId = 100;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.deleteMovieById(movieId));

    }


    @Test
    @Disabled
    void getAllMovies_Exception() {
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveAllMovies());
    }

}
