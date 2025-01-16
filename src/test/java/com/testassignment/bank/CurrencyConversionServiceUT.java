package com.testassignment.bank;

import com.testassignment.bank.entity.Account;
import com.testassignment.bank.entity.CurrencyConversionRate;
import com.testassignment.bank.enums.CurrencyEnum;
import com.testassignment.bank.repository.CurrencyConversionRateRepository;
import com.testassignment.bank.service.CurrencyConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class CurrencyConversionServiceUT {

    @Mock
    private CurrencyConversionRateRepository conversionRateRepository;

    @InjectMocks
    private CurrencyConversionService currencyConversionService;

    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        account = new Account();
        Map<CurrencyEnum, BigDecimal> balances = new HashMap<>();
        balances.put(CurrencyEnum.USD, BigDecimal.valueOf(100.00));
        balances.put(CurrencyEnum.EUR, BigDecimal.valueOf(200.00));
        balances.put(CurrencyEnum.SEK, BigDecimal.valueOf(300.00));
        balances.put(CurrencyEnum.RUB, BigDecimal.valueOf(400.00));
        account.setBalances(balances);
    }

    @ParameterizedTest
    @CsvSource({
            "USD, EUR, 50.00, 0.85, 42.50",
            "EUR, SEK, 100.00, 10.50, 1050.00",
            "SEK, RUB, 200.00, 9.00, 1800.00",
            "RUB, USD, 300.00, 0.01, 3.00"
    })
    void testConvertSuccess(String fromCurrency, String toCurrency, BigDecimal amount, BigDecimal rate, BigDecimal expectedConvertedAmount) {
        CurrencyConversionRate conversionRate = new CurrencyConversionRate();
        conversionRate.setFromCurrency(CurrencyEnum.valueOf(fromCurrency));
        conversionRate.setToCurrency(CurrencyEnum.valueOf(toCurrency));
        conversionRate.setRate(rate);

        when(conversionRateRepository.findByFromCurrencyAndToCurrency(CurrencyEnum.valueOf(fromCurrency), CurrencyEnum.valueOf(toCurrency)))
                .thenReturn(Optional.of(conversionRate));

        BigDecimal convertedAmount = currencyConversionService.convert(account, CurrencyEnum.valueOf(fromCurrency), CurrencyEnum.valueOf(toCurrency), amount);

        assertEquals(expectedConvertedAmount.setScale(2, RoundingMode.HALF_UP), convertedAmount);
        assertEquals(account.getBalances().get(CurrencyEnum.valueOf(toCurrency)).setScale(2, RoundingMode.HALF_UP), account.getBalances().get(CurrencyEnum.valueOf(toCurrency)));
    }

    @ParameterizedTest
    @CsvSource({
            "USD, EUR, 150.00",
            "EUR, SEK, 250.00",
            "SEK, RUB, 350.00",
            "RUB, USD, 450.00"
    })
    void testInsufficientFunds(String fromCurrency, String toCurrency, BigDecimal amount) {
        assertThrows(IllegalArgumentException.class, () -> {
            currencyConversionService.convert(account, CurrencyEnum.valueOf(fromCurrency), CurrencyEnum.valueOf(toCurrency), amount);
        });
    }

    @ParameterizedTest
    @CsvSource({
            "USD, EUR, 50.00",
            "EUR, SEK, 100.00",
            "SEK, RUB, 200.00",
            "RUB, USD, 300.00"
    })
    void testConversionRateNotFound(String fromCurrency, String toCurrency, BigDecimal amount) {
        when(conversionRateRepository.findByFromCurrencyAndToCurrency(CurrencyEnum.valueOf(fromCurrency), CurrencyEnum.valueOf(toCurrency)))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            currencyConversionService.convert(account, CurrencyEnum.valueOf(fromCurrency), CurrencyEnum.valueOf(toCurrency), amount);
        });
    }

    @ParameterizedTest
    @CsvSource({
            "USD, EUR, -50.00",
            "EUR, SEK, -100.00",
            "SEK, RUB, -200.00",
            "RUB, USD, -300.00"
    })
    void testConvertNegativeAmount(String fromCurrency, String toCurrency, BigDecimal amount) {
        assertThrows(IllegalArgumentException.class, () -> {
            currencyConversionService.convert(account, CurrencyEnum.valueOf(fromCurrency), CurrencyEnum.valueOf(toCurrency), amount);
        });
    }
}
