package com.testassignment.bank;

import com.testassignment.bank.dao.AccountDAO;
import com.testassignment.bank.dto.AccountDTO;
import com.testassignment.bank.entity.Account;
import com.testassignment.bank.enums.CurrencyEnum;
import com.testassignment.bank.enums.TransactionType;
import com.testassignment.bank.service.AccountService;
import com.testassignment.bank.service.CurrencyConversionService;
import com.testassignment.bank.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AccountServiceUT {

    @Mock
    private AccountDAO accountDAO;

    @Mock
    private TransactionService transactionService;

    @Mock
    private CurrencyConversionService currencyConversionService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("12345");
        account.setBalances(new HashMap<>());

        when(accountDAO.save(any(Account.class))).thenReturn(account);

        AccountDTO accountDTO = accountService.createAccount("12345");

        assertNotNull(accountDTO);
        assertEquals("12345", accountDTO.getAccountNumber());
        verify(accountDAO, times(1)).save(any(Account.class));
    }

    @ParameterizedTest
    @CsvSource({
            "USD, 100.00",
            "EUR, 100.00",
            "SEK, 100.00",
            "RUB, 100.00"
    })
    void testAddMoney(String currency, BigDecimal amount) {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("12345");
        account.setBalances(new HashMap<>());

        when(accountDAO.findById(1L)).thenReturn(Optional.of(account));
        when(accountDAO.save(any(Account.class))).thenReturn(account);
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("OK");

        // Initial balance
        BigDecimal initialBalance = account.getBalances().getOrDefault(currency, BigDecimal.ZERO);

        AccountDTO accountDTO = accountService.addMoney(1L, CurrencyEnum.valueOf(currency), amount);

        // Final balance
        BigDecimal finalBalance = account.getBalances().get(CurrencyEnum.valueOf(currency));

        assertNotNull(accountDTO);
        assertEquals(amount, accountDTO.getBalances().get(CurrencyEnum.valueOf(currency)));
        assertEquals(initialBalance.add(amount), finalBalance);
        verify(accountDAO, times(1)).save(any(Account.class));
        verify(transactionService, times(1)).saveTransaction(eq(1L), eq(currency), eq(amount), eq(TransactionType.DEPOSIT));
    }

    @ParameterizedTest
    @CsvSource({
            "USD, 100.00, 50.00",
            "EUR, 100.00, 50.00",
            "SEK, 100.00, 50.00",
            "RUB, 100.00, 50.00"
    })
    void testAddMoneyTwice(String currency, BigDecimal firstAmount, BigDecimal secondAmount) {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("12345");
        account.setBalances(new HashMap<>());

        when(accountDAO.findById(1L)).thenReturn(Optional.of(account));
        when(accountDAO.save(any(Account.class))).thenReturn(account);
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("OK");

        // First time adding money (100)
        AccountDTO accountDTO = accountService.addMoney(1L, CurrencyEnum.valueOf(currency), firstAmount);
        assertNotNull(accountDTO);
        assertEquals(firstAmount, accountDTO.getBalances().get(CurrencyEnum.valueOf(currency)));
        verify(accountDAO, times(1)).save(any(Account.class));
        verify(transactionService, times(1)).saveTransaction(eq(1L), eq(currency), eq(firstAmount), eq(TransactionType.DEPOSIT));

        // Second time adding money (50, 150 in total)
        accountDTO = accountService.addMoney(1L, CurrencyEnum.valueOf(currency), secondAmount);
        assertNotNull(accountDTO);
        assertEquals(firstAmount.add(secondAmount), accountDTO.getBalances().get(CurrencyEnum.valueOf(currency)));
        verify(accountDAO, times(2)).save(any(Account.class));
        verify(transactionService, times(1)).saveTransaction(eq(1L), eq(currency), eq(secondAmount), eq(TransactionType.DEPOSIT));
    }

    @ParameterizedTest
    @CsvSource({
            "USD, -100.00",
            "EUR, -100.00",
            "SEK, -100.00",
            "RUB, -100.00"
    })
    void testAddNegativeMoney(String currency, BigDecimal amount) {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("12345");
        Map<CurrencyEnum, BigDecimal> balances = new HashMap<>();
        balances.put(CurrencyEnum.valueOf(currency), BigDecimal.valueOf(250.00));
        account.setBalances(balances);

        when(accountDAO.findById(1L)).thenReturn(Optional.of(account));

        BigDecimal initialBalance = account.getBalances().get(CurrencyEnum.valueOf(currency));

        assertThrows(IllegalArgumentException.class, () -> {
            accountService.addMoney(1L, CurrencyEnum.valueOf(currency), amount);
        });

        BigDecimal finalBalance = account.getBalances().get(CurrencyEnum.valueOf(currency));

        assertEquals(initialBalance, finalBalance);
        verify(accountDAO, never()).save(any(Account.class));
        verify(transactionService, never()).saveTransaction(anyLong(), anyString(), any(BigDecimal.class), any(TransactionType.class));
    }

    @ParameterizedTest
    @CsvSource({
            "USD, 100.00",
            "EUR, 100.00",
            "SEK, 100.00",
            "RUB, 100.00"
    })
    void testDebitMoney(String currency, BigDecimal amount) {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("12345");
        Map<CurrencyEnum, BigDecimal> balances = new HashMap<>();
        balances.put(CurrencyEnum.valueOf(currency), BigDecimal.valueOf(200.00));
        account.setBalances(balances);

        when(accountDAO.findById(1L)).thenReturn(Optional.of(account));
        when(accountDAO.save(any(Account.class))).thenReturn(account);
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("OK");

        AccountDTO accountDTO = accountService.debitMoney(1L, CurrencyEnum.valueOf(currency), amount);

        assertNotNull(accountDTO);
        assertTrue(BigDecimal.valueOf(100.0).compareTo(accountDTO.getBalances().get(CurrencyEnum.valueOf(currency))) == 0);

        verify(accountDAO, times(1)).save(any(Account.class));
        verify(transactionService, times(1)).saveTransaction(
                eq(1L),
                eq(currency),
                argThat(argument -> argument.compareTo(amount.negate()) == 0),
                eq(TransactionType.DEBIT)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "USD, -100.00",
            "EUR, -100.00",
            "SEK, -100.00",
            "RUB, -100.00"
    })
    void testDebitNegativeMoney(String currency, BigDecimal amount) {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("12345");
        Map<CurrencyEnum, BigDecimal> balances = new HashMap<>();
        balances.put(CurrencyEnum.valueOf(currency), BigDecimal.valueOf(250.00));
        account.setBalances(balances);

        when(accountDAO.findById(1L)).thenReturn(Optional.of(account));

        BigDecimal initialBalance = account.getBalances().get(CurrencyEnum.valueOf(currency));

        assertThrows(IllegalArgumentException.class, () -> {
            accountService.debitMoney(1L, CurrencyEnum.valueOf(currency), amount);
        });

        BigDecimal finalBalance = account.getBalances().get(CurrencyEnum.valueOf(currency));

        assertEquals(initialBalance, finalBalance);
        verify(accountDAO, never()).save(any(Account.class));
        verify(transactionService, never()).saveTransaction(anyLong(), anyString(), any(BigDecimal.class), any(TransactionType.class));
    }

    @ParameterizedTest
    @CsvSource({
            "USD, 10.00",
            "EUR, 10.00",
            "SEK, 10.00",
            "RUB, 10.00"
    })
    void testDebitMoreThanBalance(String currency, BigDecimal amount) {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("12345");
        Map<CurrencyEnum, BigDecimal> balances = new HashMap<>();
        balances.put(CurrencyEnum.valueOf(currency), BigDecimal.valueOf(5.00));
        account.setBalances(balances);

        when(accountDAO.findById(1L)).thenReturn(Optional.of(account));

        BigDecimal initialBalance = account.getBalances().get(CurrencyEnum.valueOf(currency));

        assertThrows(IllegalArgumentException.class, () -> {
            accountService.debitMoney(1L, CurrencyEnum.valueOf(currency), amount);
        });

        BigDecimal finalBalance = account.getBalances().get(CurrencyEnum.valueOf(currency));

        assertEquals(initialBalance, finalBalance);
        verify(accountDAO, never()).save(any(Account.class));
        verify(transactionService, never()).saveTransaction(anyLong(), anyString(), any(BigDecimal.class), any(TransactionType.class));
    }

    @Test
    void testGetAccountBalance() {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("12345");
        Map<CurrencyEnum, BigDecimal> balances = new HashMap<>();
        balances.put(CurrencyEnum.USD, BigDecimal.valueOf(100.00));
        balances.put(CurrencyEnum.EUR, BigDecimal.valueOf(200.00));
        balances.put(CurrencyEnum.SEK, BigDecimal.valueOf(300.00));
        balances.put(CurrencyEnum.RUB, BigDecimal.valueOf(400.00));
        account.setBalances(balances);

        when(accountDAO.findById(1L)).thenReturn(Optional.of(account));

        Map<CurrencyEnum, BigDecimal> accountBalances = accountService.getAccountBalance(1L);

        assertNotNull(accountBalances);
        assertEquals(BigDecimal.valueOf(100.00), accountBalances.get(CurrencyEnum.USD));
        assertEquals(BigDecimal.valueOf(200.00), accountBalances.get(CurrencyEnum.EUR));
        assertEquals(BigDecimal.valueOf(300.00), accountBalances.get(CurrencyEnum.SEK));
        assertEquals(BigDecimal.valueOf(400.00), accountBalances.get(CurrencyEnum.RUB));
        verify(accountDAO, times(1)).findById(1L);
    }

    @ParameterizedTest
    @CsvSource({
            "USD, EUR, 50.00, 42.50",
            "EUR, SEK, 100.00, 1050.00",
            "SEK, RUB, 200.00, 1800.00",
            "RUB, USD, 300.00, 4.00"
    })
    void testExchangeCurrency(String fromCurrency, String toCurrency, BigDecimal amount, BigDecimal expectedConvertedAmount) {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("12345");
        Map<CurrencyEnum, BigDecimal> balances = new HashMap<>();
        balances.put(CurrencyEnum.USD, BigDecimal.valueOf(100.00));
        balances.put(CurrencyEnum.EUR, BigDecimal.valueOf(200.00));
        balances.put(CurrencyEnum.SEK, BigDecimal.valueOf(300.00));
        balances.put(CurrencyEnum.RUB, BigDecimal.valueOf(400.00));
        account.setBalances(balances);

        when(accountDAO.findById(1L)).thenReturn(Optional.of(account));
        when(currencyConversionService.convert(any(Account.class), eq(CurrencyEnum.valueOf(fromCurrency)), eq(CurrencyEnum.valueOf(toCurrency)), eq(amount)))
                .thenReturn(expectedConvertedAmount);
        when(accountDAO.save(any(Account.class))).thenReturn(account);

        AccountDTO accountDTO = accountService.exchangeCurrency(1L, CurrencyEnum.valueOf(fromCurrency), CurrencyEnum.valueOf(toCurrency), amount);

        assertNotNull(accountDTO);
        assertEquals(accountDTO.getBalances().get(CurrencyEnum.valueOf(fromCurrency)), balances.get(CurrencyEnum.valueOf(fromCurrency)));
        verify(accountDAO, times(1)).save(any(Account.class));
        verify(transactionService, times(1)).saveTransaction(
                eq(1L),
                eq(fromCurrency),
                argThat(argument -> argument.compareTo(amount.negate()) == 0),
                eq(TransactionType.EXCHANGE)
        );
        verify(transactionService, times(1)).saveTransaction(
                eq(1L),
                eq(toCurrency),
                argThat(argument -> argument.compareTo(expectedConvertedAmount) == 0),
                eq(TransactionType.EXCHANGE)
        );

        // Update the initial balances to reflect the new state after the exchange
        balances.put(CurrencyEnum.valueOf(fromCurrency), balances.get(CurrencyEnum.valueOf(fromCurrency)).subtract(amount));
        balances.put(CurrencyEnum.valueOf(toCurrency), balances.get(CurrencyEnum.valueOf(toCurrency)).add(expectedConvertedAmount));

        // Check if the account balance is correct after the exchange
        Map<CurrencyEnum, BigDecimal> finalBalances = accountService.getAccountBalance(1L);
        assertEquals(balances.get(CurrencyEnum.valueOf(fromCurrency)), finalBalances.get(CurrencyEnum.valueOf(fromCurrency)));
        assertEquals(balances.get(CurrencyEnum.valueOf(toCurrency)), finalBalances.get(CurrencyEnum.valueOf(toCurrency)));
    }

    @ParameterizedTest
    @CsvSource({
            "USD, EUR, -50.00",
            "EUR, SEK, -100.00",
            "SEK, RUB, -200.00",
            "RUB, USD, -300.00"
    })
    void testExchangeNegativeAmount(String fromCurrency, String toCurrency, BigDecimal amount) {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("12345");
        Map<CurrencyEnum, BigDecimal> balances = new HashMap<>();
        balances.put(CurrencyEnum.USD, BigDecimal.valueOf(100.00));
        balances.put(CurrencyEnum.EUR, BigDecimal.valueOf(200.00));
        balances.put(CurrencyEnum.SEK, BigDecimal.valueOf(300.00));
        balances.put(CurrencyEnum.RUB, BigDecimal.valueOf(400.00));
        account.setBalances(balances);

        when(accountDAO.findById(1L)).thenReturn(Optional.of(account));

        BigDecimal initialFromBalance = account.getBalances().get(CurrencyEnum.valueOf(fromCurrency));
        BigDecimal initialToBalance = account.getBalances().get(CurrencyEnum.valueOf(toCurrency));

        assertThrows(IllegalArgumentException.class, () -> {
            accountService.exchangeCurrency(1L, CurrencyEnum.valueOf(fromCurrency), CurrencyEnum.valueOf(toCurrency), amount);
        });

        BigDecimal finalFromBalance = account.getBalances().get(CurrencyEnum.valueOf(fromCurrency));
        BigDecimal finalToBalance = account.getBalances().get(CurrencyEnum.valueOf(toCurrency));

        assertEquals(initialFromBalance, finalFromBalance);
        assertEquals(initialToBalance, finalToBalance);
        verify(accountDAO, never()).save(any(Account.class));
        verify(transactionService, never()).saveTransaction(anyLong(), anyString(), any(BigDecimal.class), any(TransactionType.class));
    }
}
