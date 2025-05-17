package com.financeapp.model;

import com.financeapp.model.AIClassifier;
import com.financeapp.model.Transaction;
import com.financeapp.controller.TransactionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FinanceAppTest {

    private TransactionController controller;
    private AIClassifier classifier;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        controller = new TransactionController();
        classifier = new AIClassifier();
    }

    // Transaction Class Tests
    @Test
    void testTransactionCreation() {
        LocalDate date = LocalDate.now();
        Transaction transaction = new Transaction(date, "Food", 12.50, "Lunch at cafe");

        assertEquals(date, transaction.getDate());
        assertEquals("Food", transaction.getCategory());
        assertEquals(12.50, transaction.getAmount(), 0.001);
        assertEquals("Lunch at cafe", transaction.getDescription());
    }

    @Test
    void testTransactionToCSV() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        Transaction transaction = new Transaction(date, "Transportation", 3.75, "Bus fare");

        String expectedCSV = "2024-01-15,Transportation,3.75,Bus fare";
        assertEquals(expectedCSV, transaction.toCSV());
    }

    @Test
    void testTransactionFromCSV() {
        String csvLine = "2024-01-15,Transportation,3.75,Bus fare";
        Transaction transaction = Transaction.fromCSV(csvLine);

        assertEquals(LocalDate.of(2024, 1, 15), transaction.getDate());
        assertEquals("Transportation", transaction.getCategory());
        assertEquals(3.75, transaction.getAmount(), 0.001);
        assertEquals("Bus fare", transaction.getDescription());
    }

    @Test
    void testTransactionFromCSV_InvalidFormat() {
        String invalidCSV = "2024-01-15,Transportation";
        assertThrows(IllegalArgumentException.class, () -> Transaction.fromCSV(invalidCSV));
    }

    @Test
    void testTransactionEquality() {
        Transaction t1 = new Transaction(LocalDate.now(), "Food", 10.0, "Lunch");
        Transaction t2 = new Transaction(t1.getDate(), t1.getCategory(), t1.getAmount(), t1.getDescription());

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    // AIClassifier Tests
    @Test
    void testClassifierHolidayDetection() {
        Transaction holidayTransaction = new Transaction(LocalDate.of(2024, 2, 10), null, 100.0, "New Year gift");

        try {
            Transaction classified = classifier.classify(holidayTransaction);
            assertEquals("Holiday", classified.getCategory());
        } catch (IOException e) {
            fail("IOException occurred during classification");
        }
    }

    @Test
    void testClassifierNonHoliday() {
        Transaction normalTransaction = new Transaction(LocalDate.of(2024, 3, 1), null, 5.0, "Coffee");

        try {
            Transaction classified = classifier.classify(normalTransaction);
            assertNotEquals("Holiday", classified.getCategory());
        } catch (IOException e) {
            fail("IOException occurred during classification");
        }
    }

    @Test
    void testClassifierAlreadyClassified() {
        Transaction preClassified = new Transaction(LocalDate.now(), "Shopping", 50.0, "Clothes");

        try {
            Transaction result = classifier.classify(preClassified);
            assertEquals("Shopping", result.getCategory());
        } catch (IOException e) {
            fail("IOException occurred during classification");
        }
    }

    // TransactionController Tests
    @Test
    void testAddAndRetrieveTransaction() throws IOException {
        Transaction transaction = new Transaction(LocalDate.now(), "Food", 15.0, "Dinner");

        controller.addTransaction(transaction);
        List<Transaction> transactions = controller.getTransactions();

        assertEquals(1, transactions.size());
        assertEquals(transaction, transactions.get(0));
    }

    @Test
    void testAddMultipleTransactions() throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(LocalDate.now(), "Food", 10.0, "Lunch"));
        transactions.add(new Transaction(LocalDate.now(), "Transportation", 5.0, "Bus"));

        controller.addTransactions(transactions);
        assertEquals(2, controller.getTransactions().size());
    }

    @Test
    void testUpdateCategory() throws IOException {
        Transaction transaction = new Transaction(LocalDate.now(), null, 20.0, "Movie tickets");
        controller.addTransaction(transaction);

        controller.updateCategory(transaction, "Entertainment");
        assertEquals("Entertainment", transaction.getCategory());
    }

    @Test
    void testDeleteTransaction() throws IOException {
        Transaction t1 = new Transaction(LocalDate.now(), "Food", 10.0, "Lunch");
        Transaction t2 = new Transaction(LocalDate.now(), "Transportation", 5.0, "Bus");

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
    void testMonthlyExpensesCalculation() throws IOException {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        // Add transactions for current month
        controller.addTransaction(new Transaction(
                LocalDate.of(currentYear, currentMonth, 1), "Food", 10.0, "Breakfast"));
        controller.addTransaction(new Transaction(
                LocalDate.of(currentYear, currentMonth, 15), "Food", 15.0, "Lunch"));
        controller.addTransaction(new Transaction(
                LocalDate.of(currentYear, currentMonth, 20), "Transportation", 5.0, "Bus"));

        // Add transaction from different month (shouldn't be counted)
        controller.addTransaction(new Transaction(
                LocalDate.of(currentYear, currentMonth - 1, 25), "Shopping", 50.0, "Clothes"));

        Map<String, Double> expenses = controller.getMonthlyExpenses(currentYear, currentMonth);

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
}