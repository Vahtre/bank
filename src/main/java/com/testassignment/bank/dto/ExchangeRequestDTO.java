package com.testassignment.bank.dto;

import com.testassignment.bank.enums.CurrencyEnum;
import com.testassignment.bank.validation.ValidCurrencyEnum;
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
}
