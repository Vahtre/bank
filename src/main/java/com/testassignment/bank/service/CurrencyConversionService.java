package com.testassignment.bank.service;

import com.testassignment.bank.entity.Account;
import com.testassignment.bank.enums.CurrencyEnum;
import com.testassignment.bank.repository.CurrencyConversionRateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CurrencyConversionService {

    private final CurrencyConversionRateRepository conversionRateRepository;

    public CurrencyConversionService(CurrencyConversionRateRepository conversionRateRepository) {
        this.conversionRateRepository = conversionRateRepository;
    }

    public BigDecimal convert(Account account, CurrencyEnum fromCurrency, CurrencyEnum toCurrency, BigDecimal amount) {
        BigDecimal currentBalance = account.getBalances().getOrDefault(fromCurrency, BigDecimal.ZERO);

        if (currentBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        BigDecimal convertedAmount = conversionRateRepository.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency)
                .map(rate -> amount.multiply(rate.getRate()).setScale(2, RoundingMode.HALF_UP))
                .orElseThrow(() -> new IllegalArgumentException("Conversion rate not found"));

        account.getBalances().computeIfPresent(fromCurrency, (k, v) -> v.subtract(amount));
        account.getBalances().compute(toCurrency, (k, v) -> v == null ? convertedAmount : v.add(convertedAmount));

        return convertedAmount;
    }
}
