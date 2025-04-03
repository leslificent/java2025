package org.example.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinLoreTickersResponseDto {
    private List<CoinLoreTickerDto> data;
}