package com.financeapp.controller;

import com.financeapp.model.SimpleBudgetCalculator;
import com.financeapp.model.Transaction;
import com.financeapp.util.CSVHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Transaction Controller
 * Processes business logic between models and views
 */
public class TransactionController {
    
    private final List<Transaction> transactions;
    private final CSVHandler csvHandler;
    private final SimpleBudgetCalculator budgetCalculator;
    
    /**
     * Constructor
     */
    public TransactionController() {
        this.transactions = new ArrayList<>();
        this.csvHandler = new CSVHandler();
        this.budgetCalculator = new SimpleBudgetCalculator();
    }
    
    /**
     * Get all transactions
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    /**
     * Load transaction data
     * @throws IOException if an IO error occurs during loading
     */
    public void loadTransactions() throws IOException {
        transactions.clear();
        transactions.addAll(csvHandler.loadTransactions());
    }
    
    /**
     * Save transaction data
     * @throws IOException if an IO error occurs during saving
     */
    public void saveTransactions() throws IOException {
        csvHandler.saveTransactions(transactions);
    }
    
    /**
     * Add a transaction
     * @param transaction Transaction to add
     * @throws IOException if an IO error occurs during saving
     */
    public void addTransaction(Transaction transaction) throws IOException {
        transactions.add(transaction);
        saveTransactions();
    }
    
    /**
     * Add multiple transactions
     * @param newTransactions Transactions to add
     * @throws IOException if an IO error occurs during saving
     */
    public void addTransactions(List<Transaction> newTransactions) throws IOException {
        transactions.addAll(newTransactions);
        saveTransactions();
    }
    
    /**
     * Update transaction category
     * @param transaction Transaction to update
     * @param newCategory New category
     * @throws IOException if an IO error occurs during saving
     */
    public void updateCategory(Transaction transaction, String newCategory) throws IOException {
        transaction.setCategory(newCategory);
        saveTransactions();
    }
    
    /**
     * Delete a transaction
     * @param transaction Transaction to delete
     * @throws IOException if an IO error occurs during saving
     */
    public void deleteTransaction(Transaction transaction) throws IOException {
        transactions.remove(transaction);
        saveTransactions();
    }
    
    /**
     * Get current month expenses
     * @return Map of expenses by category
     */
    public Map<String, Double> getCurrentMonthExpenses() {
        LocalDate now = LocalDate.now();
        return getMonthlyExpenses(now.getYear(), now.getMonthValue());
    }
    
    /**
     * Get monthly expenses
     * @param year Year
     * @param month Month
     * @return Map of expenses by category
     */
    public Map<String, Double> getMonthlyExpenses(int year, int month) {
        return budgetCalculator.calculateMonthlyExpenses(transactions, year, month);
    }
    
    /**
     * Get historical average expenses
     * @return Map of average expenses by category
     */
    public Map<String, Double> getHistoricalAverage() {
        return budgetCalculator.calculateHistoricalAverage(transactions, 3);
    }
    
    /**
     * Get budget suggestions
     * @return List of suggestions
     */
    public List<String> getBudgetSuggestions() {
        Map<String, Double> currentExpenses = getCurrentMonthExpenses();
        Map<String, Double> historicalAverage = getHistoricalAverage();
        
        return budgetCalculator.generateBudgetSuggestions(currentExpenses, historicalAverage);
    }
    
    /**
     * Get the budget calculator
     * @return The budget calculator instance
     */
    public SimpleBudgetCalculator getBudgetCalculator() {
        return budgetCalculator;
    }
} 