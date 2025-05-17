package com.financeapp.controller;

import com.financeapp.controller.TransactionController;
import com.financeapp.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TransactionControllerTest {

    private TransactionController controller;

    @BeforeEach
    void setUp() {
        controller = new TransactionController();
    }

    @Test
    void testAddAndGetTransactions() throws IOException {
        Transaction t1 = new Transaction(
                LocalDate.now(), "Food", 15.0, "Lunch");
        Transaction t2 = new Transaction(
                LocalDate.now(), "Transportation", 5.0, "Bus fare");

        controller.addTransaction(t1);
        controller.addTransaction(t2);

        List<Transaction> transactions = controller.getTransactions();
        assertEquals(2, transactions.size());
        assertTrue(transactions.contains(t1));
        assertTrue(transactions.contains(t2));
    }

    @Test
    void testAddMultipleTransactions() throws IOException {
        List<Transaction> toAdd = new ArrayList<>();
        toAdd.add(new Transaction(
                LocalDate.now(), "Food", 10.0, "Breakfast"));
        toAdd.add(new Transaction(
                LocalDate.now(), "Shopping", 50.0, "Clothes"));

        controller.addTransactions(toAdd);
        assertEquals(2, controller.getTransactions().size());
    }

    @Test
    void testUpdateCategory() throws IOException {
        Transaction transaction = new Transaction(
                LocalDate.now(), null, 20.0, "Movie");
        controller.addTransaction(transaction);

        controller.updateCategory(transaction, "Entertainment");
        assertEquals("Entertainment", transaction.getCategory());

        // Verify the transaction in the list is updated
        assertEquals("Entertainment",
                controller.getTransactions().get(0).getCategory());
    }

    @Test
    void testDeleteTransaction() throws IOException {
        Transaction t1 = new Transaction(
                LocalDate.now(), "Food", 10.0, "Lunch");
        Transaction t2 = new Transaction(
                LocalDate.now(), "Transportation", 5.0, "Bus");

        controller.addTransaction(t1);
        controller.addTransaction(t2);
        assertEquals(2, controller.getTransactions().size());

        controller.deleteTransaction(t1);
        assertEquals(1, controller.getTransactions().size());
        assertEquals(t2, controller.getTransactions().get(0));
    }

    @Test
    void testDeleteMultipleTransactions() throws IOException {
        List<Transaction> toAdd = List.of(
                new Transaction(LocalDate.now(), "Food", 10.0, "Lunch"),
                new Transaction(LocalDate.now(), "Transportation", 5.0, "Bus"),
                new Transaction(LocalDate.now(), "Entertainment", 15.0, "Movie")
        );

        controller.addTransactions(toAdd);
        assertEquals(3, controller.getTransactions().size());

        List<Transaction> toDelete = List.of(toAdd.get(0), toAdd.get(2));
        controller.deleteTransactions(toDelete);
        assertEquals(1, controller.getTransactions().size());
        assertEquals("Transportation", controller.getTransactions().get(0).getCategory());
    }

    @Test
    void testMonthlyExpenses() throws IOException {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // Add transactions for current month
        controller.addTransaction(new Transaction(
                LocalDate.of(year, month, 1), "Food", 10.0, "Breakfast"));
        controller.addTransaction(new Transaction(
                LocalDate.of(year, month, 15), "Food", 15.0, "Lunch"));
        controller.addTransaction(new Transaction(
                LocalDate.of(year, month, 20), "Transportation", 5.0, "Bus"));

        // Add transaction from different month (shouldn't be counted)
        controller.addTransaction(new Transaction(
                LocalDate.of(year, month - 1, 25), "Shopping", 50.0, "Clothes"));

        Map<String, Double> expenses = controller.getMonthlyExpenses(year, month);

        assertEquals(2, expenses.size());
        assertEquals(25.0, expenses.get("Food"), 0.001);
        assertEquals(5.0, expenses.get("Transportation"), 0.001);
        assertNull(expenses.get("Shopping"));
    }

    @Test
    void testCurrentMonthExpenses() throws IOException {
        LocalDate now = LocalDate.now();

        controller.addTransaction(new Transaction(now, "Food", 10.0, "Breakfast"));
        controller.addTransaction(new Transaction(now, "Transportation", 5.0, "Bus"));

        Map<String, Double> expenses = controller.getCurrentMonthExpenses();

        assertTrue(expenses.containsKey("Food"));
        assertTrue(expenses.containsKey("Transportation"));
        assertEquals(10.0, expenses.get("Food"), 0.001);
        assertEquals(5.0, expenses.get("Transportation"), 0.001);
    }

    @Test
    void testBudgetSuggestions() throws IOException {
        // Add historical data (3 months)
        LocalDate now = LocalDate.now();
        LocalDate month1 = now.minusMonths(2);
        LocalDate month2 = now.minusMonths(1);

        // Historical spending - Food average is 100, current is 150 (50% increase)
        controller.addTransaction(new Transaction(month1, "Food", 100.0, "Groceries"));
        controller.addTransaction(new Transaction(month2, "Food", 100.0, "Groceries"));
        controller.addTransaction(new Transaction(now, "Food", 150.0, "Groceries"));

        List<String> suggestions = controller.getBudgetSuggestions();

        assertFalse(suggestions.isEmpty());
        assertTrue(suggestions.stream().anyMatch(s -> s.contains("Food")));
    }

    @Test
    void testFileOperations(@TempDir Path tempDir) throws IOException {
        // This test would require modifying CSVHandler to accept a path
        // For now, we'll just test that save/load doesn't throw exceptions
        Transaction t1 = new Transaction(
                LocalDate.now(), "Food", 10.0, "Lunch");

        controller.addTransaction(t1);
        assertDoesNotThrow(() -> controller.saveTransactions());
        assertDoesNotThrow(() -> controller.loadTransactions());
    }
}