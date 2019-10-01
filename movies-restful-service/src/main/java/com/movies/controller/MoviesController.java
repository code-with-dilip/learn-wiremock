package com.movies.controller;

import com.movies.constants.MoviesConstants;
import com.movies.entity.Movie;
import com.movies.entity.MovieXML;
import com.movies.repositry.MoviesRepository;
import com.movies.repositry.MoviesXMLRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@RestController
@Slf4j
@Api(value = "Movie End Points", description = "Displays all the end points in for the Movies RESTFUl Service API")
public class MoviesController {

    Function<Long, ResponseStatusException> notFoundId = (id) -> {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No Movie Available with the given Id - "+ id);
    };

    Function<String,ResponseStatusException > notFoundName = (name) -> {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Movie Available with the given name - "+ name);
    };

    Function<Integer,ResponseStatusException > notFoundYear = (id) -> {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Movie Available with the given year - "+ id);
    };


    Supplier<ResponseStatusException > serverError = () -> {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "RunTimeException from Movie Service");
    };


    @Autowired
    MoviesRepository moviesRepository;

    @Autowired
    MoviesXMLRepository moviesXMLRepository;

    @GetMapping(MoviesConstants.GET_ALL_MOVIES_V1)
    @ApiOperation("Retrieves all the Movies")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "SuccessFul Retrieval of Movies")
            }
    )
    public List<Movie> allMovies() {
        List<Movie> moviesList = new ArrayList<>();
        log.info("Received request for All Movies");
        moviesRepository.findAll().
                forEach(movie -> {
                    moviesList.add(movie);
                });
        log.info("Response is : {}", moviesList);
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

        log.info("Received the request to search by Movie Id - {} .", id);

        Optional<Movie> movieOptional = moviesRepository.findById(id);
        if (movieOptional.isPresent()) {
            log.info("Response is : {}", movieOptional.get());
            return ResponseEntity.status(HttpStatus.OK).body(movieOptional.get());
        } else {
            log.info("No Movie available for the given Movie Id - {}.", id);
            throw notFoundId.apply(id);
        }

    }

    @ApiOperation("Returns the movies using the movie name passed as part of the request.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Returns the movie using the name of the movie."),
                    @ApiResponse(code = 404, message = "No movie found for the name thats passed."),
            }
    )
    @GetMapping(MoviesConstants.MOVIE_BY_NAME_QUERY_PARAM_V1)
    public ResponseEntity<?> movieByName(@RequestParam("movie_name") String name) {

        log.info("Received the request to search by Movie name - {} .", name);

        List<Movie> movies = moviesRepository.findByMovieName(name);
        if (CollectionUtils.isEmpty(movies)) {
            log.info("No Movie available for the given Movie name - {}.", name);
            throw notFoundName.apply(name);

        } else {
            log.info("Response is : {}", movies);
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

        log.info("Received the request to search by Movie Year - {} .", year);

        List<Movie> movies = moviesRepository.findByYear(year);
        if (CollectionUtils.isEmpty(movies)) {
            log.info("No Movie available for the given Movie Year - {}.", year);
            throw notFoundYear.apply(year);
        } else {
            log.info("Response is : {}", movies);
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
    public ResponseEntity<?> createMovie(@Valid @RequestBody Movie movie) {

        log.info("Received the request to add a new Movie in the service {} ", movie);
        Movie addedMovie = moviesRepository.save(movie);
        log.info("Movie SuccessFully added to the DB. New Movie Details are - .", movie);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedMovie);

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

        log.info("Received the request to update the movie. Movie Id is {} and the updated Movie Details are {} ", id, updateMovie);

        Optional<Movie> movieToUpdateOptional = moviesRepository.findById(id);
        if (movieToUpdateOptional.isPresent()) {
            Movie movieToUpdate = movieToUpdateOptional.get();
            createUpdatedMovieEntity(movieToUpdate, updateMovie);
            moviesRepository.save(movieToUpdate);
            log.info("Updated Movie Details are - ", movieToUpdate);
            return ResponseEntity.status(HttpStatus.OK).body(movieToUpdate);
        } else {
            log.info("No Movie available for the given Movie Id - {}.", id);
            throw notFoundId.apply(id);
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

        log.info("Received the request to delete a movie and the id is {} .", id);
        Optional<Movie> movieToUpdateOptional = moviesRepository.findById(id);
        if (movieToUpdateOptional.isPresent()) {
            moviesRepository.deleteById(id);
            log.info("Movie Successfully deleted from the DB");
            return ResponseEntity.status(HttpStatus.OK).body(MoviesConstants.DELETE_MESSAGE);
        } else {
            log.info("No Movie available for the given Movie Id - {}.", id);
            throw notFoundId.apply(id);
        }


    }

   // @DeleteMapping(MoviesConstants.MOVIE_BY_NAME_PATH_PARAM_V1)
    public ResponseEntity<?> deleteMovieByName(@PathVariable String name) {
        log.info("Received the request to delete a movie and the id is {} .", name);
        log.info("Movie " + moviesRepository.findMovieByName(name));
        if(CollectionUtils.isEmpty(moviesRepository.findMovieByName(name))){
            throw notFoundName.apply(name);
        }else{
            moviesRepository.deleteByName(name);
            return ResponseEntity.status(HttpStatus.OK).build();
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

    @ApiOperation("Adds a new movie.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 201, message = "Movie Successfully added to the DB.")
            }
    )
    @PostMapping(value = MoviesConstants.ADD_MOVIE_XML_V1,consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> createMovieXML(@Valid @RequestBody MovieXML movie) {

        log.info("Received the request to add a new Movie in the service {} ", movie);
        MovieXML addedMovie = moviesXMLRepository.save(movie);
        log.info("Movie SuccessFully added to the DB. New Movie Details are - .", movie);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedMovie);

    }


}
