package com.movies.controller;

import com.movies.constants.MoviesConstants;
import com.movies.entity.Movie;
import com.movies.repositry.MoviesRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(value = "Movie End Points", description = "Displays all the end points in for the Movies RESTFUl Service API")
public class MoviesController {

    @Autowired
    MoviesRepository moviesRepository;

    @GetMapping(MoviesConstants.GET_ALL_MOVIES_V1)
    @ApiOperation("Retrieves all the Movies")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "SuccessFul Retrieval of Movies")
            }
    )
    public List<Movie> allMovies() {
        List<Movie> moviesList = new ArrayList<>();
        moviesRepository.findAll().
                forEach(movie -> {
                    moviesList.add(movie);
                });
        return moviesList;

    }

    @GetMapping(MoviesConstants.MOVIE_BY_ID_PATH_PARAM_V1)
    @ApiOperation("Retrieve a movie using the movie id.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Returns the movie for the id."),
                    @ApiResponse(code = 404, message = "No movie found for the id thats passed."),
            }
    )
    public ResponseEntity<?> movieById(@PathVariable Long id) {

        Optional<Movie> movieOptional = moviesRepository.findById(id);
        if (movieOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(movieOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @ApiOperation("Retrieve a movie using the movie name.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Returns the movie using the name of the movie."),
                    @ApiResponse(code = 404, message = "No movie found for the name thats passed."),
            }
    )
    @GetMapping(MoviesConstants.MOVIE_BY_NAME_QUERY_PARAM_V1)
    public ResponseEntity<?> movieByName(@RequestParam("movie_name") String name) {

        List<Movie> movies = moviesRepository.findByMovieName(name);
        if (CollectionUtils.isEmpty(movies)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(movies);

        }
    }

    @GetMapping(MoviesConstants.MOVIE_BY_YEAR_QUERY_PARAM_V1)
    @ApiOperation("Returns the movies for the year passed as part of the request.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Returns the movies using the year thats passed."),
                    @ApiResponse(code = 404, message = "No movie found for the year that's passed."),
            }
    )
    public ResponseEntity<?> movieByYear(@RequestParam("year") Integer year) {

        List<Movie> movies = moviesRepository.findByYear(year);
        if (CollectionUtils.isEmpty(movies)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(movies);

        }
    }

    @ApiOperation("Adds a new movie.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 201, message = "Movie Successfully added to the DB.")
            }
    )
    @PostMapping(MoviesConstants.ADD_MOVIE_V1)
    public ResponseEntity<?> createMovie(@RequestBody Movie movie) {
        //System.out.println("All Movies in the system : " + moviesRepository.findAll());
        return ResponseEntity.status(HttpStatus.CREATED).body(moviesRepository.save(movie));

    }

    @ApiOperation("Updates the movie details.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Movie details are successfully updated to the DB."),
                    @ApiResponse(code = 404, message = "No movie found for the year that's passed."),
            }
    )
    @PutMapping(MoviesConstants.MOVIE_BY_ID_PATH_PARAM_V1)
    public ResponseEntity<?> updateMovie(@PathVariable Long id, @RequestBody Movie updateMovie) {
        Optional<Movie> movieToUpdateOptional = moviesRepository.findById(id);
        if (movieToUpdateOptional.isPresent()) {
            Movie movieToUpdate = movieToUpdateOptional.get();
            createUpdatedMovieEntity(movieToUpdate, updateMovie);
            return ResponseEntity.status(HttpStatus.OK).body(moviesRepository.save(movieToUpdate));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @ApiOperation("Removes the movie details.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Movie details are successfully deleted from the DB."),
                    @ApiResponse(code = 404, message = "No movie found for the year that's passed."),
            }
    )
    @DeleteMapping(MoviesConstants.MOVIE_BY_ID_PATH_PARAM_V1)
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {

        Optional<Movie> movieToUpdateOptional = moviesRepository.findById(id);
        if (movieToUpdateOptional.isPresent()) {
            moviesRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body(MoviesConstants.DELETE_MESSAGE);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }


    }

    protected void createUpdatedMovieEntity(Movie movieToUpdate, Movie updateMovie) {
        if (checkEmptyNullString(updateMovie.getName()) && !updateMovie.getName().equals(movieToUpdate.getName())) {
            movieToUpdate.setName(updateMovie.getName());
        }
        if (updateMovie.getYear() != null && updateMovie.getYear() != movieToUpdate.getYear()) {
            movieToUpdate.setYear(updateMovie.getYear());
        }
        if (updateMovie.getRelease_date() != null && !updateMovie.getRelease_date().isEqual(movieToUpdate.getRelease_date())) {
            movieToUpdate.setRelease_date(updateMovie.getRelease_date());
        }

        if (checkEmptyNullString(updateMovie.getCast()) && !updateMovie.getCast().equals(movieToUpdate.getCast())) {
            String newCast = updateMovie.getCast();
            movieToUpdate.setCast(movieToUpdate.getCast().concat(", ").concat(newCast));
        }

    }

    private boolean checkEmptyNullString(String input) {
        return !StringUtils.isEmpty(input) && !StringUtils.isEmpty(input.trim());
    }


}
