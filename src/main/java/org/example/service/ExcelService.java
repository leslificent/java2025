package org.example.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.entity.Listing;
import org.example.repository.ListingRepository;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {
    private final ListingRepository listingRepository;

    public ExcelService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public String generateExcelReport() {
        List<Listing> listings = listingRepository.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Listings");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Title");
        headerRow.createCell(1).setCellValue("Price");
        headerRow.createCell(2).setCellValue("URL");

        int rowNum = 1;
        for (Listing listing : listings) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(listing.getTitle());
            row.createCell(1).setCellValue(listing.getPrice());
            row.createCell(2).setCellValue(listing.getUrl());
        }

        try (FileOutputStream fileOut = new FileOutputStream("listings.xlsx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Excel report generated!";
    }
}
