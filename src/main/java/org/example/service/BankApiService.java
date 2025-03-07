package org.example.service;

import org.example.entity.Bank;
import org.example.entity.BankApiResponse;
import org.example.repository.BankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class BankApiService {


    private final String API_URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/basindbank";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BankRepository bankRepository;

    public List<Bank> getBanksFromApi(String year) {
        // Формируем запрос с выбором года (год в формате YYYY0101)
        String date = year + "0101"; // Формат даты: YYYY0101


        String url = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("date", date)
                .queryParam("period", "m")
                .toUriString();

        // Отправляем запрос
        ResponseEntity<BankApiResponse[]> response = restTemplate.getForEntity(url + "&json", BankApiResponse[].class);

        // Проверяем, что запрос успешный
        // Проверяем, что запрос успешный
        if (response.getStatusCode() == HttpStatus.OK) {
            List<BankApiResponse> bankApiResponses = Arrays.asList(response.getBody());

            // Преобразуем данные в сущности и сохраняем в базу данных
            List<Bank> banks = new ArrayList<>();
            for (BankApiResponse apiResponse : bankApiResponses) {
                Bank bank = new Bank();
                bank.setRegnum(apiResponse.getId_api());  // Используем id_api как регномер
                bank.setName(apiResponse.getTxt());     // Используем описание на украинском
                bank.setDescription(apiResponse.getTxten()); // Описание на английском
                bank.setCategoryCode(apiResponse.getId_api()); // Идентификатор записи
                bank.setUnit(apiResponse.getFreq());   // Частота
                bank.setLevel(apiResponse.getLeveli()); // Уровень записи
                bank.setDate(apiResponse.getDt());     // Дата
                bank.setValue(apiResponse.getValue()); // Значение (например, 30551.5611)
                bank.setTzep(apiResponse.getTzep());   // Дополнительное поле

                banks.add(bank);
            }

            // Сохраняем данные в базу и возвращаем сохранённые объекты
            return bankRepository.saveAll(banks);
        }

        // Если запрос не удался, выбрасываем исключение
        throw new RuntimeException("Не удалось получить данные из API");
    }
}