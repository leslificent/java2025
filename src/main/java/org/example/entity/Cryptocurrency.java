package org.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
// import jakarta.persistence.GeneratedValue; // Если ID генерируется базой
// import jakarta.persistence.GenerationType;
import lombok.Data; // Если используете Lombok
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data // Lombok: генерирует геттеры, сеттеры, toString, equals, hashCode
@NoArgsConstructor // Lombok: конструктор без аргументов
@AllArgsConstructor // Lombok: конструктор со всеми аргументами
public class Cryptocurrency {

    @Id // Используем ID из CoinLore как первичный ключ
    private String id;

    private String symbol;
    private String name;
    private String nameid;
    private Integer rank;

    @Column(precision = 19, scale = 4) // Пример для точного хранения цены
    private BigDecimal price_usd;

    private String percent_change_24h;
    private String percent_change_1h;
    private String percent_change_7d;

    @Column(precision = 19, scale = 2)
    private BigDecimal market_cap_usd;

    @Column(precision = 19, scale = 4)
    private BigDecimal volume24;

    @Column(precision = 19, scale = 4)
    private BigDecimal volume24a; // volume24 native

    private String csupply; // Circulating supply
    private String tsupply; // Total supply
    private String msupply; // Max supply

    // Добавьте другие поля по необходимости
}