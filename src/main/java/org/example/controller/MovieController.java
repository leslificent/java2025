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
@RequestMapping("/api/movies") // Базовый путь для API фильмов
public class MovieController {

    @Autowired
    private MovieScraperService movieScraperService;

    @Autowired
    private ExportService exportService; // Инжектим сервис экспорта

    /**
     * Запускает процесс скрапинга и сохранения фильмов.
     * Принимает параметры года "с" и "по".
     */
    @PostMapping("/scrape") // Используем POST для действия, изменяющего состояние
    public ResponseEntity<List<Movie>> scrapeMovies(
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo) {

        // Устанавливаем значения по умолчанию, если параметры не переданы
        int currentYear = Year.now().getValue();
        int from = (yearFrom != null && yearFrom > 1800) ? yearFrom : 1990; // Дефолтное начало
        int to = (yearTo != null && yearTo <= currentYear) ? yearTo : currentYear; // Дефолтный конец

        if (from > to) { // Простая валидация
            return ResponseEntity.badRequest().body(null); // Возвращаем ошибку 400
        }

        List<Movie> processedMovies = movieScraperService.scrapeAndSaveMovies(from, to);
        // Возвращаем список обработанных (сохраненных/обновленных) фильмов
        return ResponseEntity.ok(processedMovies);
    }

    /**
     * Получает список фильмов из БД, отфильтрованный по годам.
     */
    @GetMapping("/load") // Or any other path you prefer, like /get
    public ResponseEntity<List<Movie>> loadMovies(
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo) {

        int currentYear = Year.now().getValue();
        int from = (yearFrom != null && yearFrom > 1800) ? yearFrom : 1990;
        int to = (yearTo != null && yearTo <= currentYear) ? yearTo : currentYear;

        if (from > to) {
            return ResponseEntity.badRequest().body(null);
        }

        List<Movie> movies = movieScraperService.getMovies(from, to);
        return ResponseEntity.ok(movies);
    }

    /**
     * Экспортирует отфильтрованные фильмы в CSV.
     */
    @GetMapping("/export/csv")
    public ResponseEntity<InputStreamResource> exportMoviesToCsv(
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo) throws IOException {
        String filename = "movies.xlsx";

        int currentYear = Year.now().getValue();
        int from = (yearFrom != null && yearFrom > 1800) ? yearFrom : 1990;
        int to = (yearTo != null && yearTo <= currentYear) ? yearTo : currentYear;

        if (from > to) {
            // Можно вернуть пустой файл или ошибку
            return ResponseEntity.badRequest().build();
        }

        InputStreamResource file = new InputStreamResource(exportService.generateMoviesExcel(yearFrom, yearTo));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}