package org.example.repository;

import org.example.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByReleaseYearBetweenOrderByReleaseYearDescTitleAsc(Integer yearFrom, Integer yearTo);

    Optional<Movie> findByTitleAndReleaseYear(String title, Integer releaseYear);
}