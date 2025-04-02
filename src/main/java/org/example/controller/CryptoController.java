package org.example.controller;

import org.example.entity.Cryptocurrency;
import org.example.service.CoinLoreApiService;
import org.example.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    @Autowired
    private CoinLoreApiService coinLoreApiService;

    @Autowired
    private ExportService exportService;

    @GetMapping("/fetch")
    public Mono<ResponseEntity<List<Cryptocurrency>>> fetchAndSave() {
        return coinLoreApiService.fetchAndSaveTickers()
                .map(savedList -> ResponseEntity.ok(savedList)) // Если успешно, возвращаем 200 OK и список
                .defaultIfEmpty(ResponseEntity.notFound().build()) // Если Mono пустой (редко в этом случае)
                .onErrorResume(e -> { // Базовая обработка ошибок
                    System.err.println("Error fetching data: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(500).build());
                });
    }

    // Эндпоинт для получения всех данных из БД
    @GetMapping("/all")
    public ResponseEntity<List<Cryptocurrency>> getAll() {
        List<Cryptocurrency> cryptos = coinLoreApiService.getAllCryptocurrencies();
        return ResponseEntity.ok(cryptos);
    }


    // Эндпоинт для скачивания Excel файла
    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportToExcel() throws IOException {
        String filename = "cryptocurrencies.xlsx";
        InputStreamResource file = new InputStreamResource(exportService.generateExcel());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}