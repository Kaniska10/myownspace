package com.infytel.helloworld.helloworld.dto;
import java.util.List;
public class MoviesRequestDTO {
    private List<MovieDTO> movies;

    // Getters and Setters
    public List<MovieDTO> getMovies() {
        return movies;
    }

    public void setMovies(List<MovieDTO> movies) {
        this.movies = movies;
    }
}
