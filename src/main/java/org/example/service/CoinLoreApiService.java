package org.example.service;

import org.example.entity.Cryptocurrency;
import org.example.entity.dto.CoinLoreTickerDto;
import org.example.entity.dto.CoinLoreTickersResponseDto;
import org.example.repository.CryptocurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class CoinLoreApiService {

    private static final Logger log = LoggerFactory.getLogger(CoinLoreApiService.class);

    private final WebClient webClient;
    private final CryptocurrencyRepository repository;
    private final String tickersEndpoint;

    @Autowired
    public CoinLoreApiService(Builder webClientBuilder,
                              CryptocurrencyRepository repository,
                              @Value("${coinlore.api.baseurl}") String baseUrl,
                              @Value("${coinlore.api.tickers_endpoint:/tickers/}") String tickersEndpoint) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.repository = repository;
        this.tickersEndpoint = (tickersEndpoint != null && !tickersEndpoint.isEmpty() && !tickersEndpoint.startsWith("/"))
                ? "/" + tickersEndpoint
                : tickersEndpoint;
        log.info("CoinLore API Base URL: {}, Tickers Endpoint: {}", baseUrl, this.tickersEndpoint);
    }


    @Transactional
    public Mono<List<Cryptocurrency>> fetchAndSaveTickers() {
        log.info("Запит даних з CoinLore API ендпоінта: {}", tickersEndpoint);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(tickersEndpoint)
                        .queryParam("start", "0")
                        .queryParam("limit", "100")
                        .build())
                .retrieve()
                .bodyToMono(CoinLoreTickersResponseDto.class)
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("Помилка HTTP при запиті до CoinLore API: Status={}, Body={}", ex.getStatusCode(), ex.getResponseBodyAsString())
                )
                .doOnError(Exception.class, ex ->
                        log.error("Помилка при обробці відповіді від CoinLore API: {}", ex.getMessage(), ex)
                )
                .map(CoinLoreTickersResponseDto::getData)
                .filter(Objects::nonNull)
                .flatMapMany(Flux::fromIterable)
                .map(this::mapDtoToEntity)
                .collectList()
                .flatMap(entities -> {
                    if (entities.isEmpty()) {
                        log.warn("Не отримано даних від CoinLore API чи не вдалося їх смаппити.");
                        return Mono.just(Collections.<Cryptocurrency>emptyList());
                    }
                    log.info("Отримано {} тикерів для забереження/оновлення.", entities.size());
                    List<Cryptocurrency> savedEntities = repository.saveAll(entities);
                    log.info("Успішно збережено/оновлено {} записів у БД.", savedEntities.size());
                    return Mono.just(savedEntities);
                })
                .onErrorResume(e -> {
                    log.error("Не вдалося отримати та зберегти дані: {}", e.getMessage(), e);
                    return Mono.just(Collections.emptyList());
                });
    }

    private Cryptocurrency mapDtoToEntity(CoinLoreTickerDto dto) {
        if (dto == null) {
            return null;
        }
        Cryptocurrency entity = new Cryptocurrency();
        entity.setId(dto.getId());
        entity.setSymbol(dto.getSymbol());
        entity.setName(dto.getName());
        entity.setNameid(dto.getNameid());
        entity.setRank(dto.getRank());
        entity.setPrice_usd(dto.getPrice_usd());
        entity.setPercent_change_1h(dto.getPercent_change_1h());
        entity.setPercent_change_24h(dto.getPercent_change_24h());
        entity.setPercent_change_7d(dto.getPercent_change_7d());
        entity.setMarket_cap_usd(dto.getMarket_cap_usd());
        entity.setVolume24(dto.getVolume24());
        entity.setVolume24a(dto.getVolume24a());
        entity.setCsupply(dto.getCsupply());
        entity.setTsupply(dto.getTsupply());
        entity.setMsupply(dto.getMsupply());

        return entity;
    }
}