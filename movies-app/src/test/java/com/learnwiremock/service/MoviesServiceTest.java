package com.learnwiremock.service;

import com.learnwiremock.dto.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class MoviesServiceTest {

    MoviesService moviesService = null;
    WebClient webClient;


    @BeforeEach
    void setUp(){
        int port = 8081;
        final String baseUrl = String.format("http://localhost:%s", port);
        webClient= WebClient.create();
        moviesService = new MoviesService(baseUrl, webClient);

    }

    @Test
    void getAllMovies(){

        List<Movie> moviesList = moviesService.retrieveAllMovies();
        System.out.println("moviesList : " + moviesList);
        assertTrue(!moviesList.isEmpty());
        assertEquals(10, moviesList.size());
    }
}
