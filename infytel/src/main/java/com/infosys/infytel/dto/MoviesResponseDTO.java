package com.infosys.infytel.dto;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
public class MoviesResponseDTO {
    private List<JsonNode> movies;

    // Getters and Setters
    public List<JsonNode> getMovies() {
        return movies;
    }

    public void setMovies(List<JsonNode> movies) {
        this.movies = movies;
    }

    public void clear() {
        this.movies = null;
    }
}
