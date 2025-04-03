package org.example.controller;

import org.example.entity.Movie;
import org.example.service.ExportService;
import org.example.service.MovieScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @Autowired
    private MovieScraperService movieScraperService;

    @Autowired
    private ExportService exportService;


    @GetMapping("/scrape")
    public ResponseEntity<List<Movie>> scrapeMovies() {
        List<Movie> processedMovies = movieScraperService.scrapeAndSaveMovies();
        return ResponseEntity.ok(processedMovies);
    }


    @GetMapping("/load")
    public ResponseEntity<List<Movie>> loadMovies(
            @RequestParam Integer yearFrom,
            @RequestParam Integer yearTo) {

        int currentYear = Year.now().getValue();
        int from = (yearFrom != null && yearFrom > 1800) ? yearFrom : 1990;
        int to = (yearTo != null && yearTo <= currentYear) ? yearTo : currentYear;

        if (from > to) {
            return ResponseEntity.badRequest().body(null);
        }

        List<Movie> movies = movieScraperService.getMovies(from, to);
        return ResponseEntity.ok(movies);
    }


    @GetMapping("/export/xlsx")
    public ResponseEntity<InputStreamResource> exportMoviesToXlsx(
            @RequestParam Integer yearFrom,
            @RequestParam Integer yearTo) throws IOException {
        String filename = "movies.xlsx";

        int currentYear = Year.now().getValue();
        int from = (yearFrom != null && yearFrom > 1800) ? yearFrom : 1990;
        int to = (yearTo != null && yearTo <= currentYear) ? yearTo : currentYear;

        if (from > to) {
            return ResponseEntity.badRequest().build();
        }

        InputStreamResource file = new InputStreamResource(exportService.generateMoviesExcel(yearFrom, yearTo));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}