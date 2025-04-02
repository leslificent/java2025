package org.example.service;

import org.example.entity.CoinLoreTickerDto;
import org.example.entity.CoinLoreTickersResponseDto;
import org.example.entity.Cryptocurrency;
import org.example.repository.CryptocurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Важно для saveAll
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CoinLoreApiService {

    private static final Logger log = LoggerFactory.getLogger(CoinLoreApiService.class);

    private final WebClient webClient;
    private final CryptocurrencyRepository repository;
    private final String tickersEndpoint;

    @Autowired
    public CoinLoreApiService(WebClient.Builder webClientBuilder,
                              CryptocurrencyRepository repository,
                              @Value("${coinlore.api.baseurl}") String baseUrl,
                              @Value("${coinlore.api.tickers_endpoint:/tickers/}") String tickersEndpoint) { // Добавлено значение по умолчанию
        // Настройка WebClient с базовым URL
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.repository = repository;
        // Убедимся, что эндпоинт начинается со слеша, если он не пустой
        this.tickersEndpoint = (tickersEndpoint != null && !tickersEndpoint.isEmpty() && !tickersEndpoint.startsWith("/"))
                ? "/" + tickersEndpoint
                : tickersEndpoint;
        log.info("CoinLore API Base URL: {}, Tickers Endpoint: {}", baseUrl, this.tickersEndpoint);
    }

    /**
     * Получает данные о тикерах с CoinLore API и сохраняет их в базу данных.
     * Обрабатывает ответ API, маппит DTO в сущности и выполняет сохранение.
     * Использует limit=100, так как бесплатный API обычно возвращает не больше.
     * Для получения ВСЕХ тикеров может потребоваться платный API и/или пагинация.
     *
     * @return Mono со списком сохраненных (или обновленных) сущностей Cryptocurrency.
     */
    @Transactional // Убедимся, что saveAll выполняется в транзакции
    public Mono<List<Cryptocurrency>> fetchAndSaveTickers() {
        log.info("Запрос данных с CoinLore API эндпоинта: {}", tickersEndpoint);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(tickersEndpoint)
                        // Бесплатный API возвращает 100 по умолчанию, можно явно указать
                        .queryParam("start", "0")
                        .queryParam("limit", "100") // Максимум для бесплатного API
                        .build())
                .retrieve() // Начинаем получение ответа
                .bodyToMono(CoinLoreTickersResponseDto.class) // Преобразуем тело ответа в Mono<CoinLoreTickersResponseDto>
                .doOnError(WebClientResponseException.class, ex ->
                        // Логируем ошибки HTTP-запроса
                        log.error("Ошибка HTTP при запросе к CoinLore API: Status={}, Body={}", ex.getStatusCode(), ex.getResponseBodyAsString())
                )
                .doOnError(Exception.class, ex ->
                        // Логируем другие ошибки (например, десериализации)
                        log.error("Ошибка при обработке ответа от CoinLore API: {}", ex.getMessage(), ex)
                )
                .map(CoinLoreTickersResponseDto::getData) // Извлекаем список DTO из ответа
                .filter(Objects::nonNull) // Убедимся, что список не null
                .flatMapMany(Flux::fromIterable) // Преобразуем список DTO в поток Flux<CoinLoreTickerDto>
                .map(this::mapDtoToEntity) // Маппим каждый DTO в сущность Cryptocurrency
                .collectList() // Собираем сущности в список Mono<List<Cryptocurrency>>
                .flatMap(entities -> {
                    if (entities.isEmpty()) {
                        log.warn("Не получено данных от CoinLore API или не удалось их смаппить.");
                        return Mono.just(Collections.<Cryptocurrency>emptyList()); // Возвращаем пустой список, если ничего нет
                    }
                    log.info("Получено {} тикеров для сохранения/обновления.", entities.size());
                    // Сохраняем все сущности в БД. saveAll выполняет INSERT или UPDATE.
                    List<Cryptocurrency> savedEntities = repository.saveAll(entities);
                    log.info("Успешно сохранено/обновлено {} записей в БД.", savedEntities.size());
                    return Mono.just(savedEntities); // Возвращаем список сохраненных сущностей
                })
                .onErrorResume(e -> {
                    // Обработка ошибок на уровне всего процесса
                    log.error("Не удалось получить и сохранить данные: {}", e.getMessage(), e);
                    // Возвращаем пустой список в случае ошибки, чтобы не ломать цепочку вызовов
                    return Mono.just(Collections.emptyList());
                });
    }

    /**
     * Возвращает все записи о криптовалютах из локальной базы данных.
     * @return Список всех сущностей Cryptocurrency.
     */
    public List<Cryptocurrency> getAllCryptocurrencies() {
        log.info("Запрос всех записей из CryptocurrencyRepository.");
        List<Cryptocurrency> cryptos = repository.findAll();
        log.info("Найдено {} записей в БД.", cryptos.size());
        return cryptos;
    }

    /**
     * Вспомогательный метод для преобразования DTO из API в сущность для БД.
     * @param dto Объект CoinLoreTickerDto, полученный из API.
     * @return Объект Cryptocurrency, готовый к сохранению.
     */
    private Cryptocurrency mapDtoToEntity(CoinLoreTickerDto dto) {
        if (dto == null) {
            return null; // Или бросить исключение
        }
        Cryptocurrency entity = new Cryptocurrency();
        // Используем ID из API как первичный ключ
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
        entity.setVolume24a(dto.getVolume24a()); // volume24 native
        entity.setCsupply(dto.getCsupply()); // Circulating supply
        entity.setTsupply(dto.getTsupply()); // Total supply
        entity.setMsupply(dto.getMsupply()); // Max supply

        // Добавьте маппинг для других полей, если вы их добавили в Entity и DTO
        return entity;
    }
}