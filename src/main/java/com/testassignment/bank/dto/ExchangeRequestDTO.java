package com.testassignment.bank.dto;

import com.testassignment.bank.enums.CurrencyEnum;
import com.testassignment.bank.validation.ValidCurrencyEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExchangeRequestDTO {

    @NotNull(message = "From currency is mandatory")
    @ValidCurrencyEnum
    private CurrencyEnum fromCurrency;

    @NotNull(message = "To currency is mandatory")
    @ValidCurrencyEnum
    private CurrencyEnum toCurrency;

    @NotNull(message = "Amount is mandatory")
    private Double amount;

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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
