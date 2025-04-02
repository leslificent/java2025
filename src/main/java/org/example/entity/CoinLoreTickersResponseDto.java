package org.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinLoreTickersResponseDto {
    private List<CoinLoreTickerDto> data;
    // Могут быть и другие поля в ответе API (например, "info")
}