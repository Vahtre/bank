package com.testassignment.bank.validation;

import com.testassignment.bank.enums.CurrencyEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CurrencyEnumValidator implements ConstraintValidator<ValidCurrencyEnum, CurrencyEnum> {

    @Override
    public boolean isValid(CurrencyEnum value, ConstraintValidatorContext context) {
        return value != null;
    }
}
