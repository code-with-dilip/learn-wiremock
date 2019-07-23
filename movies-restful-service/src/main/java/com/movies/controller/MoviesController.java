package com.movies.controller;

import com.movies.constants.MoviesConstants;
import com.movies.entity.Movie;
import com.movies.repositry.MoviesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class MoviesController {

    @Autowired
    MoviesRepository moviesRepository;

    @GetMapping(MoviesConstants.GET_ALL_MOVIES)
    public List<Movie> allMovies(){

        List<Movie> moviesList = new ArrayList<>();
         moviesRepository.findAll().
                forEach(movie -> {
                    moviesList.add(movie);
                });
         return moviesList;

    }



}
