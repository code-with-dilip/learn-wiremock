package com.movies.controller;


import com.movies.constants.MoviesConstants;
import com.movies.entity.Movie;
import com.movies.repositry.MoviesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
public class MoviesControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    MoviesRepository moviesRepository;

    @BeforeEach
    public void setUp(){

        List<String> darKKnightCrew = List.of("Christian Bale", "Joker");
        List<String> avengersCrew = List.of("Robert Downey Junior", "Chris Hemsworth");
        List<Movie> moviesList = List.of(new Movie(000,"DarK Knight",2011, darKKnightCrew, LocalDate.of(2011,02,02)),
                new Movie(001,"Avengers EndGame",2019, avengersCrew, LocalDate.of(2019,04,05)));

       moviesRepository.saveAll(moviesList);
    }

    @Test
    void getAllItems(){

       List<Movie> items =  webTestClient.get().uri(MoviesConstants.GET_ALL_MOVIES)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Movie.class)
                .getResponseBody()
                .toStream().collect(Collectors.toList());

       assertEquals(2, items.size());

    }
}
