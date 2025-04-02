package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Data // Lombok
@NoArgsConstructor
// Добавляем уникальный составной ключ на название и год, чтобы избежать дубликатов
@Table(name = "movies", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"title", "year"})
})
@Getter
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Автоинкремент ID
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer releaseYear; // Год фильма

    @Column(nullable = false)
    private String genres; // Жанры фильма

    // Конструктор для удобства создания
    public Movie(String title, Integer releaseYear, String genres) {
        this.title = title;
        this.releaseYear = releaseYear;
        this.genres = genres;
    }

    // Переопределяем equals и hashCode для корректной работы с коллекциями
    // и для возможной проверки на существование по объекту (хотя мы будем использовать title+year)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        // Считаем фильмы одинаковыми, если совпадают название и год
        return Objects.equals(title, movie.title) &&
                Objects.equals(releaseYear, movie.releaseYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, releaseYear);
    }
}