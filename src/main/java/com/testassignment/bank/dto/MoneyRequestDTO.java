package com.testassignment.bank.dto;

import com.testassignment.bank.enums.CurrencyEnum;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MoneyRequestDTO {

    @NotNull(message = "Currency is mandatory")
    private CurrencyEnum currency;

    @NotNull(message = "Amount is mandatory")
    @Positive(message = "Amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "Amount cannot have more than 2 decimal places")
    private Double amount;
}