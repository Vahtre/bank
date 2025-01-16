package com.testassignment.bank.dto;

import com.testassignment.bank.entity.Account;
import com.testassignment.bank.enums.CurrencyEnum;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

public class AccountDTO {

    private Long id;
    private String accountNumber;
    private Map<CurrencyEnum, BigDecimal> balances;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Map<CurrencyEnum, BigDecimal> getBalances() {
        return balances;
    }

    public void setBalances(Map<CurrencyEnum, BigDecimal> balances) {
        this.balances = balances;
    }

    public static AccountDTO fromEntity(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalances(account.getBalances());

        return dto;
    }
}
