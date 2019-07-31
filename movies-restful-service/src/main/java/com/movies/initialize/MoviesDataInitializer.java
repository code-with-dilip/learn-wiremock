package com.movies.initialize;

import com.movies.entity.Movie;
import com.movies.repositry.MoviesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("!test")
@Slf4j
public class MoviesDataInitializer implements CommandLineRunner {

    @Autowired
    MoviesRepository moviesRepository;

    @Override
    public void run(String... args) throws Exception {

        Movie batmanBegins = new Movie(null, "Batman Begins", 2005, "Christian Bale, Katie Holmes , Liam Neeson", LocalDate.parse("2005-06-15"));
        Movie darkKnight = new Movie(null, "Dark Knight", 2008, "Christian Bale, Heath Ledger , Michael Caine", LocalDate.parse("2008-07-18"));
        Movie darkKnightRises = new Movie(null, "The Dark Knight Rises", 2012, "Christian Bale, Heath Ledger , Michael Caine", LocalDate.parse("2012-07-20"));
        Movie avengers = new Movie(null, "The Avengers", 2012, "Robert Downey Jr, Chris Evans , Chris HemsWorth", LocalDate.parse("2012-05-04"));
        Movie avengersAgeOfUltron = new Movie(null, "Avengers: Age of Ultron", 2015, "Robert Downey Jr, Chris Evans , Chris HemsWorth", LocalDate.parse("2015-05-01"));
        Movie avengersInfinityWar = new Movie(null, "Avengers: Infinity War", 2018, "Robert Downey Jr, Chris Evans , Chris HemsWorth", LocalDate.parse("2018-04-27"));
        Movie avengersEndGame = new Movie(null, "Avengers: End Game", 2019, "Robert Downey Jr, Chris Evans , Chris HemsWorth", LocalDate.parse("2019-04-26"));
        Movie hangOver = new Movie(null, "The Hangover", 2009, "Bradley Cooper, Ed Helms , Zach Galifianakis", LocalDate.parse("2009-06-05"));
        Movie theImitationGame = new Movie(null, "The Imitation Game", 2014, "Benedict Cumberbatch, Keira Knightley", LocalDate.parse("2014-12-25"));
        Movie theDeparted = new Movie(null, "The Departed", 2006, "Leonardo DiCaprio, Matt Damon , Mark Wahlberg", LocalDate.parse("2006-10-06"));

        List<Movie> moviesList = Arrays.asList(batmanBegins,darkKnight,darkKnightRises,avengers,avengersAgeOfUltron,avengersInfinityWar,avengersEndGame,hangOver,theImitationGame,theDeparted);
        moviesRepository.saveAll(moviesList);
        log.info("********* Movies RestFul Service Initial Data Starts *********");
        moviesRepository.findAll()
                .forEach((movie -> log.info(""+movie)));
        log.info("********* Movies RestFul Service Initial Data Ends *********");
    }
}
