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
    private CryptocurrencyRepository cryptocurrencyRepository;
    @Autowired
    private MovieRepository movieRepository;
    private final static String[] CRYPTO_HEADERS = {
            "ID", "Symbol", "Name", "Rank", "Price USD", "% Change 1h", "% Change 24h", "% Change 7d",
            "Market Cap USD", "Volume 24h USD", "Volume 24h Native",
            "Circulating Supply", "Total Supply", "Max Supply"
    };


    public ByteArrayInputStream generateExcelForCrypto() throws IOException {
        log.info("Запит даних для генерації Excel...");
        List<Cryptocurrency> cryptos = cryptocurrencyRepository.findAll();
        log.info("Знайдено {} записів для експорту в Excel.", cryptos.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Cryptocurrencies");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < CRYPTO_HEADERS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(CRYPTO_HEADERS[col]);
                cell.setCellStyle(headerCellStyle);
            }

            CellStyle numberCellStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            numberCellStyle.setDataFormat(format.getFormat("#,##0.0000"));

            int rowIdx = 1;
            for (Cryptocurrency crypto : cryptos) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(crypto.getId());
                row.createCell(1).setCellValue(crypto.getSymbol());
                row.createCell(2).setCellValue(crypto.getName());
                if (crypto.getRank() != null) row.createCell(3).setCellValue(crypto.getRank());
                else row.createCell(3).setBlank();
                setCellValue(row.createCell(4), crypto.getPrice_usd(), numberCellStyle);
                row.createCell(5).setCellValue(crypto.getPercent_change_1h());
                row.createCell(6).setCellValue(crypto.getPercent_change_24h());
                row.createCell(7).setCellValue(crypto.getPercent_change_7d());
                setCellValue(row.createCell(8), crypto.getMarket_cap_usd(), numberCellStyle);
                setCellValue(row.createCell(9), crypto.getVolume24(), numberCellStyle);
                setCellValue(row.createCell(10), crypto.getVolume24a(), numberCellStyle);

                row.createCell(11).setCellValue(crypto.getCsupply());
                row.createCell(12).setCellValue(crypto.getTsupply());
                row.createCell(13).setCellValue(crypto.getMsupply());

            }

            for (int i = 0; i < CRYPTO_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("Excel дані успішно сформовані.");
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Помилка при генерації Excel файла: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void setCellValue(Cell cell, BigDecimal value, CellStyle style) {
        if (value != null) {
            cell.setCellValue(value.doubleValue());
            if (style != null) {
                cell.setCellStyle(style);
            }
        } else {
            cell.setBlank();
        }
    }

    private final static String[] MOVIE_HEADERS = {"ID", "Title", "Year", "Genres"};

    public ByteArrayInputStream generateMoviesExcel(Integer yearFrom, Integer yearTo) throws IOException {
        log.info("Запит даних для генерації Excel фільмів за період {}-{}", yearFrom, yearTo);
        List<Movie> movies = movieRepository.findByReleaseYearBetweenOrderByReleaseYearDescTitleAsc(yearFrom, yearTo);
        log.info("Знайдено {} фільмів для експорту в Excel.", movies.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Movies");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < MOVIE_HEADERS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(MOVIE_HEADERS[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (Movie movie : movies) {
                Row row = sheet.createRow(rowIdx++);

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

            for (int i = 0; i < MOVIE_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("Excel дані для фільмів успішно сформовані.");
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Помилка при генерації Excel файла фільмів: {}", e.getMessage(), e);
            throw e;
        }
    }

}