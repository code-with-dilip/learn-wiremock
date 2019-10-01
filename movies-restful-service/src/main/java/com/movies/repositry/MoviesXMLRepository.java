package com.movies.repositry;

import com.movies.entity.Movie;
import com.movies.entity.MovieXML;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface MoviesXMLRepository extends CrudRepository<MovieXML, Long> {

}
