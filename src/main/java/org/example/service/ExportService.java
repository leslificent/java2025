package org.example.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entity.Cryptocurrency;
import org.example.entity.Movie;
import org.example.repository.CryptocurrencyRepository;
import org.example.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    @Autowired
    private CryptocurrencyRepository repository;
    @Autowired // Инжектируем репозиторий фильмов
    private MovieRepository movieRepository;
    // Определяем заголовки один раз. Убедитесь, что порядок соответствует данным ниже.
    private final String[] HEADERS = {
            "ID", "Symbol", "Name", "Rank", "Price USD", "% Change 1h", "% Change 24h", "% Change 7d",
            "Market Cap USD", "Volume 24h USD", "Volume 24h Native",
            "Circulating Supply", "Total Supply", "Max Supply"
            // Добавьте другие заголовки, если нужно
    };


    /**
     * Генерирует Excel (.xlsx) файл со всеми криптовалютами из базы данных.
     *
     * @return ByteArrayInputStream с данными Excel файла.
     * @throws IOException в случае ошибки ввода/вывода.
     */
    public ByteArrayInputStream generateExcel() throws IOException {
        log.info("Запрос данных для генерации Excel...");
        List<Cryptocurrency> cryptos = repository.findAll();
        log.info("Найдено {} записей для экспорта в Excel.", cryptos.size());

        // Используем try-with-resources для автоматического закрытия Workbook и OutputStream
        try (Workbook workbook = new XSSFWorkbook(); // Создаем новую книгу Excel (.xlsx)
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Cryptocurrencies"); // Создаем лист

            // Стиль для заголовка (опционально)
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Создание строки заголовка
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
                cell.setCellStyle(headerCellStyle); // Применяем стиль
            }

            // Стиль для числовых ячеек (опционально, для форматирования)
            CellStyle numberCellStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            // Пример формата: #,##0.0000 (4 знака после запятой, разделитель тысяч)
            numberCellStyle.setDataFormat(format.getFormat("#,##0.0000"));

            // Заполнение данными
            int rowIdx = 1; // Начинаем со второй строки (индекс 1)
            for (Cryptocurrency crypto : cryptos) {
                Row row = sheet.createRow(rowIdx++);

                // Заполняем ячейки. Важно использовать правильный тип данных!
                row.createCell(0).setCellValue(crypto.getId());
                row.createCell(1).setCellValue(crypto.getSymbol());
                row.createCell(2).setCellValue(crypto.getName());
                if (crypto.getRank() != null) row.createCell(3).setCellValue(crypto.getRank());
                else row.createCell(3).setBlank();
                setCellValue(row.createCell(4), crypto.getPrice_usd(), numberCellStyle); // Цена
                row.createCell(5).setCellValue(crypto.getPercent_change_1h());
                row.createCell(6).setCellValue(crypto.getPercent_change_24h());
                row.createCell(7).setCellValue(crypto.getPercent_change_7d());
                // Капитализация (можно использовать другой формат)
                setCellValue(row.createCell(8), crypto.getMarket_cap_usd(), numberCellStyle);
                // Объемы
                setCellValue(row.createCell(9), crypto.getVolume24(), numberCellStyle);
                setCellValue(row.createCell(10), crypto.getVolume24a(), numberCellStyle);

                row.createCell(11).setCellValue(crypto.getCsupply());
                row.createCell(12).setCellValue(crypto.getTsupply());
                row.createCell(13).setCellValue(crypto.getMsupply());

                // Добавьте ячейки для остальных полей...
            }

            // Автоподбор ширины колонок (может быть медленным для больших файлов)
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out); // Записываем книгу в поток байт
            log.info("Excel данные успешно сформированы.");
            return new ByteArrayInputStream(out.toByteArray()); // Возвращаем поток
        } catch (IOException e) {
            log.error("Ошибка при генерации Excel файла: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Вспомогательный метод для безопасного получения строкового значения объекта
    private String getStringValue(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    // Вспомогательный метод для безопасного получения строки из BigDecimal
    private String getBigDecimalString(BigDecimal bd) {
        // Используем toPlainString() чтобы избежать научной нотации в CSV
        return bd != null ? bd.toPlainString() : "";
    }

    // Вспомогательный метод для установки значения ячейки BigDecimal с проверкой на null
    private void setCellValue(Cell cell, BigDecimal value, CellStyle style) {
        if (value != null) {
            cell.setCellValue(value.doubleValue()); // Excel работает с double
            if (style != null) {
                cell.setCellStyle(style);
            }
        } else {
            cell.setBlank(); // Оставляем ячейку пустой
        }
    }

    private final String[] MOVIE_HEADERS = {"ID", "Title", "Year", "Genres"};

    /**
     * Генерирует Excel (.xlsx) файл с фильмами из базы данных по диапазону лет.
     *
     * @param yearFrom Начальный год
     * @param yearTo   Конечный год
     * @return ByteArrayInputStream с данными Excel файла.
     * @throws IOException в случае ошибки ввода/вывода.
     */
    public ByteArrayInputStream generateMoviesExcel(Integer yearFrom, Integer yearTo) throws IOException {
        log.info("Запрос данных для генерации Excel фильмов за период {}-{}", yearFrom, yearTo);
        // Получаем отфильтрованные данные из репозитория
        List<Movie> movies = movieRepository.findByReleaseYearBetweenOrderByReleaseYearDescTitleAsc(yearFrom, yearTo);
        log.info("Найдено {} фильмов для экспорта в Excel.", movies.size());

        // Используем try-with-resources для автоматического закрытия Workbook и OutputStream
        try (Workbook workbook = new XSSFWorkbook(); // Создаем новую книгу Excel (.xlsx)
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Movies"); // Создаем лист

            // Стиль для заголовка (опционально)
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Создание строки заголовка
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < MOVIE_HEADERS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(MOVIE_HEADERS[col]);
                cell.setCellStyle(headerCellStyle); // Применяем стиль
            }

            // Заполнение данными
            int rowIdx = 1; // Начинаем со второй строки (индекс 1)
            for (Movie movie : movies) {
                Row row = sheet.createRow(rowIdx++);

                // Заполняем ячейки. Важно использовать правильный тип данных!
                row.createCell(0).setCellValue(movie.getId());
                row.createCell(1).setCellValue(movie.getTitle());
                if (movie.getReleaseYear() != null) {
                    row.createCell(2).setCellValue(movie.getReleaseYear());
                } else {
                    row.createCell(2).setBlank();
                }
                if (movie.getGenres() != null) {
                    row.createCell(3).setCellValue(movie.getGenres());
                } else {
                    row.createCell(3).setBlank();
                }
            }

            // Автоподбор ширины колонок (может быть медленным для больших файлов)
            for (int i = 0; i < MOVIE_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out); // Записываем книгу в поток байт
            log.info("Excel данные для фильмов успешно сформированы.");
            return new ByteArrayInputStream(out.toByteArray()); // Возвращаем поток
        } catch (IOException e) {
            log.error("Ошибка при генерации Excel файла фильмов: {}", e.getMessage(), e);
            throw e;
        }
    }

}