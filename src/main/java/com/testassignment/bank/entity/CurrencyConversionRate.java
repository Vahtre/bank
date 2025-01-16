package com.testassignment.bank.entity;

import com.testassignment.bank.enums.CurrencyEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class CurrencyConversionRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CurrencyEnum fromCurrency;

    @Enumerated(EnumType.STRING)
    private CurrencyEnum toCurrency;

    private BigDecimal rate;
}
