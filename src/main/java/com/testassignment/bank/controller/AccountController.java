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

    // Constructor injection for AccountService
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Endpoint to create a new account.
     * @param request DTO containing the account number.
     * @return AccountDTO containing the details of the created account.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createAccount(@Valid @RequestBody CreateAccountRequestDTO request) {
        return accountService.createAccount(request.getAccountNumber());
    }

    /**
     * Endpoint to deposit money into an account.
     * @param accountId ID of the account.
     * @param request DTO containing the currency and amount to deposit.
     * @return AccountDTO containing the updated account details.
     */
    @PostMapping("/{accountId}/deposit")
    public AccountDTO addMoney(@PathVariable Long accountId, @Valid @RequestBody MoneyRequestDTO request) {
        return accountService.depositMoney(accountId, request.getCurrency(), BigDecimal.valueOf(request.getAmount()));
    }

    /**
     * Endpoint to debit money from an account.
     * @param accountId ID of the account.
     * @param request DTO containing the currency and amount to debit.
     * @return AccountDTO containing the updated account details.
     */
    @PostMapping("/{accountId}/debit")
    public AccountDTO debitMoney(@PathVariable Long accountId, @Valid @RequestBody MoneyRequestDTO request) {
        return accountService.debitMoney(accountId, request.getCurrency(), BigDecimal.valueOf(request.getAmount()));
    }

    /**
     * Endpoint to get the balance of an account.
     * @param accountId ID of the account.
     * @return Map containing the balances of the account in different currencies.
     */
    @GetMapping("/{accountId}/balance")
    public Map<CurrencyEnum, BigDecimal> getAccountBalance(@PathVariable Long accountId) {
        return accountService.getAccountBalance(accountId);
    }

    /**
     * Endpoint to exchange currency in an account.
     * @param accountId ID of the account.
     * @param request DTO containing the currencies and amount to exchange.
     * @return AccountDTO containing the updated account details.
     */
    @PostMapping("/{accountId}/exchange")
    public AccountDTO exchangeCurrency(@PathVariable Long accountId, @Valid @RequestBody ExchangeRequestDTO request) {
        return accountService.exchangeCurrency(accountId, request.getFromCurrency(), request.getToCurrency(), BigDecimal.valueOf(request.getAmount()));
    }

    /**
     * Endpoint to get the transaction history of an account.
     * @param accountId ID of the account.
     * @return List of transactions for the account.
     */
    @GetMapping("/{accountId}/transactions")
    public List<Transaction> getAccountTransactionHistory(@PathVariable Long accountId) {
        return accountService.getAccountTransactionHistory(accountId);
    }
}
