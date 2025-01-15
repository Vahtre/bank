package com.testassignment.bank.dto;

import java.time.LocalDateTime;

public class TransactionDTO {

    private Long id;
    private Long accountId;
    private String currency;
    private Double amount;
    private LocalDateTime timestamp;
}
