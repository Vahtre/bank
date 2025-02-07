package com.testassignment.bank.service;

import com.testassignment.bank.dao.AccountDAO;
import com.testassignment.bank.dto.AccountDTO;
import com.testassignment.bank.entity.Account;
import com.testassignment.bank.entity.Transaction;
import com.testassignment.bank.enums.CurrencyEnum;
import com.testassignment.bank.enums.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Transactional
public class AccountService {

    private final AccountDAO accountDAO;
    private final TransactionService transactionService;
    private final CurrencyConversionService currencyConversionService;
    private final RestTemplate restTemplate = new RestTemplate();

    public AccountService(AccountDAO accountDAO, TransactionService transactionService, CurrencyConversionService currencyConversionService) {
        this.accountDAO = accountDAO;
        this.transactionService = transactionService;
        this.currencyConversionService = currencyConversionService;
    }

    public AccountDTO createAccount(String accountNumber) {
        Account account = new Account();
        account.setAccountNumber(accountNumber);

        // Initialize account balances with 0.0 for no advantage and disadvantage :)
        Map<CurrencyEnum, BigDecimal> balances = new HashMap<>();
        for (CurrencyEnum currency : CurrencyEnum.values()) {
            balances.put(currency, BigDecimal.ZERO);
        }
        account.setBalances(balances);

        Account savedAccount = accountDAO.save(account);
        restTemplate.postForObject("https://httpstat.us/200", null, String.class);
        return AccountDTO.fromEntity(savedAccount);
    }

    public AccountDTO depositMoney(Long accountId, CurrencyEnum currency, BigDecimal amount) {
        return updateBalance(accountId, currency, amount, TransactionType.DEPOSIT);
    }

    public AccountDTO debitMoney(Long accountId, CurrencyEnum currency, BigDecimal amount) {
        return updateBalance(accountId, currency, amount, TransactionType.DEBIT);
    }

    private AccountDTO updateBalance(Long accountId, CurrencyEnum currency, BigDecimal amount, TransactionType transactionType) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Account account = accountDAO.findById(accountId).orElseThrow();
        BigDecimal roundedAmount = roundToTwoDecimalPlaces(amount);
        BigDecimal currentBalance = account.getBalances().getOrDefault(currency, BigDecimal.ZERO);

        if (transactionType == TransactionType.DEBIT && currentBalance.compareTo(roundedAmount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        if (transactionType == TransactionType.DEBIT) {
            account.getBalances().computeIfPresent(currency, (k, v) -> v.subtract(roundedAmount));
        } else {
            account.getBalances().merge(currency, roundedAmount, BigDecimal::add);
        }

        accountDAO.save(account);
        transactionService.saveTransaction(accountId, currency.name(), transactionType == TransactionType.DEBIT ? roundedAmount.negate() : roundedAmount, transactionType);

        restTemplate.postForObject("https://httpstat.us/200", null, String.class);

        return AccountDTO.fromEntity(account);
    }

    public Map<CurrencyEnum, BigDecimal> getAccountBalance(Long accountId) {
        Account account = accountDAO.findById(accountId).orElseThrow();
        restTemplate.postForObject("https://httpstat.us/200", null, String.class);
        return account.getBalances();
    }

    public AccountDTO exchangeCurrency(Long accountId, CurrencyEnum fromCurrency, CurrencyEnum toCurrency, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Account account = accountDAO.findById(accountId).orElseThrow();

        BigDecimal convertedAmount = currencyConversionService.convert(account, fromCurrency, toCurrency, amount);

        accountDAO.save(account);

        transactionService.saveTransaction(account.getId(), fromCurrency.name(), amount.negate(), TransactionType.EXCHANGE);
        transactionService.saveTransaction(account.getId(), toCurrency.name(), convertedAmount, TransactionType.EXCHANGE);

        restTemplate.postForObject("https://httpstat.us/200", null, String.class);
        return AccountDTO.fromEntity(account);
    }

    public List<Transaction> getAccountTransactionHistory(Long accountId) {
        accountDAO.findById(accountId).orElseThrow(() -> new NoSuchElementException("No value present"));
        restTemplate.postForObject("https://httpstat.us/200", null, String.class);
        return transactionService.getTransactionHistory(accountId);
    }

    private BigDecimal roundToTwoDecimalPlaces(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
