package com.testassignment.bank.dto;

import com.testassignment.bank.entity.Account;
import com.testassignment.bank.enums.CurrencyEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class AccountDTO {

    private Long id;
    private String accountNumber;
    private Map<CurrencyEnum, BigDecimal> balances;

    public static AccountDTO fromEntity(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalances(account.getBalances());

        return dto;
    }
}
