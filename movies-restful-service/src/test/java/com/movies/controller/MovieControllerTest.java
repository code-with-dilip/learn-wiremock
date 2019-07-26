package com.movies.controller;

import com.movies.entity.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MovieControllerTest {

    MoviesController controllerTest = new MoviesController();

    private Movie movieToUpdate;

    @BeforeEach
    public void setUp(){
        String batmanBeginsCrew = "Christian Bale, Liam Neesan";
        movieToUpdate = new Movie(1000l, "DarK Knight", 2011, batmanBeginsCrew, LocalDate.of(2011, 02, 02));
    }

    @Test
    void createUpdatedMovieEntity_updateCrew(){
        String newCrewMember = "Katie Holmes";
        Movie updateMovie = new Movie(null, "", null, newCrewMember, null);
        controllerTest.createUpdatedMovieEntity(movieToUpdate, updateMovie);
        String expectedCast = "Christian Bale, Liam Neesan, Katie Holmes";
        assertEquals(expectedCast, movieToUpdate.getCast());
    }

    @Test
    void createUpdatedMovieEntity_updateCrew_Empty(){
        String newCastMember = " ";
        Movie updateMovie = new Movie(null, "", null, newCastMember, null);
        String batmanBeginsCrew = "Christian Bale, Liam Neesan";
        Movie movieToUpdate = new Movie(1000l, "DarK Knight", 2011, batmanBeginsCrew, LocalDate.of(2011, 02, 02));
        controllerTest.createUpdatedMovieEntity(movieToUpdate, updateMovie);
        String expectedCast = "Christian Bale, Liam Neesan";
        assertEquals(expectedCast, movieToUpdate.getCast());
    }

    @Test
    void createUpdatedMovieEntity_updateYear(){
        Integer year= 2012;
        Movie updateMovie = new Movie(null, "", 2012, null, null);
        controllerTest.createUpdatedMovieEntity(movieToUpdate, updateMovie);
        assertEquals(year, movieToUpdate.getYear());
    }

    @Test
    void createUpdatedMovieEntity_updateYear_nullValue(){
        Integer year= 2011;
        Movie updateMovie = new Movie(null, null, null, null, null);
        controllerTest.createUpdatedMovieEntity(movieToUpdate, updateMovie);
        assertEquals(year, movieToUpdate.getYear());
    }

    @Test
    void createUpdatedMovieEntity_updateName(){
        String name= "DarK Knight1";
        Movie updateMovie = new Movie(null, name, null, null, null);
        controllerTest.createUpdatedMovieEntity(movieToUpdate, updateMovie);
        assertEquals(name, movieToUpdate.getName());
    }

    @Test
    void createUpdatedMovieEntity_updateName_null(){
        String name= "DarK Knight";
        Movie updateMovie = new Movie(null, null, null, null, null);
        controllerTest.createUpdatedMovieEntity(movieToUpdate, updateMovie);
        assertEquals(name, movieToUpdate.getName());
    }

    @Test
    void createUpdatedMovieEntity_updateName_Empty(){

        //given
        String name= " ";
        Movie updateMovie = new Movie(null, name, null, null, null);

        //when
        controllerTest.createUpdatedMovieEntity(movieToUpdate, updateMovie);

        //then
        String expectedName = "DarK Knight";
        assertEquals(expectedName, movieToUpdate.getName());
    }

    @Test
    void createUpdatedMovieEntity_updateReleaseDate(){

        //given
        LocalDate newMovieReleaseDate = LocalDate.of(2013, 03, 03);
        Movie updateMovie = new Movie(null, null, null, null, newMovieReleaseDate);

        //when
        controllerTest.createUpdatedMovieEntity(movieToUpdate, updateMovie);

        //then
        assertEquals(newMovieReleaseDate, movieToUpdate.getRelease_date());

    }

    @Test
    void createUpdatedMovieEntity_updateReleaseDate_null(){

        //given
        LocalDate expectedReleaseDate = LocalDate.of(2011, 02, 02);
        Movie updateMovie = new Movie(null, null, null, null, null);

        //when
        controllerTest.createUpdatedMovieEntity(movieToUpdate, updateMovie);

        //then
        assertEquals(expectedReleaseDate, movieToUpdate.getRelease_date());

    }

    @Test
    void createUpdatedMovieEntity(){

        //given
        LocalDate newMovieReleaseDate = LocalDate.of(2013, 03, 03);
        String name= "DarK Knight1";
        Integer year= 2011;
        String newCrewMember = "Katie Holmes";
        Movie updateMovie = new Movie(null, name, year, newCrewMember, newMovieReleaseDate);

        //when
        controllerTest.createUpdatedMovieEntity(movieToUpdate, updateMovie);

        //then
        String expectedCast = "Christian Bale, Liam Neesan, Katie Holmes";
        assertEquals(newMovieReleaseDate, movieToUpdate.getRelease_date());
        assertEquals(name, movieToUpdate.getName());
        assertEquals(year, movieToUpdate.getYear());
        assertEquals(expectedCast, movieToUpdate.getCast());

    }

}
