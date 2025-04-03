package org.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cryptocurrency {

    @Id
    private String id;

    private String symbol;
    private String name;
    private String nameid;
    private Integer rank;

    @Column(precision = 19, scale = 4)
    private BigDecimal price_usd;

    private String percent_change_24h;
    private String percent_change_1h;
    private String percent_change_7d;

    @Column(precision = 19, scale = 2)
    private BigDecimal market_cap_usd;

    @Column(precision = 19, scale = 4)
    private BigDecimal volume24;

    @Column(precision = 19, scale = 4)
    private BigDecimal volume24a;

    private String csupply;
    private String tsupply;
    private String msupply;

}