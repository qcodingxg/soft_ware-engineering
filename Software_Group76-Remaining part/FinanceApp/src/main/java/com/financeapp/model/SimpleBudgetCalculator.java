package com.financeapp.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple Budget Calculator
 * Calculates monthly expenses and generates budget suggestions
 */
public class SimpleBudgetCalculator {
    
    // Warning threshold ratio (current expenses exceeding historical average)
    private static final double THRESHOLD_RATIO = 1.2; // 20% over threshold triggers warning
    
    /**
     * Calculate monthly expenses
     * @param transactions List of transactions
     * @param year Year to calculate for
     * @param month Month to calculate for
     * @return Map of expenses by category
     */
    public Map<String, Double> calculateMonthlyExpenses(List<Transaction> transactions, int year, int month) {
        // Filter transactions for the specified month
        YearMonth targetMonth = YearMonth.of(year, month);
        List<Transaction> monthlyTransactions = transactions.stream()
            .filter(t -> YearMonth.from(t.getDate()).equals(targetMonth))
            .collect(Collectors.toList());
        
        // Group transactions by category and sum amounts
        Map<String, Double> expensesByCategory = monthlyTransactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.summingDouble(Transaction::getAmount)
            ));
        
        // Sort by amount in descending order
        return expensesByCategory.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    /**
     * Calculate expenses in a date range
     * @param transactions List of transactions
     * @param startDate Start date
     * @param endDate End date
     * @return Map of expenses by category
     */
    public Map<String, Double> calculateExpensesInRange(List<Transaction> transactions, 
            LocalDate startDate, LocalDate endDate) {
        // Filter transactions within the date range
        List<Transaction> rangeTransactions = transactions.stream()
            .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
            .collect(Collectors.toList());
        
        // Group transactions by category and sum amounts
        Map<String, Double> expensesByCategory = rangeTransactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.summingDouble(Transaction::getAmount)
            ));
        
        // Sort by amount in descending order
        return expensesByCategory.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    /**
     * Generate budget suggestions
     * @param currentExpenses Current month expenses
     * @param historicalAverage Historical average expenses
     * @return List of suggestions
     */
    public List<String> generateBudgetSuggestions(Map<String, Double> currentExpenses, 
            Map<String, Double> historicalAverage) {
        List<String> suggestions = new ArrayList<>();
        
        if (currentExpenses.isEmpty()) {
            suggestions.add("No expenses recorded for the current month.");
            return suggestions;
        }
        
        // Calculate total expenses
        double totalCurrent = currentExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalHistorical = historicalAverage.values().stream().mapToDouble(Double::doubleValue).sum();
        
        // Compare total expenses
        if (totalCurrent > totalHistorical * 1.2) {
            suggestions.add(String.format("Total expenses (%.2f) are 20%% higher than historical average (%.2f).", 
                    totalCurrent, totalHistorical));
        } else if (totalCurrent < totalHistorical * 0.8) {
            suggestions.add(String.format("Total expenses (%.2f) are 20%% lower than historical average (%.2f).", 
                    totalCurrent, totalHistorical));
        }
        
        // Compare expenses by category
        for (Map.Entry<String, Double> entry : currentExpenses.entrySet()) {
            String category = entry.getKey();
            double currentAmount = entry.getValue();
            Double historicalAmount = historicalAverage.get(category);
            
            if (historicalAmount != null) {
                if (currentAmount > historicalAmount * 1.2) {
                    suggestions.add(String.format("%s expenses (%.2f) are 20%% higher than historical average (%.2f).", 
                            category, currentAmount, historicalAmount));
                } else if (currentAmount < historicalAmount * 0.8) {
                    suggestions.add(String.format("%s expenses (%.2f) are 20%% lower than historical average (%.2f).", 
                            category, currentAmount, historicalAmount));
                }
            }
        }
        
        return suggestions;
    }
    
    /**
     * Calculate historical average expenses
     * @param transactions List of transactions
     * @param months Number of months to consider
     * @return Map of average expenses by category
     */
    public Map<String, Double> calculateHistoricalAverage(List<Transaction> transactions, int months) {
        if (transactions.isEmpty()) {
            return new HashMap<>();
        }
        
        // Get the most recent date
        LocalDate latestDate = transactions.stream()
            .map(Transaction::getDate)
            .max(LocalDate::compareTo)
            .orElse(LocalDate.now());
        
        // Calculate start date
        LocalDate startDate = latestDate.minusMonths(months - 1).withDayOfMonth(1);
        
        // Filter transactions within the date range
        List<Transaction> historicalTransactions = transactions.stream()
            .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(latestDate))
            .collect(Collectors.toList());
        
        // Group transactions by category and calculate average
        Map<String, Double> totalByCategory = historicalTransactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.summingDouble(Transaction::getAmount)
            ));
        
        // Calculate average for each category
        Map<String, Double> averageByCategory = new HashMap<>();
        for (Map.Entry<String, Double> entry : totalByCategory.entrySet()) {
            averageByCategory.put(entry.getKey(), entry.getValue() / months);
        }
        
        // Sort by amount in descending order
        return averageByCategory.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    /**
     * Detect expense alerts
     * @param currentExpenses Current expenses
     * @param thresholds Threshold settings
     * @return List of alert messages
     */
    public List<String> detectExpenseAlerts(Map<String, Double> currentExpenses, Map<String, Double> thresholds) {
        List<String> alerts = new ArrayList<>();
        
        if (currentExpenses.isEmpty()) {
            return alerts;
        }
        
        // Calculate total expenses
        double totalExpense = currentExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
        
        // Check global threshold (if provided)
        if (thresholds.containsKey("_GLOBAL_")) {
            double globalThreshold = thresholds.get("_GLOBAL_");
            if (totalExpense > globalThreshold) {
                alerts.add(String.format("Total expenses (%.2f) have exceeded the global threshold (%.2f)", 
                        totalExpense, globalThreshold));
            }
        }
        
        // Check category thresholds
        for (Map.Entry<String, Double> entry : currentExpenses.entrySet()) {
            String category = entry.getKey();
            double amount = entry.getValue();
            
            // Skip global settings
            if ("_GLOBAL_".equals(category)) {
                continue;
            }
            
            // Check specific category threshold
            if (thresholds.containsKey(category)) {
                double threshold = thresholds.get(category);
                if (amount > threshold) {
                    alerts.add(String.format("Category '%s' expenses (%.2f) have exceeded the threshold (%.2f)", 
                            category, amount, threshold));
                }
            }
        }
        
        return alerts;
    }
    
    /**
     * Detect if expenses exceed historical average
     * @param currentExpenses Current expenses
     * @param historicalAverage Historical average
     * @return List of alert messages
     */
    public List<String> detectAbnormalExpenses(Map<String, Double> currentExpenses, 
            Map<String, Double> historicalAverage) {
        List<String> alerts = new ArrayList<>();
        
        if (currentExpenses.isEmpty() || historicalAverage.isEmpty()) {
            return alerts;
        }
        
        // Calculate total expenses
        double totalCurrent = currentExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalHistorical = historicalAverage.values().stream().mapToDouble(Double::doubleValue).sum();
        
        // Check if total expenses are abnormal
        if (totalCurrent > totalHistorical * THRESHOLD_RATIO) {
            alerts.add(String.format("Total expenses (%.2f) have exceeded historical average (%.2f) by %.0f%%", 
                    totalCurrent, totalHistorical, (THRESHOLD_RATIO - 1) * 100));
        }
        
        // Check each category
        for (Map.Entry<String, Double> entry : currentExpenses.entrySet()) {
            String category = entry.getKey();
            double amount = entry.getValue();
            
            if (historicalAverage.containsKey(category)) {
                double avgAmount = historicalAverage.get(category);
                if (amount > avgAmount * THRESHOLD_RATIO) {
                    alerts.add(String.format("Category '%s' expenses (%.2f) have exceeded historical average (%.2f) by %.0f%%", 
                            category, amount, avgAmount, (THRESHOLD_RATIO - 1) * 100));
                }
            }
        }
        
        return alerts;
    }
    
    /**
     * Detect trend-based expense alerts
     * Analyzes spending patterns over time to detect concerning trends
     * @param transactions List of all transactions
     * @param monthsToAnalyze Number of months to analyze for trends
     * @return List of trend alert messages
     */
    public List<String> detectTrendAlerts(List<Transaction> transactions, int monthsToAnalyze) {
        List<String> alerts = new ArrayList<>();
        
        if (transactions.isEmpty() || monthsToAnalyze < 2) {
            return alerts;
        }
        
        // Get the most recent date
        LocalDate latestDate = transactions.stream()
            .map(Transaction::getDate)
            .max(LocalDate::compareTo)
            .orElse(LocalDate.now());
        
        // Analyze monthly spending for the last several months
        Map<String, List<Double>> categoryTrends = new HashMap<>();
        Map<Integer, Double> totalByMonth = new HashMap<>();
        
        // Process each month
        for (int i = 0; i < monthsToAnalyze; i++) {
            LocalDate monthStart = latestDate.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            
            // Get transactions for this month
            List<Transaction> monthTransactions = transactions.stream()
                .filter(t -> !t.getDate().isBefore(monthStart) && !t.getDate().isAfter(monthEnd))
                .collect(Collectors.toList());
                
            // Calculate total for this month
            double monthTotal = monthTransactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
            totalByMonth.put(i, monthTotal);
            
            // Group by category
            Map<String, Double> monthCategoryTotals = monthTransactions.stream()
                .collect(Collectors.groupingBy(
                    Transaction::getCategory,
                    Collectors.summingDouble(Transaction::getAmount)
                ));
                
            // Add to trends map
            for (Map.Entry<String, Double> entry : monthCategoryTotals.entrySet()) {
                String category = entry.getKey();
                Double amount = entry.getValue();
                
                categoryTrends.putIfAbsent(category, new ArrayList<>());
                List<Double> trend = categoryTrends.get(category);
                
                // Ensure we have a value for each month (padding with zeros if needed)
                while (trend.size() < i) {
                    trend.add(0.0);
                }
                trend.add(amount);
            }
        }
        
        // Analyze total spending trend
        boolean consistentIncrease = true;
        for (int i = 1; i < monthsToAnalyze; i++) {
            double currentMonth = totalByMonth.getOrDefault(monthsToAnalyze - i - 1, 0.0);
            double previousMonth = totalByMonth.getOrDefault(monthsToAnalyze - i, 0.0);
            
            if (previousMonth == 0.0 || currentMonth <= previousMonth) {
                consistentIncrease = false;
                break;
            }
        }
        
        if (consistentIncrease && monthsToAnalyze >= 3) {
            alerts.add(String.format("WARNING: Your total spending has consistently increased over the last %d months!", 
                    monthsToAnalyze));
        }
        
        // Analyze category trends
        for (Map.Entry<String, List<Double>> entry : categoryTrends.entrySet()) {
            String category = entry.getKey();
            List<Double> trend = entry.getValue();
            
            // Need at least 3 months of data for meaningful trend analysis
            if (trend.size() >= 3) {
                boolean categoryIncreasing = true;
                double increaseRate = 0.0;
                
                // Check if spending consistently increases
                for (int i = 1; i < trend.size(); i++) {
                    if (trend.get(i) <= trend.get(i-1)) {
                        categoryIncreasing = false;
                        break;
                    }
                    
                    // Calculate rate of increase
                    if (trend.get(i-1) > 0) {
                        double rate = (trend.get(i) - trend.get(i-1)) / trend.get(i-1);
                        increaseRate = Math.max(increaseRate, rate);
                    }
                }
                
                if (categoryIncreasing && increaseRate > 0.1) { // 10% increase rate threshold
                    alerts.add(String.format("TREND ALERT: '%s' expenses have been consistently increasing (up to %.0f%% month-to-month)!", 
                            category, increaseRate * 100));
                }
            }
        }
        
        // Check for seasonal patterns
        detectSeasonalPatterns(transactions, alerts);
        
        return alerts;
    }
    
    /**
     * Detect seasonal spending patterns
     * @param transactions List of all transactions
     * @param alerts List to add alerts to
     */
    private void detectSeasonalPatterns(List<Transaction> transactions, List<String> alerts) {
        if (transactions.isEmpty()) {
            return;
        }
        
        // Get current month
        int currentMonth = LocalDate.now().getMonthValue();
        
        // Group transactions by month and year
        Map<String, Map<String, Double>> spendingByMonthYear = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            LocalDate date = transaction.getDate();
            String monthYear = date.getYear() + "-" + date.getMonthValue();
            String category = transaction.getCategory();
            double amount = transaction.getAmount();
            
            spendingByMonthYear.putIfAbsent(monthYear, new HashMap<>());
            Map<String, Double> categoryMap = spendingByMonthYear.get(monthYear);
            
            categoryMap.put(category, categoryMap.getOrDefault(category, 0.0) + amount);
        }
        
        // Analyze historical spending for current month across years
        Map<Integer, Map<String, Double>> spendingByMonth = new HashMap<>();
        
        for (Map.Entry<String, Map<String, Double>> entry : spendingByMonthYear.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int month = Integer.parseInt(parts[1]);
            
            spendingByMonth.putIfAbsent(month, new HashMap<>());
            Map<String, Double> monthData = spendingByMonth.get(month);
            
            // Aggregate category spending for this month across years
            for (Map.Entry<String, Double> categoryEntry : entry.getValue().entrySet()) {
                String category = categoryEntry.getKey();
                double amount = categoryEntry.getValue();
                
                monthData.put(category, monthData.getOrDefault(category, 0.0) + amount);
            }
        }
        
        // Get historical data for previous month
        Map<String, Double> currentMonthHistory = spendingByMonth.getOrDefault(currentMonth, new HashMap<>());
        
        // Get current month transactions
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        
        List<Transaction> currentMonthTransactions = transactions.stream()
            .filter(t -> !t.getDate().isBefore(monthStart) && !t.getDate().isAfter(monthEnd))
            .collect(Collectors.toList());
            
        Map<String, Double> currentSpending = currentMonthTransactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.summingDouble(Transaction::getAmount)
            ));
            
        // Compare with historical patterns
        for (Map.Entry<String, Double> entry : currentSpending.entrySet()) {
            String category = entry.getKey();
            double amount = entry.getValue();
            
            double historicalAmount = currentMonthHistory.getOrDefault(category, 0.0) / 
                                     Math.max(1, spendingByMonthYear.size() / 12); // Average by number of years
            
            if (amount > historicalAmount * 1.5 && amount > 1000) { // 50% higher and significant amount
                alerts.add(String.format("SEASONAL ALERT: '%s' spending (%.2f) is much higher than usual for this month (avg: %.2f)!", 
                        category, amount, historicalAmount));
            }
        }
    }
} 