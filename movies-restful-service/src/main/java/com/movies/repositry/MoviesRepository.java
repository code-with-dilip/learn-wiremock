package com.movies.repositry;

import com.movies.entity.Movie;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface MoviesRepository extends CrudRepository<Movie, Long> {

    @Query("select m from Movie m where m.name like %?1%")
    List<Movie> findByMovieName(String movieName);

    List<Movie> findByYear(Integer year);

    List<Movie> findMovieByName(String movieName);


    @Transactional
    @Modifying
    @Query("delete from Movie m where m.name = ?1")
    void deleteByName(String name);
}
