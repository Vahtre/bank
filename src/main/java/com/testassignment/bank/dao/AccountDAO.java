package com.testassignment.bank.dao;

import com.testassignment.bank.entity.Account;
import com.testassignment.bank.repository.AccountRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountDAO {

    private final AccountRepository accountRepository;

    public AccountDAO(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }
}
