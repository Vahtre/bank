package com.testassignment.bank.integration;

import com.testassignment.bank.dao.AccountDAO;
import com.testassignment.bank.dto.AccountDTO;
import com.testassignment.bank.entity.Account;
import com.testassignment.bank.entity.Transaction;
import com.testassignment.bank.enums.CurrencyEnum;
import com.testassignment.bank.enums.TransactionType;
import com.testassignment.bank.service.AccountService;
import com.testassignment.bank.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
@Transactional
public class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountDAO accountDAO;

    private Account createTestAccount() {
        Account account = new Account();
        account.setAccountNumber("12345");

        Map<CurrencyEnum, BigDecimal> balances = new HashMap<>();
        for (CurrencyEnum currency : CurrencyEnum.values()) {
            balances.put(currency, BigDecimal.valueOf(1000));
        }

        account.setBalances(balances);

        return accountDAO.save(account);
    }

    @Test
    void testCreateAccount() throws Exception {
        String accountJson = "{ \"accountNumber\": \"12345\" }";

        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("12345"))
                .andExpect(jsonPath("$.balances.EUR").value(0))
                .andExpect(jsonPath("$.balances.USD").value(0))
                .andExpect(jsonPath("$.balances.SEK").value(0))
                .andExpect(jsonPath("$.balances.RUB").value(0))
                .andReturn();
    }

    @Test
    void testCreateAccountFailsWhenAccountNumberIsMissing() throws Exception {
        String accountJson = "{ }";

        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.details[0]").value("accountNumber: Account number is mandatory"))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void testCreateAccountFailsWhenAccountNumberIsInvalid() throws Exception {
        String accountJson = "{ \"accountNumber\": \"\" }";

        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.details[0]").value("accountNumber: Account number is mandatory"))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void testDepositMoney() throws Exception {
        Account savedAccount = createTestAccount();

        for (CurrencyEnum currency : CurrencyEnum.values()) {
            String moneyJson = String.format("{ \"currency\": \"%s\", \"amount\": 500.00 }", currency.name());

            // Add money the first time
            mockMvc.perform(post("/account/" + savedAccount.getId() + "/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(moneyJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balances." + currency.name()).value(1500.00));

            // Check transaction history
            List<Transaction> transactions = transactionService.getTransactionHistory(savedAccount.getId());
            Transaction lastTransaction = transactions.get(transactions.size() - 1);
            assertEquals(currency.name(), lastTransaction.getCurrency());
            assertEquals(0, lastTransaction.getAmount().compareTo(BigDecimal.valueOf(500.00)));
            assertEquals(TransactionType.DEPOSIT, lastTransaction.getTransactionType());

            // Add money the second time
            mockMvc.perform(post("/account/" + savedAccount.getId() + "/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(moneyJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balances." + currency.name()).value(2000.00));

            // Check transaction history
            transactions = transactionService.getTransactionHistory(savedAccount.getId());
            lastTransaction = transactions.get(transactions.size() - 1);
            assertEquals(currency.name(), lastTransaction.getCurrency());
            assertEquals(0, lastTransaction.getAmount().compareTo(BigDecimal.valueOf(500.00)));
            assertEquals(TransactionType.DEPOSIT, lastTransaction.getTransactionType());
        }
    }

    @Test
    void testAddMoneyFailsWhenAmountIsNegative() throws Exception {
        Account savedAccount = createTestAccount();

        for (CurrencyEnum currency : CurrencyEnum.values()) {
            String moneyJson = String.format("{ \"currency\": \"%s\", \"amount\": -500.00 }", currency.name());

            mockMvc.perform(post("/account/" + savedAccount.getId() + "/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(moneyJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation Failed"))
                    .andExpect(jsonPath("$.details[0]").value("amount: Amount must be positive"))
                    .andExpect(jsonPath("$.statusCode").value(400));
        }
    }

    @Test
    void testAddMoneyFailsWhenCurrencyIsInvalid() throws Exception {
        Account savedAccount = createTestAccount();

        String moneyJson = "{ \"currency\": \"INVALID\", \"amount\": 500.00 }";

        mockMvc.perform(post("/account/" + savedAccount.getId() + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(moneyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid input"))
                .andExpect(jsonPath("$.details[0]").value("JSON parse error: Cannot deserialize value of type `com.testassignment.bank.enums.CurrencyEnum` from String \"INVALID\": not one of the values accepted for Enum class: [SEK, EUR, USD, RUB]"))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void testDebitMoney() throws Exception {
        Account savedAccount = createTestAccount();

        for (CurrencyEnum currency : CurrencyEnum.values()) {
            String moneyJson = String.format("{ \"currency\": \"%s\", \"amount\": 200.00 }", currency.name());

            // Debit money the first time
            mockMvc.perform(post("/account/" + savedAccount.getId() + "/debit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(moneyJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balances." + currency.name()).value(800.00));

            // Check transaction history
            List<Transaction> transactions = transactionService.getTransactionHistory(savedAccount.getId());
            Transaction lastTransaction = transactions.get(transactions.size() - 1);
            assertEquals(currency.name(), lastTransaction.getCurrency());
            assertEquals(0, lastTransaction.getAmount().compareTo(BigDecimal.valueOf(200.00).negate()));
            assertEquals(TransactionType.DEBIT, lastTransaction.getTransactionType());

            // Debit money the second time
            mockMvc.perform(post("/account/" + savedAccount.getId() + "/debit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(moneyJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balances." + currency.name()).value(600.00));

            // Check transaction history
            transactions = transactionService.getTransactionHistory(savedAccount.getId());
            lastTransaction = transactions.get(transactions.size() - 1);
            assertEquals(currency.name(), lastTransaction.getCurrency());
            assertEquals(0, lastTransaction.getAmount().compareTo(BigDecimal.valueOf(200.00).negate()));
            assertEquals(TransactionType.DEBIT, lastTransaction.getTransactionType());
        }
    }

    @Test
    void testDebitMoneyFailsWhenAmountIsNegative() throws Exception {
        Account savedAccount = createTestAccount();

        for (CurrencyEnum currency : CurrencyEnum.values()) {
            String moneyJson = String.format("{ \"currency\": \"%s\", \"amount\": -200.00 }", currency.name());

            mockMvc.perform(post("/account/" + savedAccount.getId() + "/debit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(moneyJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation Failed"))
                    .andExpect(jsonPath("$.details[0]").value("amount: Amount must be positive"))
                    .andExpect(jsonPath("$.statusCode").value(400));
        }
    }

    @Test
    void testDebitMoneyFailsWhenAmountExceedsBalance() throws Exception {
        Account savedAccount = createTestAccount();

        for (CurrencyEnum currency : CurrencyEnum.values()) {
            String moneyJson = String.format("{ \"currency\": \"%s\", \"amount\": 2000.00 }", currency.name());

            mockMvc.perform(post("/account/" + savedAccount.getId() + "/debit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(moneyJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid argument"))
                    .andExpect(jsonPath("$.details[0]").value("Insufficient funds"))
                    .andExpect(jsonPath("$.statusCode").value(400));
        }
    }

    @Test
    void testGetAccountBalance() throws Exception {
        Account savedAccount = createTestAccount();

        for (CurrencyEnum currency : CurrencyEnum.values()) {
            mockMvc.perform(get("/account/" + savedAccount.getId() + "/balance"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + currency.name()).value(1000.00));
        }
    }

    @Test
    void testAddMoneyInDifferentCurrencies() throws Exception {
        Account savedAccount = createTestAccount();

        Map<CurrencyEnum, BigDecimal> initialBalances = new HashMap<>();
        for (CurrencyEnum currency : CurrencyEnum.values()) {
            initialBalances.put(currency, BigDecimal.valueOf(1000));
        }

        for (CurrencyEnum currency : CurrencyEnum.values()) {
            String moneyJson = String.format("{ \"currency\": \"%s\", \"amount\": 500.00 }", currency.name());

            mockMvc.perform(post("/account/" + savedAccount.getId() + "/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(moneyJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balances." + currency.name()).value(initialBalances.get(currency).add(BigDecimal.valueOf(500)).intValue()));

            // Check that other currency balances remain unchanged
            for (CurrencyEnum otherCurrency : CurrencyEnum.values()) {
                if (!otherCurrency.equals(currency)) {
                    mockMvc.perform(get("/account/" + savedAccount.getId() + "/balance"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$." + otherCurrency.name()).value(initialBalances.get(otherCurrency).intValue()));
                }
            }

            // Update the initial balance for the current currency
            initialBalances.put(currency, initialBalances.get(currency).add(BigDecimal.valueOf(500)));

            // Check transaction history
            List<Transaction> transactions = transactionService.getTransactionHistory(savedAccount.getId());
            Transaction lastTransaction = transactions.get(transactions.size() - 1);
            assertEquals(currency.name(), lastTransaction.getCurrency());
            assertEquals(0, lastTransaction.getAmount().compareTo(BigDecimal.valueOf(500.00)));
            assertEquals(TransactionType.DEPOSIT, lastTransaction.getTransactionType());
        }
    }

    @Test
    void testGetAccountBalanceFailsWhenAccountNotFound() throws Exception {
        mockMvc.perform(get("/account/99999/balance"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No value present"))
                .andExpect(jsonPath("$.details[0]").value("No value present"))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    void testExchangeCurrency() throws Exception {
        Account savedAccount = createTestAccount();

        // Mock the conversion rate (e.g., 1 USD = 0.98 EUR)
        BigDecimal conversionRate = BigDecimal.valueOf(0.98);
        String exchangeJson = "{ \"fromCurrency\": \"USD\", \"toCurrency\": \"EUR\", \"amount\": 100.00 }";

        mockMvc.perform(post("/account/" + savedAccount.getId() + "/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exchangeJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balances.USD").value(900.00))
                .andExpect(jsonPath("$.balances.EUR").value(100.00 * conversionRate.doubleValue() + 1000));

        // Check transaction history
        List<Transaction> transactions = transactionService.getTransactionHistory(savedAccount.getId());
        Transaction lastTransaction = transactions.get(transactions.size() - 2);
        assertEquals("USD", lastTransaction.getCurrency());
        assertEquals(0, lastTransaction.getAmount().compareTo(BigDecimal.valueOf(100.00).negate()));
        assertEquals(TransactionType.EXCHANGE, lastTransaction.getTransactionType());

        lastTransaction = transactions.get(transactions.size() - 1);
        assertEquals("EUR", lastTransaction.getCurrency());
        assertEquals(0, lastTransaction.getAmount().compareTo(BigDecimal.valueOf(100.00 * conversionRate.doubleValue())));
        assertEquals(TransactionType.EXCHANGE, lastTransaction.getTransactionType());
    }

    @Test
    void testExchangeCurrencyFailsWhenAmountIsNegative() throws Exception {
        Account savedAccount = createTestAccount();

        for (CurrencyEnum currency : CurrencyEnum.values()) {
            String moneyJson = String.format("{ \"currency\": \"%s\", \"amount\": 2000.00 }", currency.name());

            mockMvc.perform(post("/account/" + savedAccount.getId() + "/debit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(moneyJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid argument"))
                    .andExpect(jsonPath("$.details[0]").value("Insufficient funds"))
                    .andExpect(jsonPath("$.statusCode").value(400));
        }
    }

    @Test
    void testExchangeCurrencyFailsWhenInsufficientFunds() throws Exception {
        Account savedAccount = createTestAccount();

        for (CurrencyEnum currency : CurrencyEnum.values()) {
            String exchangeJson = "{ \"fromCurrency\": \"USD\", \"toCurrency\": \"EUR\", \"amount\": 2000.00 }";

            mockMvc.perform(post("/account/" + savedAccount.getId() + "/exchange")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(exchangeJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid argument"))
                    .andExpect(jsonPath("$.details[0]").value("Insufficient funds"))
                    .andExpect(jsonPath("$.statusCode").value(400));
        }
    }

    @Test
    void testGetAccountTransactionHistory() throws Exception {
        Account savedAccount = createTestAccount();

        mockMvc.perform(get("/account/" + savedAccount.getId() + "/transactions"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAccountTransactionHistoryFailsWhenAccountNotFound() throws Exception {
        mockMvc.perform(get("/account/99999/transactions"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No value present"))
                .andExpect(jsonPath("$.details[0]").value("No value present"))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

}