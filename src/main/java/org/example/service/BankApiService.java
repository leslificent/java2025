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
        String date = year + "0101";
        String url = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("date", date)
                .queryParam("period", "m")
                .toUriString();

        ResponseEntity<BankApiResponse[]> response = restTemplate.getForEntity(url + "&json", BankApiResponse[].class);

        if (response.getStatusCode() == HttpStatus.OK) {
            List<BankApiResponse> bankApiResponses = Arrays.asList(response.getBody());

            List<Bank> banks = new ArrayList<>();
            for (BankApiResponse apiResponse : bankApiResponses) {
                Bank bank = new Bank();
                bank.setRegistration_number(apiResponse.getId_api());
                bank.setName(apiResponse.getTxt());
                bank.setDescription(apiResponse.getTxten());
                bank.setCategoryCode(apiResponse.getId_api());
                bank.setUnit(apiResponse.getFreq());
                bank.setLevel(apiResponse.getLeveli());
                bank.setDate(apiResponse.getDt());
                bank.setValue(apiResponse.getValue());
                bank.setAddition(apiResponse.getTzep());

                banks.add(bank);
            }

            return bankRepository.saveAll(banks);
        }

        throw new RuntimeException("Can't get data from API");
    }
}