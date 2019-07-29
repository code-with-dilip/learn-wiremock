package com.learnwiremock.service;

import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesServiceTest {

    MoviesService moviesService = null;
    WebClient webClient;


    @BeforeEach
    void setUp() {
        int port = 8081;
        final String baseUrl = String.format("http://localhost:%s", port);
        webClient = WebClient.create();
        moviesService = new MoviesService(baseUrl, webClient);

    }

    @Test
    void getAllMovies() {

        //when
        List<Movie> movieList = moviesService.retrieveAllMovies();

        //then
        assertTrue(!movieList.isEmpty());
    }

    @Test
    void retrieveMovieById() {
        //given
        Integer movieId = 1;

        //when
        Movie movie = moviesService.retrieveMovieById(movieId);

        //then
        assertEquals("Batman Begins", movie.getName());
    }

    @Test
    void retrieveMovieById_NotFound() {
        //given
        Integer movieId = 100;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesService.retrieveMovieById(movieId));

    }

    @Test
    void retrieveMovieByName() {
        //given
        String movieName = "Avengers";

        //when
        List<Movie> movieList = moviesService.retrieveMovieByName(movieName);

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
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesService.retrieveMovieByName(movieName));
    }


    @Test
    void retrieveMovieByYear() {
        //given
        Integer year = 2012;

        //when
        List<Movie> movieList = moviesService.retreieveMovieByYear(year);

        //then
        assertEquals(2, movieList.size());

    }

    @Test
    void retrieveMovieByYear_Not_Found() {
        //given
        Integer year = 1950;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesService.retreieveMovieByYear(year));

    }

    @Test
    void addNewMovie() {
        //given
        String batmanBeginsCrew = "Tom Hanks, Tim Allen";
        Movie toyStory = new Movie(null, "Toy Story 4", 2019, batmanBeginsCrew, LocalDate.of(2019, 06, 20));

        //when
        Movie movie = moviesService.addNewMovie(toyStory);

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
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesService.addNewMovie(toyStory));

    }

    @Test
    void updateMovie() {
        //given
        String darkNightRisesCrew = "Tom Hardy";
        Movie darkNightRises = new Movie(null, null, null, darkNightRisesCrew, null);
        Integer movieId = 3;

        //when
        Movie updatedMovie = moviesService.updateMovie(movieId, darkNightRises);

        //then
        String updatedCastName = "Christian Bale, Heath Ledger , Michael Caine, Tom Hardy";
        assertEquals(updatedCastName, updatedMovie.getCast());


    }

    @Test
    void updateMovie_Not_Found() {
        //given
        String darkNightRisesCrew = "Tom Hardy";
        Movie darkNightRises = new Movie(null, null, null, darkNightRisesCrew, null);
        Integer movieId = 100;

        //when
        Assertions.assertThrows(MovieErrorResponse.class,()->moviesService.updateMovie(movieId, darkNightRises));
    }


    @Test
    @Disabled
    void getAllMovies_Exception() {
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesService.retrieveAllMovies());
    }

}
