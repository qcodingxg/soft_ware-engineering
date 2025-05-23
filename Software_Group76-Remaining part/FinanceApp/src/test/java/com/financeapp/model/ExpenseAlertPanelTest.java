package com.financeapp.model;

import com.financeapp.controller.TransactionController;
import com.financeapp.view.ExpenseAlertPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExpenseAlertPanelTest {

    private ExpenseAlertPanel expenseAlertPanel;
    private TransactionController transactionController;

    @BeforeEach
    public void setUp() {
        // Mock the TransactionController object
        transactionController = mock(TransactionController.class);
        // Do not load settings to avoid file lookup
        expenseAlertPanel = new ExpenseAlertPanel(transactionController, false);
    }
    @Test
    public void testGetCurrentMonthExpense() {
        // Mock the current date
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();

        // Create mock transaction data (using the four - parameter constructor)
        Transaction transaction1 = new Transaction(
                LocalDate.of(currentYear, currentMonth, 1),    // Date
                "Food",                                      // Category
                100.0,                                      // Amount
                "Lunch"                                      // Description
        );
        Transaction transaction2 = new Transaction(
                LocalDate.of(currentYear, currentMonth, 15),   // Date
                "Good",                                      // Category
                200.0,                                      // Amount
                "Buy something"                                  // Description
        );
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);

        // When the getTransactions method is called, return the mock data
        when(transactionController.getTransactions()).thenReturn(transactions);

        // Call the method to be tested
        double result = expenseAlertPanel.getCurrentMonthExpenseForTest();

        // Verify the result
        assertEquals(300.0, result, 0.001);
    }

    @Test
    public void testAlertTriggeredWhenThresholdExceeded() {
        // Mock the current date
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();

        // Set the category threshold (set the threshold for the dining category to 200.0)
        expenseAlertPanel.setCategoryThreshold("Dining", 200.0);

        // Create mock transaction data (total expenditure is 300.0, exceeding the threshold)
        Transaction transaction1 = new Transaction(
                LocalDate.of(currentYear, currentMonth, 1),
                "Dining",
                150.0,
                "Lunch"
        );
        Transaction transaction2 = new Transaction(
                LocalDate.of(currentYear, currentMonth, 15),
                "Dining",
                150.0,
                "Dinner"
        );
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);

        // When the getTransactions method is called, return the mock data
        when(transactionController.getTransactions()).thenReturn(transactions);

        // Call the update method (assuming this method checks the threshold and updates the alert status)
        expenseAlertPanel.updateExpenseData();

        // Verify if the alert is triggered
        boolean isAlertTriggered = expenseAlertPanel.isCategoryOverThreshold("Dining");
        assertEquals(true, isAlertTriggered, "An alert should be triggered when the expenditure exceeds the threshold");
    }

    @Test
    public void testAddAndRemoveCategoryThreshold() {
        // In the initial state, check if the threshold does not exist
        assertFalse(expenseAlertPanel.hasCategoryThreshold("Entertainment"), "There should be no entertainment category threshold in the initial state");

        // Add a threshold
        expenseAlertPanel.setCategoryThreshold("Entertainment", 1500.0);
        assertTrue(expenseAlertPanel.hasCategoryThreshold("Entertainment"), "There should be an entertainment category threshold after adding the threshold");
        assertEquals(1500.0, expenseAlertPanel.getCategoryThreshold("Entertainment"), 0.001, "The threshold should be set to 1500.0");

        // Remove the threshold
        expenseAlertPanel.removeCategoryThreshold("Entertainment");
        assertFalse(expenseAlertPanel.hasCategoryThreshold("Entertainment"), "There should be no entertainment category threshold after removing the threshold");
    }

    @Test
    public void testSetNegativeThreshold() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            expenseAlertPanel.setCategoryThreshold("Food", -100.0);
        });
        assertEquals("Threshold cannot be negative", exception.getMessage());
    }

    @Test
    public void testSetNullCategory() {
        // Verify no threshold exists for test category before invocation
        assertFalse(expenseAlertPanel.hasCategoryThreshold("TestCategory"));

        try {
            // Invoke method with null category, expecting exception
            expenseAlertPanel.setCategoryThreshold(null, 100.0);

            // Fail test if no exception is thrown
            fail("Expected IllegalArgumentException when category is null");
        } catch (IllegalArgumentException e) {
            // Validate exception message (optional)
            assertEquals("Category cannot be null or empty", e.getMessage());
        }

        // Verify no threshold was created after exception
        assertFalse(expenseAlertPanel.hasCategoryThreshold("TestCategory"),
                "Null category should not create any threshold entry");
    }
}





