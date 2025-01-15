package com.testassignment.bank.dto;

import com.testassignment.bank.entity.Account;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

public class AccountDTO {

    private Long id;
    private String accountNumber;
    private Map<String, BigDecimal> balances;

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

    public Map<String, BigDecimal> getBalances() {
        return balances;
    }

    public void setBalances(Map<String, BigDecimal> balances) {
        this.balances = balances;
    }

    public static AccountDTO fromEntity(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());

        Map<String, BigDecimal> balances = account.getBalances().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        dto.setBalances(balances);

        return dto;
    }
}
