package org.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Bank {

    @Id
    private String regnum;       // Регистрационный номер или идентификатор (например, BS3_AmountsDueCorpNonres)
    private String name;         // Название записи (например, "Кошти суб'єктів господарювання-нерезидентів")
    private String description;  // Описание на английском языке
    private String categoryCode; // Идентификатор записи (например, "BS3_AmountsDueCorpNonres")
    private String unit;         // Частота (например, "M")
    private int level;           // Уровень записи (например, 4)
    private String date;         // Дата (например, 20160101)
    private double value;        // Значение (например, 30551.5611)
    private String tzep;         // Дополнительное поле (например, T070_7)
}
