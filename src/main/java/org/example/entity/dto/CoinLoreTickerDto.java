package org.example.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoinLoreTickerDto {
    private String id;
    private String symbol;
    private String name;
    private String nameid;
    private Integer rank;
    private BigDecimal price_usd;
    private String percent_change_24h;
    private String percent_change_1h;
    private String percent_change_7d;
    private BigDecimal market_cap_usd;
    @JsonProperty("volume24")
    private BigDecimal volume24;
    @JsonProperty("volume24a")
    private BigDecimal volume24a;
    private String csupply;
    private String tsupply;
    private String msupply;
}
