package com.infytel.helloworld.helloworld.Entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Movies {

    @Id
    @Column(name = "movie_id", nullable = false)
    Long id;
    @Column(name = "movie_title", nullable = false, length = 200)
    String title;
    @Column(name = "movie_year", nullable = false)
    Long year;
    @Column(name = "movie_director", nullable = false, length = 200)
    String director;
    @Column(name = "movie_director", nullable = false, length = 230)
    String genre;
    @Column(name = "movie_time", nullable = false)
    Long runTime;

    public Movies()
    {
        super();
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getYear() {
        return year;
    }

    public void setYear(Long year) {
        this.year = year;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Long getRunTime() {
        return runTime;
    }

    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }
}
