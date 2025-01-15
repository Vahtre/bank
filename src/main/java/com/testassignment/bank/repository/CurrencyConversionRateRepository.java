package com.testassignment.bank.repository;

import com.testassignment.bank.entity.CurrencyConversionRate;
import com.testassignment.bank.enums.CurrencyEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrencyConversionRateRepository extends JpaRepository<CurrencyConversionRate, Long> {
    Optional<CurrencyConversionRate> findByFromCurrencyAndToCurrency(CurrencyEnum fromCurrency, CurrencyEnum toCurrency);
}