package com.financeapp.view;

import com.financeapp.controller.TransactionController;
import com.financeapp.model.AlertService;
import com.financeapp.model.AlertService.Alert;
import com.financeapp.model.AlertService.AlertLevel;
import com.financeapp.model.Transaction;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Expense Alert Panel
 * Displays alert information when individual category or total expenses exceed set thresholds
 */
public class ExpenseAlertPanel extends JPanel {
    
    private final TransactionController transactionController;
    private final AlertService alertService;
    
    // UI Components
    private JPanel alertsPanel;
    private JPanel settingsPanel;
    private JPanel thresholdsPanel;
    private JPanel alertHistoryPanel;
    private JPanel notificationPanel;
    private JTabbedPane alertTabPane;
    
    // Alerts Components
    private JPanel activeAlertsPanel;
    private JScrollPane activeAlertsScrollPane;
    
    // History Components
    private JTable alertHistoryTable;
    private DefaultTableModel alertHistoryModel;
    private JComboBox<String> filterComboBox;
    private JCheckBox showAcknowledgedCheckbox;
    
    // Notification Components 
    private JCheckBox enableNotificationsCheckbox;
    private JCheckBox notifyOnlyHighPriorityCheckbox;
    private JList<String> mutedCategoriesList;
    private DefaultListModel<String> mutedCategoriesModel;
    
    // Settings Components
    private JCheckBox enableAlertsCheckbox;
    private JTextField globalThresholdField;
    private JTable categoryThresholdsTable;
    private DefaultTableModel categoryThresholdsModel;
    private JButton saveSettingsButton;
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
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create title panel
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // Create tabbed panel for different alert sections
        alertTabPane = new JTabbedPane();
        alertTabPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        alertTabPane.setBackground(BACKGROUND_COLOR);
        alertTabPane.setForeground(TEXT_COLOR);
        alertTabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        add(alertTabPane, BorderLayout.CENTER);
        
        // Create active alerts panel
        activeAlertsPanel = createActiveAlertsPanel();
        alertTabPane.addTab("Active Alerts", activeAlertsPanel);
        
        // Create alert history panel
        alertHistoryPanel = createAlertHistoryPanel();
        alertTabPane.addTab("Alert History", alertHistoryPanel);
        
        // Create notification settings panel
        notificationPanel = createNotificationPanel();
        alertTabPane.addTab("Notification Settings", notificationPanel);
        
        // Create settings panel
        settingsPanel = createSettingsPanel();
        alertTabPane.addTab("Alert Rules", settingsPanel);
        
        // Initial data update
        updateExpenseAlerts();
    }
    
    /**
     * Create title panel
     */
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Expense Alerts Center");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.WEST);
        
        JLabel subtitleLabel = new JLabel("Monitor expenses exceeding budget");
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subtitleLabel.setForeground(SECONDARY_COLOR);
        
        JPanel subPanel = new JPanel(new BorderLayout());
        subPanel.setOpaque(false);
        subPanel.add(subtitleLabel, BorderLayout.WEST);
        
        panel.add(subPanel, BorderLayout.SOUTH);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh Alerts");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> updateExpenseAlerts());
        panel.add(refreshButton, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Create active alerts panel
     */
    private JPanel createActiveAlertsPanel() {
        JPanel panel = createCardPanel("Current Expense Alerts");
        panel.setLayout(new BorderLayout(0, 10));
        
        // Create alerts content panel
        JPanel alertsContent = new JPanel();
        alertsContent.setLayout(new BoxLayout(alertsContent, BoxLayout.Y_AXIS));
        alertsContent.setOpaque(false);
        
        // Wrap in scroll pane
        activeAlertsScrollPane = new JScrollPane(alertsContent);
        activeAlertsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(activeAlertsScrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton refreshButton = new JButton("Refresh Alerts");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> updateExpenseAlerts());
        
        JButton acknowledgeAllButton = new JButton("Acknowledge All");
        acknowledgeAllButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        acknowledgeAllButton.setFocusPainted(false);
        acknowledgeAllButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        acknowledgeAllButton.addActionListener(e -> acknowledgeAllAlerts());
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(acknowledgeAllButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create alert history panel
     */
    private JPanel createAlertHistoryPanel() {
        JPanel panel = createCardPanel("Alert History");
        panel.setLayout(new BorderLayout(0, 10));
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);
        
        JLabel filterLabel = new JLabel("Filter by: ");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        String[] filterOptions = {"All Alerts", "Today", "This Week", "This Month", "Critical Only", "Warning Only"};
        filterComboBox = new JComboBox<>(filterOptions);
        filterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterComboBox.addActionListener(e -> refreshAlertHistory());
        
        showAcknowledgedCheckbox = new JCheckBox("Show Acknowledged", false);
        showAcknowledgedCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showAcknowledgedCheckbox.setOpaque(false);
        showAcknowledgedCheckbox.addActionListener(e -> refreshAlertHistory());
        
        filterPanel.add(filterLabel);
        filterPanel.add(filterComboBox);
        filterPanel.add(showAcknowledgedCheckbox);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        
        // Create table model
        String[] columns = {"Date/Time", "Level", "Category", "Alert Message", "Status"};
        alertHistoryModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };
        
        alertHistoryTable = new JTable(alertHistoryModel);
        alertHistoryTable.setRowHeight(25);
        alertHistoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        alertHistoryTable.getTableHeader().setBackground(SECONDARY_COLOR);
        alertHistoryTable.getTableHeader().setForeground(Color.WHITE);
        alertHistoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set column widths
        alertHistoryTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Date/Time
        alertHistoryTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Level
        alertHistoryTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Category
        alertHistoryTable.getColumnModel().getColumn(3).setPreferredWidth(350); // Message
        alertHistoryTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Status
        
        // Customize renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        alertHistoryTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        alertHistoryTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        
        JScrollPane scrollPane = new JScrollPane(alertHistoryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton acknowledgeButton = new JButton("Acknowledge Selected");
        acknowledgeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        acknowledgeButton.setFocusPainted(false);
        acknowledgeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        acknowledgeButton.addActionListener(e -> acknowledgeSelectedAlert());
        
        JButton clearOldButton = new JButton("Clear Old Alerts");
        clearOldButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearOldButton.setFocusPainted(false);
        clearOldButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearOldButton.addActionListener(e -> clearOldAlerts());
        
        buttonPanel.add(acknowledgeButton);
        buttonPanel.add(clearOldButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create notification settings panel
     */
    private JPanel createNotificationPanel() {
        JPanel panel = createCardPanel("Notification Settings");
        panel.setLayout(new BorderLayout(10, 10));
        
        // Settings panel
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setOpaque(false);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Enable notifications
        enableNotificationsCheckbox = new JCheckBox("Enable Alert Notifications", true);
        enableNotificationsCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        enableNotificationsCheckbox.setOpaque(false);
        enableNotificationsCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsPanel.add(enableNotificationsCheckbox);
        settingsPanel.add(Box.createVerticalStrut(10));
        
        // Only high priority
        notifyOnlyHighPriorityCheckbox = new JCheckBox("Only Show High Priority Alerts", false);
        notifyOnlyHighPriorityCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        notifyOnlyHighPriorityCheckbox.setOpaque(false);
        notifyOnlyHighPriorityCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsPanel.add(notifyOnlyHighPriorityCheckbox);
        settingsPanel.add(Box.createVerticalStrut(20));
        
        // Muted categories
        JLabel mutedLabel = new JLabel("Muted Categories:");
        mutedLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mutedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsPanel.add(mutedLabel);
        settingsPanel.add(Box.createVerticalStrut(10));
        
        mutedCategoriesModel = new DefaultListModel<>();
        mutedCategoriesList = new JList<>(mutedCategoriesModel);
        mutedCategoriesList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mutedCategoriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane listScrollPane = new JScrollPane(mutedCategoriesList);
        listScrollPane.setPreferredSize(new Dimension(300, 150));
        listScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        listScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsPanel.add(listScrollPane);
        settingsPanel.add(Box.createVerticalStrut(10));
        
        // Buttons for muted categories
        JPanel categoryButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        categoryButtonPanel.setOpaque(false);
        categoryButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton muteButton = new JButton("Mute Category");
        muteButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        muteButton.setFocusPainted(false);
        muteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        muteButton.addActionListener(e -> muteCategory());
        
        JButton unmuteButton = new JButton("Unmute Selected");
        unmuteButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        unmuteButton.setFocusPainted(false);
        unmuteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        unmuteButton.addActionListener(e -> unmuteSelectedCategory());
        
        categoryButtonPanel.add(muteButton);
        categoryButtonPanel.add(unmuteButton);
        settingsPanel.add(categoryButtonPanel);
        
        panel.add(new JScrollPane(settingsPanel), BorderLayout.CENTER);
        
        // Save button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton saveButton = new JButton("Save Notification Settings");
        saveButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        saveButton.setBackground(Color.WHITE);
        saveButton.setForeground(Color.BLACK);
        saveButton.setFocusPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> saveNotificationSettings());
        
        JButton resetButton = new JButton("Reset to Default");
        resetButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resetButton.setFocusPainted(false);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetButton.addActionListener(e -> resetNotificationSettings());
        
        buttonPanel.add(resetButton);
        buttonPanel.add(saveButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create settings panel
     */
    private JPanel createSettingsPanel() {
        JPanel panel = createCardPanel("Alert Settings");
        panel.setLayout(new BorderLayout(10, 10));
        
        // Main settings panel
        JPanel mainSettingsPanel = new JPanel(new BorderLayout(10, 10));
        mainSettingsPanel.setOpaque(false);
        
        // Global settings panel
        JPanel globalSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        globalSettingsPanel.setOpaque(false);
        
        enableAlertsCheckbox = new JCheckBox("Enable Expense Alerts", true);
        enableAlertsCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        enableAlertsCheckbox.setOpaque(false);
        
        JLabel globalThresholdLabel = new JLabel("Global Expense Alert Threshold: ¥");
        globalThresholdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        globalThresholdField = new JTextField(10);
        globalThresholdField.setText(String.valueOf(globalThreshold));
        
        globalSettingsPanel.add(enableAlertsCheckbox);
        globalSettingsPanel.add(globalThresholdLabel);
        globalSettingsPanel.add(globalThresholdField);
        
        mainSettingsPanel.add(globalSettingsPanel, BorderLayout.NORTH);
        
        // Category threshold settings panel
        thresholdsPanel = createThresholdsPanel();
        mainSettingsPanel.add(thresholdsPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        saveSettingsButton = new JButton("Save Settings");
        saveSettingsButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        saveSettingsButton.setBackground(Color.WHITE);
        saveSettingsButton.setForeground(Color.BLACK);
        saveSettingsButton.setFocusPainted(false);
        saveSettingsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveSettingsButton.addActionListener(e -> saveSettings());
        
        buttonPanel.add(saveSettingsButton);
        
        mainSettingsPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(mainSettingsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create category thresholds panel
     */
    private JPanel createThresholdsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR), 
                "Category Threshold Settings", 
                TitledBorder.LEFT, 
                TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 12), 
                PRIMARY_COLOR));
        
        // Table model
        String[] columns = {"Category", "Threshold (¥)"};
        categoryThresholdsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only allow editing the threshold column
            }
        };
        
        categoryThresholdsTable = new JTable(categoryThresholdsModel);
        categoryThresholdsTable.setRowHeight(25);
        categoryThresholdsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        categoryThresholdsTable.getTableHeader().setBackground(SECONDARY_COLOR);
        categoryThresholdsTable.getTableHeader().setForeground(Color.WHITE);
        
        // Set renderer to right-align numbers
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        categoryThresholdsTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        
        JScrollPane scrollPane = new JScrollPane(categoryThresholdsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add and remove category buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        
        addCategoryButton = new JButton("Add Category");
        addCategoryButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addCategoryButton.setFocusPainted(false);
        addCategoryButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addCategoryButton.addActionListener(e -> addCategory());
        
        removeCategoryButton = new JButton("Remove Category");
        removeCategoryButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        removeCategoryButton.setFocusPainted(false);
        removeCategoryButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeCategoryButton.addActionListener(e -> removeCategory());
        
        buttonPanel.add(addCategoryButton);
        buttonPanel.add(removeCategoryButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create card style panel
     */
    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)
                )
        ));
        
        if (title != null && !title.isEmpty()) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLabel.setForeground(PRIMARY_COLOR);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            panel.add(titleLabel, BorderLayout.NORTH);
        }
        
        return panel;
    }
    
    /**
     * Card shadow border
     */
    private class ShadowBorder extends AbstractBorder {
        private static final int SHADOW_SIZE = 5;
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw shadow
            g2d.setColor(new Color(0, 0, 0, 50));
            for (int i = 0; i < SHADOW_SIZE; i++) {
                g2d.fillRoundRect(x + i, y + i, width - i * 2, height - i * 2, 10, 10);
            }
            
            g2d.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = SHADOW_SIZE;
            return insets;
        }
    }
    
    /**
     * Update alerts
     */
    private void updateExpenseAlerts() {
        try {
            // Get all transactions
            transactionController.loadTransactions();
            List<Transaction> transactions = transactionController.getTransactions();
            
            // Get current month expenses
            Map<String, Double> currentExpenses = transactionController.getCurrentMonthExpenses();
            
            // Get historical average
            Map<String, Double> historicalAverage = transactionController.getHistoricalAverage();
            
            // Clear existing content in active alerts panel
            Component firstComponent = activeAlertsPanel.getComponent(0);
            if (!(firstComponent instanceof JScrollPane)) {
                System.err.println("Expected JScrollPane but got: " + firstComponent.getClass().getName());
                return;
            }
            
            JScrollPane scrollPane = (JScrollPane) firstComponent;
            Component view = scrollPane.getViewport().getView();
            if (!(view instanceof JPanel)) {
                System.err.println("Expected JPanel but got: " + view.getClass().getName());
                return;
            }
            
            JPanel alertsContent = (JPanel) view;
            alertsContent.removeAll();
            
            // Get threshold alerts
            List<String> alerts = new ArrayList<>();
            
            // Check if total expenses exceed global threshold
            double totalCurrent = currentExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
            
            if (alertsEnabled && totalCurrent > globalThreshold) {
                String alertMessage = String.format("Total expenses (¥%.2f) have exceeded your global threshold (¥%.2f)!", 
                        totalCurrent, globalThreshold);
                alerts.add(alertMessage);
                
                // Create alert in alert service
                alertService.createAlert(
                    alertMessage,
                    AlertLevel.WARNING,
                    "Global"
                );
            }
            
            // Check each category against its threshold
            for (Map.Entry<String, Double> entry : currentExpenses.entrySet()) {
                String category = entry.getKey();
                double amount = entry.getValue();
                
                if (categoryThresholds.containsKey(category) && alertsEnabled) {
                    double threshold = categoryThresholds.get(category);
                    if (amount > threshold) {
                        String alertMessage = String.format("'%s' expenses (¥%.2f) have exceeded your threshold (¥%.2f)", 
                                category, amount, threshold);
                        alerts.add(alertMessage);
                        
                        // Create alert in alert service
                        alertService.createAlert(
                            alertMessage,
                            AlertLevel.WARNING,
                            category
                        );
                    }
                }
            }
            
            // Check historical anomalies
            if (alertsEnabled) {
                // Check if total expenses exceed historical average
                double totalHistorical = historicalAverage.values().stream().mapToDouble(Double::doubleValue).sum();
                
                if (totalCurrent > totalHistorical * 1.3) { // 30% higher threshold for critical alerts
                    String alertMessage = String.format("CRITICAL: Total expenses (¥%.2f) are more than 30%% higher than historical average (¥%.2f)!", 
                            totalCurrent, totalHistorical);
                    alerts.add(alertMessage);
                    
                    // Create alert in alert service
                    alertService.createAlert(
                        alertMessage,
                        AlertLevel.CRITICAL,
                        "Global"
                    );
                } else if (totalCurrent > totalHistorical * 1.2) { // 20% higher for warnings
                    String alertMessage = String.format("WARNING: Total expenses (¥%.2f) are more than 20%% higher than historical average (¥%.2f)", 
                            totalCurrent, totalHistorical);
                    alerts.add(alertMessage);
                    
                    // Create alert in alert service
                    alertService.createAlert(
                        alertMessage,
                        AlertLevel.WARNING,
                        "Global"
                    );
                }
                
                // Check each category
                for (Map.Entry<String, Double> entry : currentExpenses.entrySet()) {
                    String category = entry.getKey();
                    double amount = entry.getValue();
                    Double historicalAmount = historicalAverage.get(category);
                    
                    if (historicalAmount != null) {
                        if (amount > historicalAmount * 1.5) { // 50% higher for critical
                            String alertMessage = String.format("CRITICAL: '%s' expenses (¥%.2f) are more than 50%% higher than historical average (¥%.2f)", 
                                    category, amount, historicalAmount);
                            alerts.add(alertMessage);
                            
                            // Create alert in alert service
                            alertService.createAlert(
                                alertMessage,
                                AlertLevel.CRITICAL,
                                category
                            );
                        } else if (amount > historicalAmount * 1.2) { // 20% higher for warnings
                            String alertMessage = String.format("WARNING: '%s' expenses (¥%.2f) are more than 20%% higher than historical average (¥%.2f)", 
                                    category, amount, historicalAmount);
                            alerts.add(alertMessage);
                            
                            // Create alert in alert service
                            alertService.createAlert(
                                alertMessage,
                                AlertLevel.WARNING,
                                category
                            );
                        }
                    }
                }
            }
            
            // Check for trend-based alerts - using SimpleBudgetCalculator directly
            if (alertsEnabled && transactionController.getBudgetCalculator() instanceof com.financeapp.model.SimpleBudgetCalculator) {
                com.financeapp.model.SimpleBudgetCalculator calculator = 
                    (com.financeapp.model.SimpleBudgetCalculator) transactionController.getBudgetCalculator();
                
                List<String> trendAlerts = calculator.detectTrendAlerts(transactions, 6); // Last 6 months
                
                for (String alert : trendAlerts) {
                    alerts.add(alert);
                    
                    // Create alert in alert service - determine level based on content
                    AlertLevel level = alert.contains("CRITICAL") ? 
                        AlertLevel.CRITICAL : 
                        (alert.contains("WARNING") ? AlertLevel.WARNING : AlertLevel.INFO);
                    
                    String category = "Trends";
                    if (alert.contains("'")) {
                        // Extract category from message like "Category 'Food' expenses..."
                        int start = alert.indexOf("'") + 1;
                        int end = alert.indexOf("'", start);
                        if (start > 0 && end > start) {
                            category = alert.substring(start, end);
                        }
                    }
                    
                    alertService.createAlert(alert, level, category);
                }
            }
            
            // Get unacknowledged alerts from the alert service
            List<Alert> serviceAlerts = alertService.getUnacknowledgedAlerts();
            
            // Display alerts in UI
            if (serviceAlerts.isEmpty()) {
                JLabel noAlertsLabel = new JLabel("No expense alerts at the moment", JLabel.CENTER);
                noAlertsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                noAlertsLabel.setForeground(SUCCESS_COLOR);
                alertsContent.add(noAlertsLabel);
            } else {
                // Sort by level (critical first) then timestamp (newest first)
                serviceAlerts.sort((a1, a2) -> {
                    if (a1.getLevel() != a2.getLevel()) {
                        return a1.getLevel().ordinal() - a2.getLevel().ordinal();
                    }
                    return a2.getTimestamp().compareTo(a1.getTimestamp());
                });
                
                // Show up to 10 alerts
                int count = Math.min(serviceAlerts.size(), 10);
                for (int i = 0; i < count; i++) {
                    Alert alert = serviceAlerts.get(i);
                    JPanel alertPanel = createAlertItemFromAlert(alert);
                    alertsContent.add(alertPanel);
                    if (i < count - 1) {
                        alertsContent.add(Box.createVerticalStrut(5));
                    }
                }
                
                // Show count if there are more alerts
                if (serviceAlerts.size() > 10) {
                    JLabel moreLabel = new JLabel("There are " + (serviceAlerts.size() - 10) + " more alerts...", JLabel.CENTER);
                    moreLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    moreLabel.setForeground(ERROR_COLOR);
                    alertsContent.add(Box.createVerticalStrut(5));
                    alertsContent.add(moreLabel);
                    
                    // Add "View All" button
                    JButton viewAllButton = new JButton("View All in History");
                    viewAllButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    viewAllButton.addActionListener(e -> {
                        alertTabPane.setSelectedIndex(1); // Switch to history tab
                        refreshAlertHistory();
                    });
                    
                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    buttonPanel.setOpaque(false);
                    buttonPanel.add(viewAllButton);
                    
                    alertsContent.add(Box.createVerticalStrut(5));
                    alertsContent.add(buttonPanel);
                }
            }
            
            // Update the history table as well
            refreshAlertHistory();
            
            // Refresh UI
            alertsContent.revalidate();
            alertsContent.repaint();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error updating alerts: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Create alert panel from Alert object
     */
    private JPanel createAlertItemFromAlert(Alert alert) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        
        // Set background color based on alert level
        Color bgColor;
        Icon icon;
        
        switch (alert.getLevel()) {
            case CRITICAL:
                bgColor = new Color(255, 200, 200);
                icon = UIManager.getIcon("OptionPane.errorIcon");
                break;
            case WARNING:
                bgColor = new Color(255, 235, 210);
                icon = UIManager.getIcon("OptionPane.warningIcon");
                break;
            default: // INFO
                bgColor = new Color(230, 240, 255);
                icon = UIManager.getIcon("OptionPane.informationIcon");
                break;
        }
        
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(panel.getBackground().darker(), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Warning icon
        JLabel iconLabel = new JLabel(icon);
        panel.add(iconLabel, BorderLayout.WEST);
        
        // Create a panel for message and category
        JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
        messagePanel.setOpaque(false);
        
        // Alert text
        JLabel textLabel = new JLabel(alert.getMessage());
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        textLabel.setForeground(alert.getLevel() == AlertLevel.CRITICAL ? ERROR_COLOR : TEXT_COLOR);
        messagePanel.add(textLabel, BorderLayout.CENTER);
        
        // Category label below the message
        JLabel categoryLabel = new JLabel("Category: " + alert.getCategory());
        categoryLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        categoryLabel.setForeground(Color.DARK_GRAY);
        messagePanel.add(categoryLabel, BorderLayout.SOUTH);
        
        panel.add(messagePanel, BorderLayout.CENTER);
        
        // Panel for date and acknowledge button
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setOpaque(false);
        
        // Date/time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        JLabel dateLabel = new JLabel(alert.getTimestamp().format(formatter));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateLabel.setForeground(TEXT_COLOR);
        rightPanel.add(dateLabel, BorderLayout.NORTH);
        
        // Acknowledge button
        JButton ackButton = new JButton("Acknowledge");
        ackButton.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        ackButton.setMargin(new Insets(2, 4, 2, 4));
        ackButton.setFocusPainted(false);
        ackButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ackButton.addActionListener(e -> {
            if (alertService.acknowledgeAlert(alert.getId())) {
                updateExpenseAlerts();
                refreshAlertHistory();
            }
        });
        rightPanel.add(ackButton, BorderLayout.SOUTH);
        
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Add category
     */
    private void addCategory() {
        // Get all transaction categories
        List<String> allCategories = new ArrayList<>();
        try {
            transactionController.loadTransactions();
            List<Transaction> transactions = transactionController.getTransactions();
            allCategories = transactions.stream()
                    .map(Transaction::getCategory)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Failed to load categories: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Filter out already added categories
        for (int i = 0; i < categoryThresholdsModel.getRowCount(); i++) {
            String existingCategory = (String) categoryThresholdsModel.getValueAt(i, 0);
            allCategories.remove(existingCategory);
        }
        
        if (allCategories.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No more categories to add.", 
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Show selection dialog
        String selectedCategory = (String) JOptionPane.showInputDialog(this, 
                "Select category to add:", 
                "Add Category", 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                allCategories.toArray(), 
                allCategories.get(0));
        
        if (selectedCategory != null) {
            // Add to table
            Object[] row = {selectedCategory, 1000.0}; // Default threshold 1000
            categoryThresholdsModel.addRow(row);
        }
    }
    
    /**
     * Remove category
     */
    private void removeCategory() {
        int selectedRow = categoryThresholdsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a category to remove.", 
                    "Tip", 
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        categoryThresholdsModel.removeRow(selectedRow);
    }
    
    /**
     * Save settings
     */
    private void saveSettings() {
        try {
            // Update global settings
            alertsEnabled = enableAlertsCheckbox.isSelected();
            globalThreshold = Double.parseDouble(globalThresholdField.getText().trim());
            
            // Update category thresholds
            categoryThresholds.clear();
            for (int i = 0; i < categoryThresholdsModel.getRowCount(); i++) {
                String category = (String) categoryThresholdsModel.getValueAt(i, 0);
                Double threshold = Double.parseDouble(categoryThresholdsModel.getValueAt(i, 1).toString());
                categoryThresholds.put(category, threshold);
            }
            
            // Save to user configuration (simplified here, actual projects should have configuration management classes)
            
            JOptionPane.showMessageDialog(this, 
                    "Settings saved.", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh alerts
            updateExpenseAlerts();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter valid numbers.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Load settings
     */
    private void loadSettings() {
        // This should load settings from configuration file or database
        // Using default values for simplification
        
        enableAlertsCheckbox.setSelected(alertsEnabled);
        globalThresholdField.setText(String.valueOf(globalThreshold));
        
        // Load default category thresholds
        categoryThresholdsModel.setRowCount(0);
        
        try {
            transactionController.loadTransactions();
            Map<String, Double> avgExpenses = transactionController.getHistoricalAverage();
            
            // Use historical average as default threshold, increased by 20%
            for (Map.Entry<String, Double> entry : avgExpenses.entrySet()) {
                String category = entry.getKey();
                double avgAmount = entry.getValue();
                double threshold = avgAmount * 1.2; // 20% higher than historical average
                
                if (threshold > 0) {
                    categoryThresholds.put(category, threshold);
                    Object[] row = {category, threshold};
                    categoryThresholdsModel.addRow(row);
                }
            }
        } catch (Exception e) {
            // If loading fails, add some default categories
            String[] defaultCategories = {"Food", "Transportation", "Housing", "Entertainment", "Shopping"};
            double[] defaultThresholds = {2000.0, 1000.0, 3000.0, 1000.0, 1500.0};
            
            for (int i = 0; i < defaultCategories.length; i++) {
                categoryThresholds.put(defaultCategories[i], defaultThresholds[i]);
                Object[] row = {defaultCategories[i], defaultThresholds[i]};
                categoryThresholdsModel.addRow(row);
            }
        }
    }
    
    /**
     * Show expense alerts tab
     */
    private void showExpenseAlertsTab() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof JTabbedPane)) {
            parent = parent.getParent();
        }
        
        if (parent instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) parent;
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                if (tabbedPane.getComponentAt(i) instanceof ExpenseAlertPanel) {
                    tabbedPane.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    
    /**
     * Acknowledge all active alerts
     */
    private void acknowledgeAllAlerts() {
        List<Alert> unacknowledgedAlerts = alertService.getUnacknowledgedAlerts();
        int count = 0;
        
        for (Alert alert : unacknowledgedAlerts) {
            if (alertService.acknowledgeAlert(alert.getId())) {
                count++;
            }
        }
        
        if (count > 0) {
            JOptionPane.showMessageDialog(this,
                    String.format("%d alerts have been acknowledged.", count),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            updateExpenseAlerts();
            refreshAlertHistory();
        } else {
            JOptionPane.showMessageDialog(this,
                    "No unacknowledged alerts found.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Refresh alert history table
     */
    private void refreshAlertHistory() {
        // Clear existing data
        alertHistoryModel.setRowCount(0);
        
        // Get selected filter
        String filterOption = (String) filterComboBox.getSelectedItem();
        boolean showAcknowledged = showAcknowledgedCheckbox.isSelected();
        
        // Get alerts based on filter
        List<Alert> alerts;
        LocalDate today = LocalDate.now();
        
        if ("Today".equals(filterOption)) {
            alerts = alertService.getAlertsByDate(today);
        } else if ("This Week".equals(filterOption)) {
            alerts = alertService.getAllAlerts();
            alerts = alerts.stream()
                    .filter(a -> a.getTimestamp().toLocalDate().isAfter(today.minusDays(7)))
                    .collect(Collectors.toList());
        } else if ("This Month".equals(filterOption)) {
            alerts = alertService.getAllAlerts();
            alerts = alerts.stream()
                    .filter(a -> a.getTimestamp().toLocalDate().isAfter(today.minusDays(30)))
                    .collect(Collectors.toList());
        } else if ("Critical Only".equals(filterOption)) {
            alerts = alertService.getAlertsByLevel(AlertLevel.CRITICAL);
        } else if ("Warning Only".equals(filterOption)) {
            alerts = alertService.getAlertsByLevel(AlertLevel.WARNING);
        } else {
            // All alerts
            alerts = alertService.getAllAlerts();
        }
        
        // Filter by acknowledged status if needed
        if (!showAcknowledged) {
            alerts = alerts.stream()
                    .filter(a -> !a.isAcknowledged())
                    .collect(Collectors.toList());
        }
        
        // Sort by date/time (newest first)
        alerts.sort((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()));
        
        // Add to table
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Alert alert : alerts) {
            Object[] row = {
                alert.getTimestamp().format(formatter),
                alert.getLevel().toString(),
                alert.getCategory(),
                alert.getMessage(),
                alert.isAcknowledged() ? "Acknowledged" : "Active"
            };
            alertHistoryModel.addRow(row);
        }
    }
    
    /**
     * Acknowledge selected alert
     */
    private void acknowledgeSelectedAlert() {
        int selectedRow = alertHistoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an alert to acknowledge.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Get all alerts
        List<Alert> alerts = alertService.getAllAlerts();
        
        // Sort by date/time (newest first) to match table display order
        alerts.sort((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()));
        
        // Apply same filter as currently displayed
        String filterOption = (String) filterComboBox.getSelectedItem();
        boolean showAcknowledged = showAcknowledgedCheckbox.isSelected();
        
        LocalDate today = LocalDate.now();
        if ("Today".equals(filterOption)) {
            alerts = alerts.stream()
                    .filter(a -> a.getTimestamp().toLocalDate().equals(today))
                    .collect(Collectors.toList());
        } else if ("This Week".equals(filterOption)) {
            alerts = alerts.stream()
                    .filter(a -> a.getTimestamp().toLocalDate().isAfter(today.minusDays(7)))
                    .collect(Collectors.toList());
        } else if ("This Month".equals(filterOption)) {
            alerts = alerts.stream()
                    .filter(a -> a.getTimestamp().toLocalDate().isAfter(today.minusDays(30)))
                    .collect(Collectors.toList());
        } else if ("Critical Only".equals(filterOption)) {
            alerts = alerts.stream()
                    .filter(a -> a.getLevel() == AlertLevel.CRITICAL)
                    .collect(Collectors.toList());
        } else if ("Warning Only".equals(filterOption)) {
            alerts = alerts.stream()
                    .filter(a -> a.getLevel() == AlertLevel.WARNING)
                    .collect(Collectors.toList());
        }
        
        // Filter by acknowledged status if needed
        if (!showAcknowledged) {
            alerts = alerts.stream()
                    .filter(a -> !a.isAcknowledged())
                    .collect(Collectors.toList());
        }
        
        // Get the alert at the selected index
        if (selectedRow >= alerts.size()) {
            JOptionPane.showMessageDialog(this,
                    "Alert not found. Please refresh the list.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Alert selectedAlert = alerts.get(selectedRow);
        
        // Acknowledge the alert
        if (alertService.acknowledgeAlert(selectedAlert.getId())) {
            JOptionPane.showMessageDialog(this,
                    "Alert has been acknowledged.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            refreshAlertHistory();
            updateExpenseAlerts();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to acknowledge alert.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Clear old alerts
     */
    private void clearOldAlerts() {
        String[] options = {"7 Days", "30 Days", "90 Days", "All"};
        String selection = (String) JOptionPane.showInputDialog(this,
                "Clear alerts older than:",
                "Clear Old Alerts",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        
        if (selection == null) {
            return; // User cancelled
        }
        
        int days;
        if ("7 Days".equals(selection)) {
            days = 7;
        } else if ("30 Days".equals(selection)) {
            days = 30;
        } else if ("90 Days".equals(selection)) {
            days = 90;
        } else {
            // Clear all
            alertService.clearAllAlerts();
            JOptionPane.showMessageDialog(this,
                    "All alerts have been cleared.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            refreshAlertHistory();
            return;
        }
        
        // Clear alerts older than specified days
        int count = alertService.deleteOldAlerts(days);
        
        JOptionPane.showMessageDialog(this,
                String.format("%d alerts have been deleted.", count),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        refreshAlertHistory();
    }
    
    /**
     * Mute a category
     */
    private void muteCategory() {
        // Get all transaction categories
        List<String> allCategories = new ArrayList<>();
        try {
            transactionController.loadTransactions();
            List<Transaction> transactions = transactionController.getTransactions();
            allCategories = transactions.stream()
                    .map(Transaction::getCategory)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load categories: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Filter out already muted categories
        for (int i = 0; i < mutedCategoriesModel.size(); i++) {
            String mutedCategory = mutedCategoriesModel.getElementAt(i);
            allCategories.remove(mutedCategory);
        }
        
        if (allCategories.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No more categories to mute.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Show selection dialog
        String selectedCategory = (String) JOptionPane.showInputDialog(this,
                "Select category to mute:",
                "Mute Category",
                JOptionPane.QUESTION_MESSAGE,
                null,
                allCategories.toArray(),
                allCategories.get(0));
        
        if (selectedCategory != null) {
            alertService.muteCategory(selectedCategory);
            mutedCategoriesModel.addElement(selectedCategory);
            JOptionPane.showMessageDialog(this,
                    "Category has been muted.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Unmute selected category
     */
    private void unmuteSelectedCategory() {
        String selectedCategory = mutedCategoriesList.getSelectedValue();
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a category to unmute.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        alertService.unmuteCategory(selectedCategory);
        mutedCategoriesModel.removeElement(selectedCategory);
        JOptionPane.showMessageDialog(this,
                "Category has been unmuted.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Save notification settings
     */
    private void saveNotificationSettings() {
        boolean enableNotifications = enableNotificationsCheckbox.isSelected();
        boolean notifyOnlyHighPriority = notifyOnlyHighPriorityCheckbox.isSelected();
        
        // Update alert service settings
        alertService.setNotifyOnlyHighPriority(notifyOnlyHighPriority);
        
        JOptionPane.showMessageDialog(this,
                "Notification settings have been saved.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Reset notification settings
     */
    private void resetNotificationSettings() {
        // Reset notification checkboxes
        enableNotificationsCheckbox.setSelected(true);
        notifyOnlyHighPriorityCheckbox.setSelected(false);
        
        // Clear muted categories
        mutedCategoriesModel.clear();
        
        // Reset alert service settings
        alertService.resetNotificationSettings();
        
        JOptionPane.showMessageDialog(this,
                "Notification settings have been reset to default.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }
} 