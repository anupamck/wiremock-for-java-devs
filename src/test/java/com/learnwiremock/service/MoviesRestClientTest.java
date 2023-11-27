package com.learnwiremock.service;

import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesRestClientTest {

    MoviesRestClient moviesRestClient;
    WebClient webClient;

    @BeforeEach
    void setUp(){
        String baseUrl = "http://localhost:8081";
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);
    }

    @Test
    void shouldRetrieveAllMovies(){
        //when
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.print("movieList: " + movieList);

        //then
        assertTrue(movieList.size()>0);
    }

    @Test
    void shouldRetrieveMovieById(){
        //given
        Integer movie_id = 1;

        //when
        Movie retrievedMovie = moviesRestClient.retrieveMovieById(movie_id);
        System.out.print("retrieved movie: " + retrievedMovie);

        //then
        assertEquals(retrievedMovie.getMovie_id(), 1);
        assertEquals(retrievedMovie.getName(), "Batman Begins");
    }

    @Test
    void shouldReturnErrorWhenRetrievingMovieWithInvalidId(){
        //given
        Integer movie_id = 100;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieById(movie_id));
    }

    @Test
    void shouldRetrieveMoviesByName() {
        //given
        String movieName = "Avengers";

        //when
        List<Movie> retrievedMovies = moviesRestClient.retrieveMoviesByName(movieName);
        System.out.print("retrieved movies: " + retrievedMovies);

        //then
        assertEquals(retrievedMovies.size(), 4);
        String castExpected = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(castExpected, retrievedMovies.get(0).getCast());
    }

    @Test
    void shouldReturnErrorWhenRetrievingMovieWithInvalidName(){
        //given
        String movieName = "Inside me";

        //when
        Assertions.assertThrows(MovieErrorResponse.class, ()-> moviesRestClient.retrieveMoviesByName(movieName));
    }

    @Test
    void shouldRetrieveMoviesByYear() {
        //given
        Integer year = 2012;

        //when
        List<Movie> retrievedMovies = moviesRestClient.retrieveMoviesByYear(year);
        System.out.print("retrieved movies: " +retrievedMovies);

        //then
        assertEquals(retrievedMovies.size(), 2);
        assertEquals(retrievedMovies.get(0).getName(), "The Dark Knight Rises");
    }

    @Test
    void shouldReturnErrorWhenRetrievingMovieWithInvalidYear(){
        //given
        Integer year = 3000;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMoviesByYear(year));
    }

    @Test
    void shouldAddNewMovie() {
        //given
        Movie newMovie = new Movie(null, "The Best Exotic Marigold Hotel", 2012, "Dev Patel, Maggie Smith, Judi Dench", LocalDate.of(2012, 02,24));
        //when
        Movie addedMovie = moviesRestClient.addMovie(newMovie);
        System.out.print(addedMovie);

        //then
        assertTrue(addedMovie.getMovie_id()!=null);
        assertEquals(addedMovie.getName(), "The Best Exotic Marigold Hotel");
    }
}
