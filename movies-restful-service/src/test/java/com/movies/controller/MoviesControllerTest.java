package com.movies.controller;


import com.movies.constants.MoviesConstants;
import com.movies.entity.Movie;
import com.movies.repositry.MoviesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.movies.constants.MoviesConstants.MOVIE_BY_NAME_QUERY_PARAM_V1;
import static com.movies.constants.MoviesConstants.MOVIE_BY_YEAR_QUERY_PARAM_V1;
import static org.junit.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class MoviesControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    MoviesRepository moviesRepository;

    @Value("${server.servlet.context-path}")
    private String contextPath;


    @BeforeEach
    public void setUp() {


        String darKKnightCrew = "Christian Bale, Joker";
        String avengersCrew = "Robert Downey Junior, Chris Hemsworth";
        List<Movie> moviesList = List.of(new Movie(000, "DarK Knight", 2011, darKKnightCrew, LocalDate.of(2011, 02, 02)),
                new Movie(001, "Avengers EndGame", 2019, avengersCrew, LocalDate.of(2019, 04, 05)));

        moviesRepository.saveAll(moviesList);
    }

    @AfterEach
    public void tearDown(){
        moviesRepository.deleteAll();
    }

    @Test
    void getAllItems() {

        List<Movie> items = webTestClient.get().uri(contextPath.concat(MoviesConstants.GET_ALL_MOVIES_V1))
                .exchange()
                .expectStatus().isOk()
                .returnResult(Movie.class)
                .getResponseBody()
                .toStream().collect(Collectors.toList());

        assertEquals(2, items.size());

    }

    @Test
    void movieById() {

        Movie movie = webTestClient.get().uri(contextPath.concat(MoviesConstants.MOVIE_BY_ID_PATH_PARAM_V1), 001)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Movie.class)
                .getResponseBody()
                .blockLast();

        assertEquals("Avengers EndGame", movie.getMovie_name());

    }

    @Test
    void movieById_NotFound() {

        webTestClient.get().uri(contextPath.concat(MoviesConstants.MOVIE_BY_ID_PATH_PARAM_V1), 123)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void movieByIdName() {

        String avengersCrew = "Robert Downey Junior, Chris Hemsworth";
        Movie movie = new Movie(002, "Avengers", 2013, avengersCrew, LocalDate.of(2013, 04, 05));
        moviesRepository.save(movie);

        List<Movie> movies = webTestClient.get().uri(uriBuilder -> uriBuilder.path(contextPath.concat(MOVIE_BY_NAME_QUERY_PARAM_V1))
                .queryParam("movie_name", "Avengers")
                .build())
                .exchange()
                .expectStatus().isOk()
        .returnResult(Movie.class)
        .getResponseBody()
        .toStream().collect(Collectors.toList());

        assertEquals(2, movies.size());
    }

    @Test
    void movieByIdName_NotFound() {
        webTestClient.get().uri(uriBuilder -> uriBuilder.path(contextPath.concat(MOVIE_BY_NAME_QUERY_PARAM_V1))
                .queryParam("movie_name", "ABC")
                .build())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void movieByYear(){

        List<Movie> movies = webTestClient.get().uri(uriBuilder -> uriBuilder.path(contextPath.concat(MOVIE_BY_YEAR_QUERY_PARAM_V1))
                .queryParam("year", 2011)
                .build())
                .exchange()
                .expectStatus().isOk()
                .returnResult(Movie.class)
                .getResponseBody()
                .toStream().collect(Collectors.toList());

        assertEquals(1, movies.size());
        assertEquals(000, movies.get(0).getMovie_id().intValue());

    }

    @Test
    void movieByYear_NotFound(){
        webTestClient.get().uri(uriBuilder -> uriBuilder.path(contextPath.concat(MOVIE_BY_YEAR_QUERY_PARAM_V1))
                .queryParam("year", 2012)
                .build())
                .exchange()
                .expectStatus().isNotFound();

    }


}
