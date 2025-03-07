package org.example.controller;

import org.example.entity.Bank;
import org.example.entity.Listing;
import org.example.service.WebScraperService;
import org.example.service.BankApiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
public class ListingController {
    private final WebScraperService webScraperService;
    private final BankApiService bankApiService;

    public ListingController(WebScraperService webScraperService, BankApiService bankApiService) {
        this.webScraperService = webScraperService;
        this.bankApiService = bankApiService;
    }
    @GetMapping("/load-from-api")
    public ResponseEntity<List<Bank>> loadBanksFromApi(@RequestParam("year") String year) {
        try {
            // Получаем список банков для указанного года
            List<Bank> banks = bankApiService.getBanksFromApi(year);
            return ResponseEntity.ok(banks);
        } catch (Exception e) {
            // Если произошла ошибка, возвращаем 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/scrape")
    public String scrapeListings() {
        webScraperService.scrapeData();
        return "Scraping started!";
    }

    @GetMapping
    public List<Listing> getAllListings() {
        return webScraperService.getAllListings();
    }
}
