package com.financeapp.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Budget Calculator Interface
 * Used to calculate budgets and expense statistics
 * Reserved for future extensions
 */
public interface BudgetCalculator {
    
    /**
     * Calculate monthly expenses
     * @param transactions All transactions
     * @param year Year
     * @param month Month
     * @return Map of expenses by category
     */
    Map<String, Double> calculateMonthlyExpenses(List<Transaction> transactions, int year, int month);
    
    /**
     * Calculate expenses within date range
     * @param transactions All transactions
     * @param startDate Start date
     * @param endDate End date
     * @return Map of expenses by category
     */
    Map<String, Double> calculateExpensesInRange(List<Transaction> transactions, LocalDate startDate, LocalDate endDate);
    
    /**
     * Generate budget suggestions
     * @param expenses Current expenses
     * @param historicalAverage Historical average expenses
     * @return Budget suggestions
     */
    List<String> generateBudgetSuggestions(Map<String, Double> expenses, Map<String, Double> historicalAverage);
} 