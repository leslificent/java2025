package org.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Bank {

    @Id
    private String registration_number;
    private String name;
    private String description;
    private String categoryCode;
    private String unit;
    private int level;
    private String date;
    private double value;
    private String addition;
}
