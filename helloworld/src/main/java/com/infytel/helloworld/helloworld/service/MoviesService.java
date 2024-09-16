package com.infytel.helloworld.helloworld.service;


import com.infytel.helloworld.helloworld.Entity.Movies;
import com.infytel.helloworld.helloworld.dto.MovieDTO;
import com.infytel.helloworld.helloworld.dto.MoviesRequestDTO;
import com.infytel.helloworld.helloworld.repository.MoviesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class MoviesService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MoviesRepository moviesRepository;

    public void uploadMovies(MoviesRequestDTO moviesRequestDTO){
        Movies movies;
        for ( MovieDTO movieDTO : moviesRequestDTO.getMovies()){
            movies = new Movies();
            movies.setId(movieDTO.getId());
            movies.setDirector(movieDTO.getDirector().trim());
            movies.setGenre(String.join("|", movieDTO.getGenre()));
            movies.setYear(movieDTO.getYear());
            movies.setTitle(movieDTO.getTitle());
            movies.setRunTime(movieDTO.getRuntime());
            moviesRepository.save(movies);
        }
    }


}
