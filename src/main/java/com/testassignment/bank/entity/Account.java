package com.testassignment.bank.entity;

import com.testassignment.bank.enums.CurrencyEnum;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accountNumber;

    @ElementCollection
    @CollectionTable(name = "account_balance", joinColumns = @JoinColumn(name = "account_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "currency")
    @Column(name = "balance")
    private Map<CurrencyEnum, BigDecimal> balances = new HashMap<>();

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
}
