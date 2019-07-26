package com.movies.controller;

import com.movies.constants.MoviesConstants;
import com.movies.entity.Movie;
import com.movies.repositry.MoviesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
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
    public ResponseEntity<?> movieById(@PathVariable Long id){

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

    @GetMapping(MoviesConstants.MOVIE_BY_YEAR_QUERY_PARAM_V1)
    public ResponseEntity<?> movieByYear(@RequestParam("year") Integer year){

        List<Movie> movies = moviesRepository.findByYear(year);
        if(CollectionUtils.isEmpty(movies)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(movies);

        }
    }

    @PostMapping(MoviesConstants.ADD_MOVIE_V1)
    public ResponseEntity<?> createMovie(@RequestBody Movie movie){
        //System.out.println("All Movies in the system : " + moviesRepository.findAll());
        return ResponseEntity.status(HttpStatus.CREATED).body(moviesRepository.save(movie));

    }


    @PutMapping(MoviesConstants.MOVIE_BY_ID_PATH_PARAM_V1)
    public ResponseEntity<?> updateMovie(@PathVariable Long id, @RequestBody Movie updateMovie){

        Optional<Movie> movieToUpdateOptional = moviesRepository.findById(id);
        if(movieToUpdateOptional.isPresent()){
            Movie movieToUpdate = movieToUpdateOptional.get();
            createUpdatedMovieEntity(movieToUpdate, updateMovie);
            return ResponseEntity.status(HttpStatus.OK).body(movieToUpdate);

        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    protected void createUpdatedMovieEntity(Movie movieToUpdate, Movie updateMovie) {
        if (updateMovie.getName()!=null && !updateMovie.getName().equals(movieToUpdate.getName())){
            movieToUpdate.setName(updateMovie.getName());
        }
        if(updateMovie.getYear()!=null && updateMovie.getYear()!= movieToUpdate.getYear()){
            movieToUpdate.setYear(updateMovie.getYear());
        }
        if(updateMovie.getRelease_date()!=null && !updateMovie.getRelease_date().isEqual(movieToUpdate.getRelease_date())){
            movieToUpdate.setRelease_date(updateMovie.getRelease_date());
        }

        if(!StringUtils.isEmpty(updateMovie.getCast()) && !StringUtils.isEmpty(updateMovie.getCast().trim()) && !updateMovie.getCast().equals(movieToUpdate.getCast())){
            String newCast = updateMovie.getCast();
            movieToUpdate.setCast(movieToUpdate.getCast().concat(", ").concat(newCast));
        }

    }


}
