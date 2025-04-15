package com.financeapp.view;

import com.financeapp.model.Transaction;
import com.financeapp.service.TransactionService;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class LocalizedFinancePanel extends JPanel {
    private TransactionService transactionService;
    private JTable transactionTable;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> monthFilter;
    private JLabel totalIncomeLabel;
    private JLabel totalExpenseLabel;
    private JLabel balanceLabel;
    private JPanel chartPanel;
    private JTextArea analysisArea;

    public LocalizedFinancePanel(TransactionService transactionService) {
        this.transactionService = transactionService;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Create top filter panel
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);

        // Create middle content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        // Create transaction table
        String[] columns = {"Date", "Category", "Amount", "Description"};
        transactionTable = new JTable(new Object[][]{}, columns);
        JScrollPane tableScrollPane = new JScrollPane(transactionTable);
        contentPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Create right analysis panel
        JPanel analysisPanel = createAnalysisPanel();
        contentPanel.add(analysisPanel, BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);

        // Create bottom statistics panel
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.SOUTH);
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Category filter
        categoryFilter = new JComboBox<>();
        categoryFilter.addItem("All Categories");
        categoryFilter.addItem("Food");
        categoryFilter.addItem("Transportation");
        categoryFilter.addItem("Shopping");
        categoryFilter.addItem("Entertainment");
        categoryFilter.addItem("Utilities");
        categoryFilter.addItem("Housing");
        categoryFilter.addItem("Investment");
        categoryFilter.addItem("Insurance");
        categoryFilter.addItem("Education");
        categoryFilter.addItem("Healthcare");
        categoryFilter.addItem("Gift");
        categoryFilter.addItem("Social");
        categoryFilter.addItem("Holiday");

        // Month filter
        monthFilter = new JComboBox<>();
        monthFilter.addItem("All Months");
        for (int i = 1; i <= 12; i++) {
            monthFilter.addItem(String.format("%02d", i));
        }

        panel.add(new JLabel("Category:"));
        panel.add(categoryFilter);
        panel.add(new JLabel("Month:"));
        panel.add(monthFilter);

        // Add filter event listeners
        categoryFilter.addActionListener(e -> filterTransactions());
        monthFilter.addActionListener(e -> filterTransactions());

        return panel;
    }

    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Financial Analysis"));

        // Create analysis text area
        analysisArea = new JTextArea();
        analysisArea.setEditable(false);
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(analysisArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        totalIncomeLabel = new JLabel("Total Income: ¥0.00");
        totalExpenseLabel = new JLabel("Total Expense: ¥0.00");
        balanceLabel = new JLabel("Balance: ¥0.00");

        panel.add(totalIncomeLabel);
        panel.add(totalExpenseLabel);
        panel.add(balanceLabel);

        return panel;
    }

    private void loadData() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        updateTable(transactions);
        updateStats(transactions);
        generateAnalysis(transactions);
    }

    private void updateTable(List<Transaction> transactions) {
        Object[][] data = new Object[transactions.size()][4];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            data[i][0] = t.getDate().format(formatter);
            data[i][1] = t.getCategory();
            data[i][2] = String.format("¥%.2f", t.getAmount());
            data[i][3] = t.getDescription();
        }

        transactionTable.setModel(new javax.swing.table.DefaultTableModel(data, 
            new String[]{"Date", "Category", "Amount", "Description"}));
    }

    private void updateStats(List<Transaction> transactions) {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction t : transactions) {
            if (t.getAmount() > 0) {
                totalIncome += t.getAmount();
            } else {
                totalExpense += Math.abs(t.getAmount());
            }
        }

        totalIncomeLabel.setText(String.format("Total Income: ¥%.2f", totalIncome));
        totalExpenseLabel.setText(String.format("Total Expense: ¥%.2f", totalExpense));
        balanceLabel.setText(String.format("Balance: ¥%.2f", totalIncome - totalExpense));
    }

    private void generateAnalysis(List<Transaction> transactions) {
        StringBuilder analysis = new StringBuilder();
        
        // Calculate expenses by category
        Map<String, Double> categoryExpenses = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getAmount() < 0) {
                String category = t.getCategory();
                categoryExpenses.merge(category, Math.abs(t.getAmount()), Double::sum);
            }
        }

        // Find category with highest expenses
        Optional<Map.Entry<String, Double>> maxExpense = categoryExpenses.entrySet().stream()
            .max(Map.Entry.comparingByValue());
        
        if (maxExpense.isPresent()) {
            analysis.append("Top Expense Category: ").append(maxExpense.get().getKey())
                   .append(" (¥").append(String.format("%.2f", maxExpense.get().getValue()))
                   .append(")\n\n");
        }

        // Analyze festival expenses
        double holidayExpense = categoryExpenses.getOrDefault("Holiday", 0.0);
        if (holidayExpense > 0) {
            analysis.append("Holiday Expenses: ¥").append(String.format("%.2f", holidayExpense))
                   .append("\n\n");
        }

        // Analyze investment situation
        double investmentExpense = categoryExpenses.getOrDefault("Investment", 0.0);
        if (investmentExpense > 0) {
            analysis.append("Investment Analysis:\n")
                   .append("Total Investment: ¥").append(String.format("%.2f", investmentExpense))
                   .append("\n\n");
        }

        // Analyze housing expenses
        double housingExpense = categoryExpenses.getOrDefault("Housing", 0.0);
        if (housingExpense > 0) {
            analysis.append("Housing Analysis:\n")
                   .append("Total Housing Expense: ¥").append(String.format("%.2f", housingExpense))
                   .append("\n\n");
        }

        // Analyze education expenses
        double educationExpense = categoryExpenses.getOrDefault("Education", 0.0);
        if (educationExpense > 0) {
            analysis.append("Education Analysis:\n")
                   .append("Total Education Expense: ¥").append(String.format("%.2f", educationExpense))
                   .append("\n\n");
        }

        analysisArea.setText(analysis.toString());
    }

    private void filterTransactions() {
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        String selectedMonth = (String) monthFilter.getSelectedItem();
        
        List<Transaction> filteredTransactions = transactionService.getAllTransactions().stream()
            .filter(t -> selectedCategory.equals("All Categories") || 
                        t.getCategory().equals(selectedCategory))
            .filter(t -> selectedMonth.equals("All Months") || 
                        String.format("%02d", t.getDate().getMonthValue()).equals(selectedMonth))
            .toList();

        updateTable(filteredTransactions);
        updateStats(filteredTransactions);
        generateAnalysis(filteredTransactions);
    }
} 