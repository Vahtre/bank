package com.testassignment.bank;

import com.testassignment.bank.dao.TransactionDAO;
import com.testassignment.bank.entity.Transaction;
import com.testassignment.bank.enums.TransactionType;
import com.testassignment.bank.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransactionServiceUT {

    @Mock
    private TransactionDAO transactionDAO;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveTransaction() {
        Long accountId = 1L;
        String currency = "USD";
        BigDecimal amount = BigDecimal.valueOf(100.00);
        TransactionType transactionType = TransactionType.DEPOSIT;

        transactionService.saveTransaction(accountId, currency, amount, transactionType);

        verify(transactionDAO, times(1)).save(any(Transaction.class));
    }

    @Test
    void testGetTransactionHistory() {
        Long accountId = 1L;
        Transaction transaction1 = new Transaction();
        transaction1.setAccountId(accountId);
        transaction1.setCurrency("USD");
        transaction1.setAmount(BigDecimal.valueOf(100.00));
        transaction1.setTimestamp(LocalDateTime.now());
        transaction1.setTransactionType(TransactionType.DEPOSIT);

        Transaction transaction2 = new Transaction();
        transaction2.setAccountId(accountId);
        transaction2.setCurrency("EUR");
        transaction2.setAmount(BigDecimal.valueOf(200.00));
        transaction2.setTimestamp(LocalDateTime.now());
        transaction2.setTransactionType(TransactionType.DEBIT);

        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);

        when(transactionDAO.findByAccountId(accountId)).thenReturn(transactions);

        List<Transaction> result = transactionService.getTransactionHistory(accountId);

        assertEquals(2, result.size());
        assertEquals(transaction1, result.get(0));
        assertEquals(transaction2, result.get(1));
    }

    @Test
    void testGetTransactionHistoryForNonExistentUser() {
        Long nonExistentAccountId = 999L;

        when(transactionDAO.findByAccountId(nonExistentAccountId)).thenReturn(Arrays.asList());

        List<Transaction> result = transactionService.getTransactionHistory(nonExistentAccountId);

        assertEquals(0, result.size());
    }
}
