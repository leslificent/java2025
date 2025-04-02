package org.example.service;

import org.example.entity.Movie;
import org.example.repository.MovieRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MovieScraperService {

    private static final Logger log = LoggerFactory.getLogger(MovieScraperService.class);
    private static final String BASE_URL = "https://ua.hdrezka.fm/f/cat=352/r-rating_kinopoisk=1;10/r-year=1925;2025/order_by=rating_kinopoisk/order=desc";
    private static final int TOTAL_PAGES = 10; // Скрапинг первых 10 страниц

    // Обновленные селекторы для ua.hdrezka.fm
    private static final String MOVIE_ITEM_SELECTOR = "div.postItem";
    private static final String TITLE_SELECTOR = "div.postItem div.postitem-title a";
    private static final String INFO_SELECTOR = "div.postItem div.postItem-title span.misc";

    private static final Pattern YEAR_PATTERN = Pattern.compile("\\b(\\d{4})\\b");

    @Autowired
    private MovieRepository movieRepository;

    @Transactional
    public List<Movie> scrapeAndSaveMovies(Integer yearFrom, Integer yearTo) {
        log.info("Запуск скрапинга фильмов с {} по {} год с первых {} страниц: {}", yearFrom, yearTo, TOTAL_PAGES, BASE_URL);
        List<Movie> savedOrUpdatedMovies = new ArrayList<>();

        for (int page = 1; page <= TOTAL_PAGES; page++) {
            String pageUrl = BASE_URL + (page > 1 ? "/page/" + page + "/" : "/");
            log.info("Скрапинг страницы: {}", pageUrl);

            try {
                Document doc = Jsoup.connect(pageUrl).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36").timeout(20000).get();                Elements movieItems = doc.select(MOVIE_ITEM_SELECTOR);
                log.info("Найдено {} элементов фильмов на странице {}.", movieItems.size(), page);

                for (Element item : movieItems) {
                    try {
                        String title = item.selectFirst(TITLE_SELECTOR).text();
                        String infoLine = item.selectFirst(INFO_SELECTOR).text();

                        Integer year = parseYear(infoLine);
                        String genres = parseGenres(infoLine);

                        if (title != null && !title.isEmpty() && year != null && genres != null && !genres.isEmpty()) {
                            if (year >= yearFrom && year <= yearTo) {
                                Optional<Movie> existingMovieOpt = movieRepository.findByTitleAndReleaseYear(title, year);

                                if (existingMovieOpt.isPresent()) {
                                    Movie existingMovie = existingMovieOpt.get();
                                    if (!Objects.equals(existingMovie.getGenres(), genres)) {
                                        log.debug("Обновление жанров для фильма: '{}' ({})", title, year);
                                        existingMovie.setGenres(genres);
                                        savedOrUpdatedMovies.add(movieRepository.save(existingMovie));
                                    } else {
                                        savedOrUpdatedMovies.add(existingMovie);
                                    }
                                } else {
                                    log.debug("Найден новый фильм: '{}' ({})", title, year);
                                    Movie newMovie = new Movie(title, year, genres);
                                    savedOrUpdatedMovies.add(movieRepository.save(newMovie));
                                }
                            } else {
                                log.trace("Фильм '{}' ({}) отфильтрован по году.", title, year);
                            }
                        } else {
                            log.warn("Не удалось извлечь название, год или жанры для одного из элементов на странице {}.", page);
                        }
                    } catch (Exception e) {
                        log.error("Ошибка при парсинге элемента фильма на странице {}: {}", page, e.getMessage());
                    }
                }

            } catch (IOException e) {
                log.error("Ошибка при подключении или чтении URL {}: {}", pageUrl, e.getMessage(), e);
            }
        }

        log.info("Скрапинг завершен. Обработано (сохранено/обновлено/найдено) {} фильмов в диапазоне {}-{}",
                savedOrUpdatedMovies.size(), yearFrom, yearTo);
        return savedOrUpdatedMovies;
    }

    /**
     * Получает список фильмов из базы данных, отфильтрованный по году.
     */
    public List<Movie> getMovies(Integer yearFrom, Integer yearTo) {
        log.info("Запрос фильмов из БД за период с {} по {}", yearFrom, yearTo);
        return movieRepository.findByReleaseYearBetweenOrderByReleaseYearDescTitleAsc(yearFrom, yearTo);
    }


    // --- Вспомогательные методы ---

    private Integer parseYear(String infoLine) {
        if (infoLine == null || infoLine.isEmpty()) {
            return null;
        }
        Matcher matcher = YEAR_PATTERN.matcher(infoLine);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("Не удалось спарсить год из найденной строки '{}'", matcher.group(1));
                return null;
            }
        }
        return null;
    }

    private String parseGenres(String infoLine) {
        if (infoLine == null || infoLine.isEmpty()) {
            return null;
        }
        Matcher matcher = YEAR_PATTERN.matcher(infoLine);
        if (matcher.find()) {
            int yearEndIndex = matcher.end();
            // Extract the substring after the year and the following comma and space (if present)
            String genres = infoLine.substring(yearEndIndex).trim();
            if (genres.startsWith(",")) {
                genres = genres.substring(1).trim();
            }
            return genres;
        }
        return null;
    }
}