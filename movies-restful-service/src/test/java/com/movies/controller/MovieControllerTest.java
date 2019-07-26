package com.movies.controller;

import com.movies.entity.Movie;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MovieControllerTest {

    MoviesController controllerTest = new MoviesController();

    @Test
    void createUpdatedMovieEntity_updateCrew(){
        String newCrewMember = "Katie Holmes";
        Movie updateMovie = new Movie(null, "", null, newCrewMember, null);
        String batmanBeginsCrew = "Christian Bale, Liam Neesan";
        Movie movieToUpdate = new Movie(1000l, "DarK Knight", 2011, batmanBeginsCrew, LocalDate.of(2011, 02, 02));
        controllerTest.createUpdatedMovieEntity(movieToUpdate, updateMovie);
        String expectedCrew = "Christian Bale, Liam Neesan, Katie Holmes";
        assertEquals(expectedCrew, movieToUpdate.getCast());
    }

    @Test
    void createUpdatedMovieEntity_updateCrew_Empty(){
        String newCrewMember = " ";
        Movie updateMovie = new Movie(null, "", null, newCrewMember, null);
        String batmanBeginsCrew = "Christian Bale, Liam Neesan";
        Movie movieToUpdate = new Movie(1000l, "DarK Knight", 2011, batmanBeginsCrew, LocalDate.of(2011, 02, 02));
        controllerTest.createUpdatedMovieEntity(movieToUpdate, updateMovie);
        String expectedCrew = "Christian Bale, Liam Neesan";
        assertEquals(expectedCrew, movieToUpdate.getCast());
    }
}
