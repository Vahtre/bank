package com.testassignment.bank.entity;

import com.testassignment.bank.enums.CurrencyEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Data
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
}
