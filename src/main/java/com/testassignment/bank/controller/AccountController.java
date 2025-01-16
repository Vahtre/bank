package com.testassignment.bank.controller;

import com.testassignment.bank.dto.AccountDTO;
import com.testassignment.bank.dto.CreateAccountRequestDTO;
import com.testassignment.bank.dto.ExchangeRequestDTO;
import com.testassignment.bank.dto.MoneyRequestDTO;
import com.testassignment.bank.entity.Transaction;
import com.testassignment.bank.enums.CurrencyEnum;
import com.testassignment.bank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createAccount(@Valid @RequestBody CreateAccountRequestDTO request) {
        return accountService.createAccount(request.getAccountNumber());
    }

    @PostMapping("/{accountId}/deposit")
    public AccountDTO addMoney(@PathVariable Long accountId, @Valid @RequestBody MoneyRequestDTO request) {
        return accountService.depositMoney(accountId, request.getCurrency(), BigDecimal.valueOf(request.getAmount()));
    }

    @PostMapping("/{accountId}/debit")
    public AccountDTO debitMoney(@PathVariable Long accountId, @Valid @RequestBody MoneyRequestDTO request) {
        return accountService.debitMoney(accountId, request.getCurrency(), BigDecimal.valueOf(request.getAmount()));
    }

    @GetMapping("/{accountId}/balance")
    public Map<CurrencyEnum, BigDecimal> getAccountBalance(@PathVariable Long accountId) {
        return accountService.getAccountBalance(accountId);
    }

    @PostMapping("/{accountId}/exchange")
    public AccountDTO exchangeCurrency(@PathVariable Long accountId, @Valid @RequestBody ExchangeRequestDTO request) {
        return accountService.exchangeCurrency(accountId, request.getFromCurrency(), request.getToCurrency(), BigDecimal.valueOf(request.getAmount()));
    }

    @GetMapping("/{accountId}/transactions")
    public List<Transaction> getAccountTransactionHistory(@PathVariable Long accountId) {
        return accountService.getAccountTransactionHistory(accountId);
    }
}
