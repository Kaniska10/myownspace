package com.infytel.helloworld.helloworld.repository;
import com.infytel.helloworld.helloworld.Entity.Movies;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

@Transactional
public interface MoviesRepository extends JpaRepository<Movies, Long>{


}
