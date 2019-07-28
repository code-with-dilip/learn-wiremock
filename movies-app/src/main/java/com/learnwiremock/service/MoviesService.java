package com.learnwiremock.service;

import com.learnwiremock.dto.Movie;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static com.learnwiremock.constants.MovieAppConstants.GET_ALL_MOVIES_V1;

public class MoviesService {

    private WebClient webClient;
    private String baseUrl;

    public MoviesService(String _baseUrl, WebClient _webClient) {
        this.webClient = _webClient;
        this.baseUrl = _baseUrl;
    }

    public List<Movie> retrieveAllMovies() {
        String getAllMoviesUrl = baseUrl+GET_ALL_MOVIES_V1;
        List<Movie> movies = webClient.get().uri(getAllMoviesUrl)
                .retrieve() // actual call is made to the api
                .bodyToFlux(Movie.class) //body is converted to flux
                .collectList() // collecting the result as a list
                .block(); // This call makes the Webclient to behave as a synchronous client.

        return movies;
    }
}
