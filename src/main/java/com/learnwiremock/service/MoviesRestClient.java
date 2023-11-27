package com.learnwiremock.service;

import com.learnwiremock.constants.MoviesAppConstants;
import com.learnwiremock.dto.Movie;
import com.learnwiremock.exception.MovieErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
public class MoviesRestClient {
    private WebClient webClient;

    public MoviesRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Movie> retrieveAllMovies(){

        return webClient.get().uri(MoviesAppConstants.GET_ALL_MOVIES_V1)
                .retrieve()
                .bodyToFlux(Movie.class)
                .collectList()
                .block();
    }

    public Movie retrieveMovieById(Integer movieId){
        try {
            return webClient.get().uri(MoviesAppConstants.RETRIEVE_MOVIE_BY_ID, movieId)
                .retrieve()
                .bodyToMono(Movie.class)
                .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in retrieveMovieById. Status Code: {}; Message: {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Exception in retrieveMovieById: {}", e);
            throw new MovieErrorResponse(e);
        }
    }

    public List<Movie> retrieveMoviesByName(String movieName) {

        String retrieveMoviesByNameUri = UriComponentsBuilder.fromUriString(MoviesAppConstants.MOVIES_BY_NAME_QUERY_PARAM_V1)
                .queryParam("movie_name", movieName)
                .buildAndExpand()
                .toUriString();

        try {
            return webClient.get().uri(retrieveMoviesByNameUri)
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in retrieveMoviesByName. " +
                            "Status Code: {}; Message: {}",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new MovieErrorResponse(e.getStatusText(), e);
        } catch (Exception e) {
            log.error("Exception in retrieveMoviesByName: {}", e);
            throw new MovieErrorResponse(e);
        }
    }

    public List<Movie> retrieveMoviesByYear(Integer year) {
        String retrieveMoviesByYearUri = UriComponentsBuilder.fromUriString(
                        MoviesAppConstants.MOVIES_BY_YEAR_QUERY_PARAM_V1)
                .queryParam("year", year)
                .buildAndExpand()
                .toUriString();

        try {
            return webClient.get().uri(retrieveMoviesByYearUri)
                    .retrieve()
                    .bodyToFlux(Movie.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
           log.error("WebClientResponseException in retrieveMoviesByYear. " +
                           "Status Code: {}; Message: {}",
                   e.getRawStatusCode(), e.getResponseBodyAsString());
           throw new MovieErrorResponse(e.getStatusText(), e);
         } catch (Exception e) {
           log.error("Exception in retrieveMoviesByYear: {}", e);
           throw new MovieErrorResponse(e);
           }
    }

    public Movie addMovie( Movie newMovie) {
        return webClient.post().uri(MoviesAppConstants.ADD_MOVIE_V1)
                .syncBody(newMovie)
                .retrieve()
                .bodyToMono(Movie.class)
                .block();
    }

}

