package com.movies.repositry;

import com.movies.entity.Movie;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MoviesRepository extends CrudRepository<Movie, Long> {

    @Query("select m from Movie m where m.movie_name like %?1%")
    List<Movie> findByMovieName(String movieName);

    List<Movie> findByYear(Integer year);
}
