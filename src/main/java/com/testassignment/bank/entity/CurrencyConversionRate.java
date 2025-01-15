package com.testassignment.bank.entity;

import com.testassignment.bank.enums.CurrencyEnum;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class CurrencyConversionRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CurrencyEnum fromCurrency;

    @Enumerated(EnumType.STRING)
    private CurrencyEnum toCurrency;

    private BigDecimal rate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CurrencyEnum getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(CurrencyEnum fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public CurrencyEnum getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(CurrencyEnum toCurrency) {
        this.toCurrency = toCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
