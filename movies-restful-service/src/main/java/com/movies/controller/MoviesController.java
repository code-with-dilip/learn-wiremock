package com.movies.controller;

import com.movies.constants.MoviesConstants;
import com.movies.entity.Movie;
import com.movies.repositry.MoviesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
public class MoviesController {

    @Autowired
    MoviesRepository moviesRepository;

    @GetMapping(MoviesConstants.GET_ALL_MOVIES_V1)
    public List<Movie> allMovies(){

        List<Movie> moviesList = new ArrayList<>();
         moviesRepository.findAll().
                forEach(movie -> {
                    moviesList.add(movie);
                });
         return moviesList;

    }

    @GetMapping(MoviesConstants.MOVIE_BY_ID_PATH_PARAM_V1)
    public ResponseEntity<?> movieById(@PathVariable Integer id){

        Optional<Movie> movieOptional = moviesRepository.findById(id);
        if(movieOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.OK).body(movieOptional.get());
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping(MoviesConstants.MOVIE_BY_NAME_QUERY_PARAM_V1)
    public ResponseEntity<?> movieByName(@RequestParam("movie_name") String name){

        List<Movie> movies = moviesRepository.findByMovieName(name);
        if(CollectionUtils.isEmpty(movies)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(movies);

        }
    }




}
