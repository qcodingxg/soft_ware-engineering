package com.financeapp.view;

import com.financeapp.controller.TransactionController;
import com.financeapp.model.AlertService;
import com.financeapp.model.AlertService.Alert;
import com.financeapp.model.AlertService.AlertLevel;
import com.financeapp.model.Transaction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Properties;

/**
 * Expense Alert Panel
 * Displays alert information when individual category or total expenses exceed set thresholds
 */
public class ExpenseAlertPanel extends JPanel {

    private final TransactionController transactionController;
    private AlertService alertService = null;

    // UI Components
    private JPanel topAlertPanel;
    private JPanel bottomSettingsPanel;
    private JPanel globalSettingsPanel;
    private JPanel categoryThresholdsPanel;
    private JButton viewAllAlertsButton;
    private JButton saveSettingsButton;
    private JButton refreshButton;
    private JButton historyAlarmsButton;
    private JButton detailsButton;
    private JCheckBox enableAlertsCheckbox;
    private JTextField globalThresholdField;
    private JTable categoryThresholdsTable;
    private DefaultTableModel categoryThresholdsModel;
    private JButton addCategoryButton;
    private JButton removeCategoryButton;

    // Colors (matching application theme)
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color CARD_BG_COLOR = new Color(255, 255, 255);

    // Default Settings
    private boolean alertsEnabled = true;
    private double globalThreshold = 5000.0; // Default global threshold
    private Map<String, Double> categoryThresholds = new HashMap<>(); // Thresholds for each category
    private static final String SETTINGS_FILE = "settings.properties";

    /**
     * Constructor
     * @param transactionController Transaction controller
     */
    public ExpenseAlertPanel(TransactionController transactionController) {
        this.transactionController = transactionController;
        this.alertService = new AlertService();

        // Register for GUI notifications
        this.alertService.registerNotificationHandler(
                new AlertService.GuiNotificationHandler(this::updateExpenseAlerts)
        );

        initUI();
        loadSettings();
    }

    /**
     * Initialize UI
     */
    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top alert display area
        topAlertPanel = createTopAlertPanel();
        add(topAlertPanel, BorderLayout.NORTH);

        // Bottom settings area
        bottomSettingsPanel = createBottomSettingsPanel();
        add(bottomSettingsPanel, BorderLayout.CENTER);

        // Refresh button in the top right corner
        refreshButton = createStyledButton("Refresh Alerts");
        refreshButton.addActionListener(e -> updateExpenseAlerts());
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshPanel.setOpaque(false);
        refreshPanel.add(refreshButton);

        // New history alarms button
        historyAlarmsButton = createStyledButton("History Alarms");
        historyAlarmsButton.addActionListener(e -> showHistoryAlarms());
        refreshPanel.add(historyAlarmsButton);

        // New Details button
        detailsButton = createStyledButton("Details");
        detailsButton.addActionListener(e -> showDetails());
        refreshPanel.add(detailsButton);

        add(refreshPanel, BorderLayout.SOUTH);

        // Initial data update
        updateExpenseAlerts();
    }

    /**
     * Create the top alert display area
     */
    private JPanel createTopAlertPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        // Alert content panel
        JPanel alertsContent = new JPanel();
        alertsContent.setLayout(new BoxLayout(alertsContent, BoxLayout.Y_AXIS));
        alertsContent.setOpaque(false);

        JScrollPane alertsScrollPane = new JScrollPane(alertsContent);
        alertsScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.add(alertsScrollPane, BorderLayout.CENTER);

        // View all alerts button in the bottom right corner
        viewAllAlertsButton = createStyledButton("View All Alerts");
        viewAllAlertsButton.addActionListener(e -> showAllAlerts());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(viewAllAlertsButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create the bottom settings area
     */
    private JPanel createBottomSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);

        // Global settings section
        globalSettingsPanel = createGlobalSettingsPanel();
        panel.add(globalSettingsPanel, BorderLayout.NORTH);

        // Category threshold settings section
        categoryThresholdsPanel = createCategoryThresholdsPanel();
        panel.add(categoryThresholdsPanel, BorderLayout.CENTER);

        // Save settings button in the bottom right corner
        saveSettingsButton = createStyledButton("Save Settings");
        saveSettingsButton.addActionListener(e -> saveSettings());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(saveSettingsButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create the global settings section
     */
    private JPanel createGlobalSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        // Checkbox to enable/disable alerts
        enableAlertsCheckbox = new JCheckBox("Enable Alerts", alertsEnabled);
        enableAlertsCheckbox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        enableAlertsCheckbox.setOpaque(false);
        panel.add(enableAlertsCheckbox);

        // Global expense alert threshold setting
        JLabel globalThresholdLabel = new JLabel("Global Threshold (¥):");
        globalThresholdLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(globalThresholdLabel);

        globalThresholdField = new JTextField(String.valueOf(globalThreshold), 10);
        globalThresholdField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(globalThresholdField);

        return panel;
    }

    /**
     * Create the category threshold settings section
     */
    private JPanel createCategoryThresholdsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(15, 15));
        panel.setOpaque(false);

        // Display category thresholds in a table
        String[] columns = {"Category", "Threshold (¥)"};
        categoryThresholdsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only the threshold column is editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        categoryThresholdsTable = new JTable(categoryThresholdsModel);
        categoryThresholdsTable.setRowHeight(25);
        categoryThresholdsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        categoryThresholdsTable.getTableHeader().setBackground(SECONDARY_COLOR);
        categoryThresholdsTable.getTableHeader().setForeground(Color.WHITE);
        categoryThresholdsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column widths
        categoryThresholdsTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Category
        categoryThresholdsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Threshold

        // Set cell content to be centered
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        categoryThresholdsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        categoryThresholdsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(categoryThresholdsTable);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons to add and remove categories
        addCategoryButton = createStyledButton("Add Category");
        addCategoryButton.addActionListener(e -> addCategory());

        removeCategoryButton = createStyledButton("Remove Category");
        removeCategoryButton.addActionListener(e -> removeCategory());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(addCategoryButton);
        buttonPanel.add(removeCategoryButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        loadCategoryThresholds();
        return panel;
    }

    /**
     * Update expense alert information
     */
    private void updateExpenseAlerts() {
        // Check for alerts
        List<Alert> alerts = checkAlerts();

        // Clear the top alert display area
        JPanel alertsContent = (JPanel) ((JScrollPane) topAlertPanel.getComponent(0)).getViewport().getView();
        alertsContent.removeAll();

        // Display at most 3 latest alerts
        int count = 0;
        for (Alert alert : alerts) {
            if (alert != null && count < 3) {
                JPanel alertPanel = createAlertPanel(alert);
                alertsContent.add(alertPanel);
                alertsContent.add(Box.createVerticalStrut(10));
                count++;
            }
        }

        // If there are more than 3 alerts, display the number of remaining alerts
        int nonNullAlertsCount = (int) alerts.stream().filter(Objects::nonNull).count();
        if (nonNullAlertsCount > 3) {
            JLabel remainingLabel = new JLabel("+" + (nonNullAlertsCount - 3) + " more alerts");
            remainingLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            remainingLabel.setForeground(ERROR_COLOR);
            alertsContent.add(remainingLabel);
        }

        alertsContent.revalidate();
        alertsContent.repaint();
    }

    /**
     * Create a single alert panel
     */
    private JPanel createAlertPanel(Alert alert) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(CARD_BG_COLOR);
        panel.setBorder(BorderFactory.createLineBorder(ERROR_COLOR));

        // Warning icon
        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 24));
        iconLabel.setForeground(ERROR_COLOR);
        panel.add(iconLabel);

        // Alert text
        JLabel messageLabel = new JLabel(alert.getMessage());
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageLabel.setForeground(ERROR_COLOR);
        panel.add(messageLabel);

        // Date
        JLabel dateLabel = new JLabel(alert.getTimestamp().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dateLabel.setForeground(TEXT_COLOR);
        panel.add(dateLabel);

        return panel;
    }

    /**
     * View all alerts
     */
    private void showAllAlerts() {
        // Implement the logic to view all alerts
        List<Alert> alerts = checkAlerts();
        JTextArea textArea = new JTextArea();
        for (Alert alert : alerts) {
            if (alert != null) {
                textArea.append(alert.getTimestamp().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " - " + alert.getMessage() + "\n");
            }
        }
        JScrollPane scrollPane = new JScrollPane(textArea);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "All Alerts", false);
        dialog.add(scrollPane);

        // Set the dialog width to the optimal width of the content and fix the height
        dialog.setSize(scrollPane.getPreferredSize().width + 50, 300);
        dialog.setResizable(false); // Disable width adjustment

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Show historical alarms
     */
    private void showHistoryAlarms() {
        List<Alert> allAlerts = alertService.getAllAlerts();
        JTextArea textArea = new JTextArea();
        for (Alert alert : allAlerts) {
            textArea.append(alert.getTimestamp().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " - " + alert.getMessage() + "\n");
        }
        JScrollPane scrollPane = new JScrollPane(textArea);

        JButton clearHistoryButton = createStyledButton("Clearing History");
        clearHistoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alertService.clearAllAlerts();
                textArea.setText("");
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(clearHistoryButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "History Alarms", false);
        dialog.add(mainPanel);

        // Set the dialog width to the optimal width of the content and fix the height
        dialog.setSize(scrollPane.getPreferredSize().width + 50, 300);
        dialog.setResizable(false); // Disable width adjustment

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Save settings
     */
    private void saveSettings() {
        // Save global settings
        alertsEnabled = enableAlertsCheckbox.isSelected();
        try {
            globalThreshold = Double.parseDouble(globalThresholdField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid global threshold value", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update the categoryThresholds map
        for (int i = 0; i < categoryThresholdsModel.getRowCount(); i++) {
            String category = (String) categoryThresholdsModel.getValueAt(i, 0);
            Object thresholdObj = categoryThresholdsModel.getValueAt(i, 1);

            // Handle unset thresholds
            if ("Not Set".equals(thresholdObj) || thresholdObj == null || thresholdObj.toString().trim().isEmpty()) {
                categoryThresholds.put(category, null);
            } else {
                try {
                    double threshold = Double.parseDouble(thresholdObj.toString());
                    categoryThresholds.put(category, threshold);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid threshold for category: " + category,
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        // Save to file
        saveCategoryThresholds();

        // Refresh alerts
        updateExpenseAlerts();

        // Show save success message
        JOptionPane.showMessageDialog(this,
                "Settings saved successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Add a category
     */
    private void addCategory() {
        List<Transaction> transactions = transactionController.getTransactions();
        Set<String> allCategories = transactions.stream()
                .map(Transaction::getCategory)
                .filter(Objects::nonNull)
                .filter(category -> !category.isEmpty())
                .collect(Collectors.toSet());

        // Exclude already added categories
        allCategories.removeAll(categoryThresholds.keySet());

        if (allCategories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No new categories available to add.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] categoryArray = allCategories.toArray(new String[0]);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Category", "Select"}, categoryArray.length) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        for (int i = 0; i < categoryArray.length; i++) {
            model.setValueAt(categoryArray[i], i, 0);
            model.setValueAt(false, i, 1);
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);

        int result = JOptionPane.showConfirmDialog(this, scrollPane, "Select Categories to Add",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            List<String> selectedCategories = new ArrayList<>();
            for (int i = 0; i < table.getRowCount(); i++) {
                if (Boolean.TRUE.equals(table.getValueAt(i, 1))) {
                    selectedCategories.add((String) table.getValueAt(i, 0));
                }
            }

            if (!selectedCategories.isEmpty()) {
                // Set thresholds for each selected category
                for (String category : selectedCategories) {
                    // Pop up a dialog to set the threshold
                    String input = JOptionPane.showInputDialog(
                            this,
                            "Set threshold for category '" + category + "':",
                            "Set Threshold",
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (input != null) { // User clicked OK
                        input = input.trim();
                        if (input.isEmpty()) {
                            // Remove the category threshold if input is empty
                            categoryThresholds.remove(category);
                        } else {
                            try {
                                double threshold = Double.parseDouble(input);
                                // Round to two decimal places
                                threshold = Math.round(threshold * 100.0) / 100.0;

                                // Validate and set the threshold using the main method
                                setCategoryThreshold(category, threshold);
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(
                                        this,
                                        "Invalid threshold value. Please enter a valid number.",
                                        "Input Error",
                                        JOptionPane.ERROR_MESSAGE
                                );
                                // Do not set threshold, allow user to retry
                            }
                        }
                    } else { // User clicked Cancel
                        // Remove the category threshold if user cancels
                        categoryThresholds.remove(category);
                    }
                }

                // Update the threshold table
                updateThresholdTable();

                // Prompt the user to save settings
                JOptionPane.showMessageDialog(this,
                        "Categories added. Please save settings to apply changes.",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Remove a category
     */
    private void removeCategory() {
        int selectedRow = categoryThresholdsTable.getSelectedRow();
        if (selectedRow != -1) {
            String category = (String) categoryThresholdsModel.getValueAt(selectedRow, 0);
            categoryThresholdsModel.removeRow(selectedRow);
            categoryThresholds.remove(category);
            saveCategoryThresholds();
        }
    }

    /**
     * Check for alerts
     */
    private List<Alert> checkAlerts() {
        List<Alert> alerts = new ArrayList<>();

        // Check if the total expense exceeds 20% of the historical average
        double totalExpense = getCurrentMonthExpense();
        double historicalAverage = getHistoricalMonthlyAverageExcludingCurrentMonth();
        if (alertsEnabled && totalExpense > historicalAverage * 1.2) {
            // Use String.format to ensure two decimal places
            String message = "Current month expense (" + String.format("%.2f", totalExpense) +
                    ") exceeds historical monthly average (" + String.format("%.2f", historicalAverage) +
                    ") by over 20%!";
            alerts.add(alertService.createAlert(message, AlertLevel.CRITICAL, "Total Expense"));
        }

        // Check if the total expense exceeds the set global threshold
        if (alertsEnabled && totalExpense > globalThreshold) {
            // Use String.format to ensure two decimal places
            String message = "Total expense (" + String.format("%.2f", totalExpense) +
                    ") exceeds global threshold (" + String.format("%.2f", globalThreshold) + ")!";
            alerts.add(alertService.createAlert(message, AlertLevel.CRITICAL, "Total Expense"));
        }

        // Check category thresholds
        for (Map.Entry<String, Double> entry : categoryThresholds.entrySet()) {
            String category = entry.getKey();
            Double threshold = entry.getValue();

            // Skip categories with unset thresholds
            if (threshold == null || threshold <= 0) continue;

            double expense = getCategoryExpensesCurrentMonth().getOrDefault(category, 0.0);
            if (expense > threshold) {
                // Use String.format to ensure two decimal places
                String message = "Expense for category " + category + " (" +
                        String.format("%.2f", expense) + ") exceeds the threshold (" +
                        String.format("%.2f", threshold) + ")!";
                alerts.add(alertService.createAlert(message, AlertLevel.CRITICAL, category));
            }
        }

        return alerts;
    }

    /**
     * Get the total expense for the current month
     */
    private double getCurrentMonthExpense() {
        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();
        int currentYear = currentDate.getYear();

        List<Transaction> transactions = transactionController.getTransactions();
        return transactions.stream()
                .filter(t -> t.getDate().getMonth() == currentMonth && t.getDate().getYear() == currentYear)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /**
     * Get the historical monthly average expense, excluding the current month
     */
    private double getHistoricalMonthlyAverageExcludingCurrentMonth() {
        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();
        int currentYear = currentDate.getYear();

        List<Transaction> transactions = transactionController.getTransactions();

        // Filter out transactions from the current month
        List<Transaction> historicalTransactions = transactions.stream()
                .filter(t -> !(t.getDate().getMonth() == currentMonth && t.getDate().getYear() == currentYear))
                .collect(Collectors.toList());

        if (historicalTransactions.isEmpty()) {
            return 0;
        }

        // Calculate the total expense for each month
        Map<String, Double> monthlyExpenses = new HashMap<>();
        for (Transaction transaction : historicalTransactions) {
            LocalDate date = transaction.getDate();
            String monthKey = date.getYear() + "-" + date.getMonthValue();
            monthlyExpenses.put(monthKey, monthlyExpenses.getOrDefault(monthKey, 0.0) + transaction.getAmount());
        }

        // Calculate the monthly average expense
        if (monthlyExpenses.isEmpty()) {
            return 0;
        }
        return monthlyExpenses.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    /**
     * Get the expenses for each category in the current month
     */
    private Map<String, Double> getCategoryExpensesCurrentMonth() {
        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();
        int currentYear = currentDate.getYear();

        List<Transaction> transactions = transactionController.getTransactions();
        return transactions.stream()
                .filter(t -> t.getDate().getMonth() == currentMonth && t.getDate().getYear() == currentYear)
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)));
    }

    /**
     * Load settings
     */
    protected void loadSettings() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
            properties.load(input);

            // Load the global threshold
            String globalThresholdStr = properties.getProperty("globalThreshold");
            if (globalThresholdStr != null) {
                globalThreshold = Double.parseDouble(globalThresholdStr);
                globalThresholdField.setText(String.valueOf(globalThreshold));
            }

            // Load category thresholds
            categoryThresholds.clear();
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith("categoryThreshold.")) {
                    String category = key.substring("categoryThreshold.".length());
                    double threshold = Double.parseDouble(properties.getProperty(key));
                    categoryThresholds.put(category, threshold);
                }
            }

            // Repopulate the table data
            categoryThresholdsModel.setRowCount(0);
            for (Map.Entry<String, Double> entry : categoryThresholds.entrySet()) {
                categoryThresholdsModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
            }

        } catch (IOException | NumberFormatException e) {
            // If the file does not exist or has an incorrect format, use default settings
            e.printStackTrace();
        }
    }

    /**
     * Save category threshold settings
     */
    private void saveCategoryThresholds() {
        Properties properties = new Properties();

        // Format the global threshold
        properties.setProperty("globalThreshold", String.format("%.2f", globalThreshold));
        properties.setProperty("alertsEnabled", String.valueOf(alertsEnabled));

        // Format category thresholds
        for (Map.Entry<String, Double> entry : categoryThresholds.entrySet()) {
            String category = entry.getKey();
            Double threshold = entry.getValue();

            // Ensure to save in two decimal places format
            properties.setProperty("categoryThreshold." + category,
                    threshold != null ? String.format("%.2f", threshold) : "");
        }

        try (OutputStream output = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load category thresholds
     */
    private void loadCategoryThresholds() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
            properties.load(input);

            categoryThresholds.clear();
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith("categoryThreshold.")) {
                    String category = key.substring("categoryThreshold.".length());
                    String value = properties.getProperty(key);

                    // Handle unset thresholds
                    if ("null".equalsIgnoreCase(value) || value.trim().isEmpty()) {
                        categoryThresholds.put(category, null);
                    } else {
                        try {
                            double threshold = Double.parseDouble(value);
                            categoryThresholds.put(category, threshold);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid threshold for category " + category + ": " + value);
                            categoryThresholds.put(category, null);
                        }
                    }
                }
            }

            // Update the table display
            updateThresholdTable();

        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateThresholdTable() {
        categoryThresholdsModel.setRowCount(0);
        for (Map.Entry<String, Double> entry : categoryThresholds.entrySet()) {
            String category = entry.getKey();
            Double threshold = entry.getValue();

            // Format directly when displaying
            String displayValue = threshold != null ?
                    String.format("%.2f", threshold) : "Not Set";
            categoryThresholdsModel.addRow(new Object[]{category, displayValue});
        }
    }

    /**
     * Create a stylized button
     * @param text Button text
     * @return A stylized button
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(SECONDARY_COLOR);
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        return button;
    }

    /**
     * Show detailed information
     */
    private void showDetails() {
        Map<String, Double> categoryExpenses = getCategoryExpensesCurrentMonth();
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Category", "Threshold", "Actual Value", "Exceeded Amount"}, 0);

        for (Map.Entry<String, Double> entry : categoryThresholds.entrySet()) {
            String category = entry.getKey();
            Double threshold = entry.getValue();
            double actualValue = categoryExpenses.getOrDefault(category, 0.0);

            // Format all display values directly
            String thresholdDisplay = threshold != null ?
                    String.format("%.2f", threshold) : "Not Set";
            String actualDisplay = String.format("%.2f", actualValue);

            double exceededAmount = threshold != null ? Math.max(0, actualValue - threshold) : 0;
            String exceededDisplay = exceededAmount > 0 ?
                    String.format("%.2f", exceededAmount) : "0.00";

            model.addRow(new Object[]{category, thresholdDisplay, actualDisplay, exceededDisplay});
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Category Details", false);
        dialog.add(scrollPane);

        // Set the dialog width to the optimal width of the content and fix the height
        dialog.setSize(scrollPane.getPreferredSize().width + 50, 400);
        dialog.setResizable(false); // Disable width adjustment

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    // Add a public method in the ExpenseAlertPanel class
    public double getCurrentMonthExpenseForTest() {
        return getCurrentMonthExpense();
    }
    // Add in the ExpenseAlertPanel class
    public ExpenseAlertPanel(TransactionController controller, boolean loadSettings) {
        this.transactionController = controller;
        if (loadSettings) {
            initUI(); // Initialize the UI, which will call loadSettings()
        }
    }

    /**
     * Sets the expense threshold for a specific category.
     * Displays a warning dialog if input is invalid.
     *
     * @param category The category name (e.g., "Dining", "Shopping").
     * @param threshold The maximum allowed expense for the category.
     *                  Must be non-negative.
     * @throws IllegalArgumentException if category is null or empty,
     *         or threshold is negative (for non-UI callers).
     */
    public void setCategoryThreshold(String category, double threshold) {
        // Validate inputs and show warnings
        if (category == null || category.isEmpty()) {
            showErrorDialog("Category name cannot be empty");
            throw new IllegalArgumentException("Category cannot be null or empty");
        }

        if (threshold < 0) {
            showErrorDialog("Threshold cannot be negative");
            throw new IllegalArgumentException("Threshold cannot be negative");
        }

        // Store the valid threshold
        categoryThresholds.put(category, threshold);
    }

    /**
     * Helper method to display error dialogs.
     */
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                "Input Error",
                JOptionPane.ERROR_MESSAGE
        );
    }


    public void updateExpenseData() {
        // Recalculate the expenses for all categories and check the thresholds
        Map<String, Double> categoryExpenses = calculateCategoryExpenses();
        for (String category : categoryExpenses.keySet()) {
            double expense = categoryExpenses.get(category);
            double threshold = categoryThresholds.getOrDefault(category, Double.MAX_VALUE);
            categoryOverThreshold.put(category, expense > threshold);
        }
    }

    // Add in the ExpenseAlertPanel class
    private Map<String, Boolean> categoryOverThreshold = new HashMap<>();

    // Add a public access method
    public boolean isCategoryOverThreshold(String category) {
        return categoryOverThreshold.getOrDefault(category, false);
    }

    // Method to update the threshold status
    private void checkCategoryThresholds() {
        Map<String, Double> categoryExpenses = calculateCategoryExpenses();
        for (String category : categoryExpenses.keySet()) {
            double expense = categoryExpenses.get(category);
            double threshold = categoryThresholds.getOrDefault(category, Double.MAX_VALUE);
            categoryOverThreshold.put(category, expense > threshold);
        }
    }
    // Add in the ExpenseAlertPanel class
    private Map<String, Double> calculateCategoryExpenses() {
        Map<String, Double> categoryExpenses = new HashMap<>();
        LocalDate now = LocalDate.now();

        for (Transaction transaction : transactionController.getTransactions()) {
            LocalDate date = transaction.getDate();
            // Filter transactions for the current month
            if (date.getYear() == now.getYear() && date.getMonth() == now.getMonth()) {
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
            }
        }
        return categoryExpenses;
    }
    // Add in the ExpenseAlertPanel class
    public boolean hasCategoryThreshold(String category) {
        return categoryThresholds.containsKey(category);
    }

    public double getCategoryThreshold(String category) {
        return categoryThresholds.getOrDefault(category, 0.0);
    }

    public void removeCategoryThreshold(String category) {
        categoryThresholds.remove(category);
    }
}