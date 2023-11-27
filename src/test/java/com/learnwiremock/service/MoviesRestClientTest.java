package com.learnwiremock.service;

import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import sun.security.x509.OtherName;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void shouldReturnErrorWhenInvalidMovieIsAdded() {
        //given
        Movie newMovie = new Movie(null, null, 2012, "Dev Patel, Maggie Smith, Judi Dench", LocalDate.of(2012, 02,24));

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addMovie(newMovie));
        MovieErrorResponse thrownException = assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addMovie(newMovie));
        Assertions.assertEquals("Bad Request", thrownException.getMessage());
    }

    @Test
    void shouldEditExistingMovie() {
        //given
        Movie newMovie = new Movie(null, "The Best Exotic Marigold Hotel", 2012, "Dev Patel, Maggie Smith, Judi Dench", LocalDate.of(2012, 02,24));
        Movie addedMovie = moviesRestClient.addMovie(newMovie);
        Integer movieId = Math.toIntExact(addedMovie.getMovie_id());
        String newCastMember = "Hugh Laurie";
        Movie newlyCastMovie = new Movie(null, null, null, newCastMember, null);

        //when
        Movie editedMovie = moviesRestClient.editMovie(movieId, newlyCastMovie);

        //then
        assertTrue(editedMovie.getCast().contains(newCastMember));
    }

    @Test
    void shouldReturnErrorWhenInvalidMovieIsEdited() {
        //given
        String newCastMember = "Morgan Freeman";
        Integer movieId = 100;
        Movie newlyCastMovie = new Movie(null, null, null, newCastMember, null);

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.editMovie(movieId, newlyCastMovie));
    }

    @Test
    void shouldDeleteMovie() {
        //given
        Movie newMovie = new Movie(null, "The Best Exotic Marigold Hotel", 2012, "Dev Patel, Maggie Smith, Judi Dench", LocalDate.of(2012, 02,24));
        Movie addedMovie = moviesRestClient.addMovie(newMovie);
        Integer movieId = Math.toIntExact(addedMovie.getMovie_id());

        //when
        String deletedMovie = moviesRestClient.deleteMovie(movieId);

        //then
        Assertions.assertEquals(deletedMovie,"Movie Deleted Successfully");
    }

    @Test
    void shouldReturnErrorWhenInvalidMovieIsDeleted() {
        //given
        Integer movieId = 100;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.deleteMovie(movieId));
    }
}
