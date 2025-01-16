package com.testassignment.bank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAccountRequestDTO {

    @NotBlank(message = "Account number is mandatory")
    private String accountNumber;
}
