package com.testassignment.bank.config;

import com.testassignment.bank.entity.CurrencyConversionRate;
import com.testassignment.bank.enums.CurrencyEnum;
import com.testassignment.bank.repository.CurrencyConversionRateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@Configuration
public class DataInitializer {

    // This method initializes the database with the conversion rates between different currencies.
    // Won't run if table is already populated.
    @Bean
    public CommandLineRunner initDatabase(CurrencyConversionRateRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            Map<CurrencyEnum, Map<CurrencyEnum, Double>> conversionRates = new EnumMap<>(CurrencyEnum.class);

            Map<CurrencyEnum, Double> eurRates = new EnumMap<>(CurrencyEnum.class);
            eurRates.put(CurrencyEnum.USD, 1.02);
            eurRates.put(CurrencyEnum.SEK, 11.50);
            eurRates.put(CurrencyEnum.RUB, 105.80);

            Map<CurrencyEnum, Double> usdRates = new EnumMap<>(CurrencyEnum.class);
            usdRates.put(CurrencyEnum.EUR, 0.98);
            usdRates.put(CurrencyEnum.SEK, 11.26);
            usdRates.put(CurrencyEnum.RUB, 103.54);

            Map<CurrencyEnum, Double> sekRates = new EnumMap<>(CurrencyEnum.class);
            sekRates.put(CurrencyEnum.EUR, 0.087);
            sekRates.put(CurrencyEnum.USD, 0.089);
            sekRates.put(CurrencyEnum.RUB, 9.08);

            Map<CurrencyEnum, Double> rubRates = new EnumMap<>(CurrencyEnum.class);
            rubRates.put(CurrencyEnum.EUR, 0.0095);
            rubRates.put(CurrencyEnum.USD, 0.0097);
            rubRates.put(CurrencyEnum.SEK, 0.11);

            conversionRates.put(CurrencyEnum.EUR, eurRates);
            conversionRates.put(CurrencyEnum.USD, usdRates);
            conversionRates.put(CurrencyEnum.SEK, sekRates);
            conversionRates.put(CurrencyEnum.RUB, rubRates);

            conversionRates.forEach((fromCurrency, rates) -> {
                rates.forEach((toCurrency, rate) -> {
                    CurrencyConversionRate conversionRate = new CurrencyConversionRate();
                    conversionRate.setFromCurrency(fromCurrency);
                    conversionRate.setToCurrency(toCurrency);
                    conversionRate.setRate(BigDecimal.valueOf(rate));
                    repository.save(conversionRate);
                });
            });
        };
    }
}
