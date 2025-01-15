package com.testassignment.bank.service;

import com.testassignment.bank.dao.TransactionDAO;
import com.testassignment.bank.entity.Transaction;
import com.testassignment.bank.enums.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionDAO transactionDAO;

    public TransactionService(TransactionDAO transactionDAO) {
        this.transactionDAO = transactionDAO;
    }

    public void saveTransaction(Long accountId, String currency, BigDecimal amount, TransactionType transactionType) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setCurrency(currency);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setTransactionType(transactionType);
        transactionDAO.save(transaction);
    }

    public List<Transaction> getTransactionHistory(Long accountId) {
        return transactionDAO.findByAccountId(accountId);
    }
}
