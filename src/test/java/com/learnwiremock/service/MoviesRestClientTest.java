package com.learnwiremock.service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.learnwiremock.constants.MoviesAppConstants;
import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import org.apache.http.impl.conn.Wire;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import sun.security.x509.OtherName;

import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;
import static sun.awt.FontConfiguration.verbose;

@ExtendWith(WireMockExtension.class)
public class MoviesRestClientTest {

    MoviesRestClient moviesRestClient;
    WebClient webClient;

    @InjectServer
    WireMockServer wireMockServer;

    @ConfigureWireMock
    Options options = wireMockConfig()
            .port(8088)
            .notifier(new ConsoleNotifier(true))
            .extensions(new ResponseTemplateTransformer(true));


    @BeforeEach
    void setUp(){
        int port = wireMockServer.port();
        String baseUrl = String.format("http://localhost:%s", port);
        System.out.println("baseUrl : " + baseUrl);
        webClient = WebClient.create(baseUrl);
        moviesRestClient = new MoviesRestClient(webClient);
    }

    @Test
    void shouldRetrieveAllMovies(){
        //given
        stubFor(get(anyUrl()).willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("all-movies.json")));

        //when
        List<Movie> movieList = moviesRestClient.retrieveAllMovies();
        System.out.print("movieList: " + movieList);

        //then
        assertTrue(movieList.size()>0);
    }
    @Test
    void shouldRetrieveAllMovies_matchesUrl(){
        //given
        stubFor(get(urlPathEqualTo(MoviesAppConstants.GET_ALL_MOVIES_V1)).willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("all-movies.json")));

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
        stubFor(get(urlPathMatching(MoviesAppConstants.MOVIE_BY_ID_PATH_PARAM_V1.replace("{id}", "[0-9]")))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("movie-by-id.json")));

        //when
        Movie retrievedMovie = moviesRestClient.retrieveMovieById(movie_id);
        System.out.print("retrieved movie: " + retrievedMovie);

        //then
        assertEquals(retrievedMovie.getMovie_id(), 1);
        assertEquals(retrievedMovie.getName(), "Batman Begins");
    }
    @Test
    void shouldRetrieveMovieById_responseTemplating(){
        //given
        Integer movie_id = 9;
        stubFor(get(urlPathMatching(MoviesAppConstants.MOVIE_BY_ID_PATH_PARAM_V1.replace("{id}", "[0-9]")))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("movie-by-id-template.json")));

        //when
        Movie retrievedMovie = moviesRestClient.retrieveMovieById(movie_id);
        System.out.print("retrieved movie: " + retrievedMovie);

        //then
        assertEquals(9, retrievedMovie.getMovie_id());
        assertEquals("Batman Begins", retrievedMovie.getName());
    }

    @Test
    void shouldReturnErrorWhenRetrievingMovieWithInvalidId(){
        //given
        Integer movie_id = 100;
        stubFor(get(urlPathMatching(MoviesAppConstants.MOVIE_BY_ID_PATH_PARAM_V1.replace("{id}", "[0-9]+")))
                .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withBodyFile("movie-by-id-not-found.json")));

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMovieById(movie_id));
    }

    @Test
    void shouldRetrieveMoviesByName() {
        //given
        String movieName = "Avengers";
        stubFor(get(urlPathEqualTo(MoviesAppConstants.MOVIES_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movies-by-name-avengers.json")));

        //when
        List<Movie> retrievedMovies = moviesRestClient.retrieveMoviesByName(movieName);
        System.out.print("retrieved movies: " + retrievedMovies);

        //then
        assertEquals(retrievedMovies.size(), 4);
        String castExpected = "Robert Downey Jr, Chris Evans , Chris HemsWorth";
        assertEquals(castExpected, retrievedMovies.get(0).getCast());
    }

    @Test
    void shouldRetrieveMoviesByNameTemplate() {
        //given
        String movieName = "Avengers";
        stubFor(get(urlPathEqualTo(MoviesAppConstants.MOVIES_BY_NAME_QUERY_PARAM_V1))
                .withQueryParam("movie_name", equalTo(movieName))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movies-by-name-templating.json")));

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
        String movieName = URLEncoder.encode( "Inside me");

        stubFor(get(urlEqualTo(MoviesAppConstants.MOVIES_BY_NAME_QUERY_PARAM_V1))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("movies-by-name-not-found.json")));

        //when
        Assertions.assertThrows(MovieErrorResponse.class, ()-> moviesRestClient.retrieveMoviesByName(movieName));
    }

    @Test
    void shouldRetrieveMoviesByYear() {
        //given
        Integer year = 2008;

        stubFor(get(urlPathEqualTo(MoviesAppConstants.MOVIES_BY_YEAR_QUERY_PARAM_V1 ))
                .withQueryParam("year", equalTo(year.toString()))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("movies-by-year-template.json")));

        //when
        List<Movie> retrievedMovies = moviesRestClient.retrieveMoviesByYear(year);
        System.out.print("retrieved movies: " +retrievedMovies);

        //then
        assertEquals(retrievedMovies.size(), 1);
        assertEquals(retrievedMovies.get(0).getName(), "Batman Begins");
        assertEquals(retrievedMovies.get(0).getYear(), year);
    }

    @Test
    void shouldReturnErrorWhenRetrievingMovieWithInvalidYear(){
        //given
        Integer year = 3000;

        stubFor(get(urlEqualTo(MoviesAppConstants.MOVIES_BY_YEAR_QUERY_PARAM_V1 + "?year=" + year))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("movie-by-year-not-found.json")));

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.retrieveMoviesByYear(year));
    }

    @Test
    void shouldAddNewMovie() {
        //given
        Movie newMovie = new Movie(null, "The Best Exotic Marigold Hotel", 2012, "Dev Patel, Maggie Smith, Judi Dench", LocalDate.of(2012, 02,24));

        stubFor(post(urlEqualTo(MoviesAppConstants.ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("The Best Exotic Marigold Hotel")))
                .withRequestBody(matchingJsonPath("$.cast", containing("Dev Patel")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie.json")));

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
        stubFor(post(urlEqualTo(MoviesAppConstants.ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.cast", containing("Dev Patel")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie-bad-request.json")));

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addMovie(newMovie));
        MovieErrorResponse thrownException = assertThrows(MovieErrorResponse.class, () -> moviesRestClient.addMovie(newMovie));
        Assertions.assertEquals("Bad Request", thrownException.getMessage());
    }

    @Test
    void shouldEditExistingMovie() {
        //given
        Integer movieId = 11;
        String newCastMember = "Hugh Laurie";
        Movie newlyCastMovie = new Movie(null, null, null, newCastMember, null);
        stubFor(put(urlPathMatching(MoviesAppConstants.MOVIE_BY_ID_PATH_PARAM_V1.replace("{id}", "[0-9]+")))
                .withRequestBody(matchingJsonPath("$.cast", containing(newCastMember)))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("edited-movie.json")));

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
        stubFor(post(urlEqualTo(MoviesAppConstants.ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("The Best Exotic Marigold Hotel")))
                .withRequestBody(matchingJsonPath("$.cast", containing("Dev Patel")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie.json")));

        stubFor(delete(urlPathMatching(MoviesAppConstants.MOVIE_BY_ID_PATH_PARAM_V1.replace("{id}", "[0-9]+")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("Movie deleted successfully")));

        Movie addedMovie = moviesRestClient.addMovie(newMovie);
        Integer movieId = Math.toIntExact(addedMovie.getMovie_id());

        //when
        String deletedMovie = moviesRestClient.deleteMovie(movieId);

        //then
        Assertions.assertEquals(deletedMovie,"Movie deleted successfully");
    }

    @Test
    void shouldReturnErrorWhenInvalidMovieIsDeleted() {
        //given
        Integer movieId = 100;

        //when
        Assertions.assertThrows(MovieErrorResponse.class, () -> moviesRestClient.deleteMovie(movieId));
    }

    @Test
    void shouldDeleteMovieByName() {
        //given
        Movie newMovie = new Movie(null, "The Best Exotic Marigold Hotel", 2012, "Dev Patel, Maggie Smith, Judi Dench", LocalDate.of(2012, 02,24));
        stubFor(post(urlEqualTo(MoviesAppConstants.ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("The Best Exotic Marigold Hotel")))
                .withRequestBody(matchingJsonPath("$.cast", containing("Dev Patel")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("add-movie.json")));
        Movie addedMovie = moviesRestClient.addMovie(newMovie);

        stubFor(delete(urlEqualTo(MoviesAppConstants.MOVIES_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + "The%20Best%20Exotic%20Marigold%20Hotel"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("Movie deleted successfully")));


        Integer movieId = Math.toIntExact(addedMovie.getMovie_id());

        //when
        String deletedMovie = moviesRestClient.deleteMovieByName(addedMovie.getName());

        //then
        Assertions.assertEquals(deletedMovie,"Movie deleted successfully");

        verify(postRequestedFor(urlPathEqualTo(MoviesAppConstants.ADD_MOVIE_V1))
                .withRequestBody(matchingJsonPath("$.name", equalTo("The Best Exotic Marigold Hotel")))
                .withRequestBody(matchingJsonPath("$.cast", containing("Dev Patel"))));

        verify(deleteRequestedFor(urlEqualTo(MoviesAppConstants.MOVIES_BY_NAME_QUERY_PARAM_V1 + "?movie_name=" + "The%20Best%20Exotic%20Marigold%20Hotel")));
    }

}