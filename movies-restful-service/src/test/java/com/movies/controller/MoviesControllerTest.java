package com.movies.controller;


import com.movies.constants.MoviesConstants;
import com.movies.entity.Movie;
import com.movies.repositry.MoviesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

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
@DirtiesContext
@SqlGroup({
       @Sql(scripts = "/sql/moviedatasetup.sql"),
        @Sql(scripts = {"/sql/TearDown.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)

})
public class MoviesControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    MoviesRepository moviesRepository;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Test
    void getAllItems() {

        List<Movie> items = webTestClient.get().uri(contextPath.concat(MoviesConstants.GET_ALL_MOVIES_V1))
                .exchange()
                .expectStatus().isOk()
                .returnResult(Movie.class)
                .getResponseBody()
                .toStream().collect(Collectors.toList());

        System.out.println("items : " + items);
        assertEquals(2, items.size());

    }

    @Test
    void movieById() {

        Movie movie = webTestClient.get().uri(contextPath.concat(MoviesConstants.MOVIE_BY_ID_PATH_PARAM_V1), 1001)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Movie.class)
                .getResponseBody()
                .blockLast();

        assertEquals("Avengers EndGame", movie.getName());

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
        Movie movie = new Movie(3l, "Avengers", 2013, avengersCrew, LocalDate.of(2013, 04, 05));
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
        System.out.println("Movie is : " + movies.get(0));
        assertEquals(2011, movies.get(0).getYear().intValue());

    }

    @Test
    void movieByYear_NotFound(){
        webTestClient.get().uri(uriBuilder -> uriBuilder.path(contextPath.concat(MOVIE_BY_YEAR_QUERY_PARAM_V1))
                .queryParam("year", 2012)
                .build())
                .exchange()
                .expectStatus().isNotFound();

    }

    @Test
    void createMovie(){

        String batmanBeginsCrew = "Christian Bale, Liam Neesan";
        Movie newMovie = new Movie(null, "Batman Begins", 2008, batmanBeginsCrew, LocalDate.of(2018, 02, 02));
        webTestClient.post().uri(contextPath.concat(MoviesConstants.ADD_MOVIE_V1))
                .body(Mono.just(newMovie), Movie.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.movie_id").isNotEmpty()
                .jsonPath("$.year", 2008);


    }

    @Test
    @Disabled
    void createMovie_DuplicateRecord(){

        String batmanBeginsCrew = "Christian Bale, Liam Neesan";
        Movie newMovie = new Movie(1000l, "DarK Knight", 2011, batmanBeginsCrew, LocalDate.of(2011, 02, 02));
        webTestClient.post().uri(contextPath.concat(MoviesConstants.ADD_MOVIE_V1))
                .body(Mono.just(newMovie), Movie.class)
                .exchange()
                .expectStatus().is5xxServerError();

    }


}
