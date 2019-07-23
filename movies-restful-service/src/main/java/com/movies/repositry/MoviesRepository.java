package com.movies.repositry;

import com.movies.entity.Movie;
import org.springframework.data.repository.CrudRepository;

public interface MoviesRepository extends CrudRepository<Movie, Integer> {
}
