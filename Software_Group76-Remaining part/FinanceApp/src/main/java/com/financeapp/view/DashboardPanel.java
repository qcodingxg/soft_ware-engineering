package com.financeapp.view;

import com.financeapp.controller.AuthController;
import com.financeapp.controller.TransactionController;
import com.financeapp.model.Transaction;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Dashboard Panel
 * Main welcome screen after login, displaying financial overview and quick links
 */
public class DashboardPanel extends JPanel {
    
    private final TransactionController transactionController;
    private final AuthController authController;
    
    // UI Components
    private JPanel overviewPanel;
    private JPanel recentTransactionsPanel;
    private JPanel quickActionsPanel;
    private JPanel tipsPanel;
    private JPanel alertsPanel;
    
    // Labels for financial overview
    private JLabel totalExpensesLabel;
    private JLabel totalIncomeLabel;
    private JLabel balanceLabel;
    private JLabel monthlyBudgetLabel;
    
    // Colors (matching app theme)
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color CARD_BG_COLOR = new Color(255, 255, 255);
    
    /**
     * Constructor
     * @param transactionController Transaction controller
     * @param authController Authentication controller
     */
    public DashboardPanel(TransactionController transactionController, AuthController authController) {
        this.transactionController = transactionController;
        this.authController = authController;
        initUI();
    }
    
    /**
     * Initialize UI
     */
    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add welcome header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create main content panel with grid layout
        JPanel contentPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        contentPanel.setOpaque(false);
        add(contentPanel, BorderLayout.CENTER);
        
        // Create individual panels
        overviewPanel = createOverviewPanel();
        recentTransactionsPanel = createRecentTransactionsPanel();
        quickActionsPanel = createQuickActionsPanel();
        tipsPanel = createTipsPanel();
        alertsPanel = createAlertsPanel();
        
        // Add panels to content panel
        contentPanel.add(overviewPanel);
        contentPanel.add(recentTransactionsPanel);
        contentPanel.add(quickActionsPanel);
        contentPanel.add(tipsPanel);
        contentPanel.add(alertsPanel);
        
        // Initial data update
        updateDashboard();
    }
    
    /**
     * Create welcome header panel
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        
        // Create welcome message
        String username = authController.isLoggedIn() ? 
                authController.getCurrentUser().getUsername() : "User";
        String greeting = getTimeBasedGreeting();
        
        JLabel welcomeLabel = new JLabel(greeting + ", " + username + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(PRIMARY_COLOR);
        panel.add(welcomeLabel, BorderLayout.WEST);
        
        // Create date display
        LocalDate today = LocalDate.now();
        JLabel dateLabel = new JLabel(today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dateLabel.setForeground(TEXT_COLOR);
        panel.add(dateLabel, BorderLayout.EAST);
        
        // Add separator and motivation message
        JPanel subHeaderPanel = new JPanel(new BorderLayout());
        subHeaderPanel.setOpaque(false);
        
        JLabel motivationLabel = new JLabel("Let's manage your finances today!");
        motivationLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        motivationLabel.setForeground(SECONDARY_COLOR);
        subHeaderPanel.add(motivationLabel, BorderLayout.WEST);
        
        panel.add(subHeaderPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create financial overview panel
     */
    private JPanel createOverviewPanel() {
        JPanel panel = createCardPanel("Financial Overview");
        panel.setLayout(new GridLayout(4, 1, 5, 10));
        
        // Total expenses
        JPanel expensesPanel = new JPanel(new BorderLayout(5, 0));
        expensesPanel.setOpaque(false);
        JLabel expensesTitle = new JLabel("Total Expenses (this month):");
        expensesTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        expensesTitle.setForeground(TEXT_COLOR);
        expensesPanel.add(expensesTitle, BorderLayout.WEST);
        
        totalExpensesLabel = new JLabel("¥0.00");
        totalExpensesLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalExpensesLabel.setForeground(ERROR_COLOR);
        expensesPanel.add(totalExpensesLabel, BorderLayout.EAST);
        
        // Total income
        JPanel incomePanel = new JPanel(new BorderLayout(5, 0));
        incomePanel.setOpaque(false);
        JLabel incomeTitle = new JLabel("Total Income (this month):");
        incomeTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        incomeTitle.setForeground(TEXT_COLOR);
        incomePanel.add(incomeTitle, BorderLayout.WEST);
        
        totalIncomeLabel = new JLabel("¥0.00");
        totalIncomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalIncomeLabel.setForeground(SUCCESS_COLOR);
        incomePanel.add(totalIncomeLabel, BorderLayout.EAST);
        
        // Current balance
        JPanel balancePanel = new JPanel(new BorderLayout(5, 0));
        balancePanel.setOpaque(false);
        JLabel balanceTitle = new JLabel("Current Balance:");
        balanceTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        balanceTitle.setForeground(TEXT_COLOR);
        balancePanel.add(balanceTitle, BorderLayout.WEST);
        
        balanceLabel = new JLabel("¥0.00");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        balanceLabel.setForeground(PRIMARY_COLOR);
        balancePanel.add(balanceLabel, BorderLayout.EAST);
        
        // Monthly budget status
        JPanel budgetPanel = new JPanel(new BorderLayout(5, 0));
        budgetPanel.setOpaque(false);
        JLabel budgetTitle = new JLabel("Budget Status:");
        budgetTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        budgetTitle.setForeground(TEXT_COLOR);
        budgetPanel.add(budgetTitle, BorderLayout.WEST);
        
        monthlyBudgetLabel = new JLabel("On Track");
        monthlyBudgetLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        monthlyBudgetLabel.setForeground(SUCCESS_COLOR);
        budgetPanel.add(monthlyBudgetLabel, BorderLayout.EAST);
        
        // Add all components to panel
        panel.add(balancePanel);
        panel.add(incomePanel);
        panel.add(expensesPanel);
        panel.add(budgetPanel);
        
        return panel;
    }
    
    /**
     * Create recent transactions panel
     */
    private JPanel createRecentTransactionsPanel() {
        JPanel panel = createCardPanel("Recent Transactions");
        panel.setLayout(new BorderLayout(0, 10));
        
        // Create table for recent transactions
        String[] columns = {"Date", "Category", "Amount", "Description"};
        Object[][] data = new Object[0][4]; // Empty initially
        
        JTable transactionsTable = new JTable(data, columns);
        transactionsTable.setFillsViewportHeight(true);
        transactionsTable.setRowHeight(25);
        transactionsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        transactionsTable.getTableHeader().setBackground(SECONDARY_COLOR);
        transactionsTable.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add "View All" button at the bottom
        JButton viewAllButton = new JButton("View All Transactions");
        viewAllButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewAllButton.setBackground(PRIMARY_COLOR);
        viewAllButton.setForeground(Color.WHITE);
        viewAllButton.setFocusPainted(false);
        viewAllButton.setBorderPainted(false);
        viewAllButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllButton.addActionListener(e -> showTransactionsTab());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(viewAllButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create quick actions panel
     */
    private JPanel createQuickActionsPanel() {
        JPanel panel = createCardPanel("Quick Actions");
        panel.setLayout(new GridLayout(2, 2, 10, 10));
        
        // Create action buttons
        JButton addTransactionButton = createActionButton("Add Transaction", "Add a new transaction record");
        addTransactionButton.addActionListener(e -> showTransactionTab());
        
        JButton importButton = createActionButton("Import CSV", "Import transactions from CSV file");
        importButton.addActionListener(e -> importCSV());
        
        JButton viewStatsButton = createActionButton("View Statistics", "Check your spending patterns");
        viewStatsButton.addActionListener(e -> showStatisticsTab());
        
        JButton aiAnalysisButton = createActionButton("AI Analysis", "Get AI-powered financial advice");
        aiAnalysisButton.addActionListener(e -> showAIAdvisorTab());
        
        // Add buttons to panel
        panel.add(addTransactionButton);
        panel.add(importButton);
        panel.add(viewStatsButton);
        panel.add(aiAnalysisButton);
        
        return panel;
    }
    
    /**
     * Create tips panel with AI-generated financial tips
     */
    private JPanel createTipsPanel() {
        JPanel panel = createCardPanel("Financial Tips");
        panel.setLayout(new BorderLayout(0, 10));
        
        // Create tips content
        JPanel tipsContentPanel = new JPanel();
        tipsContentPanel.setLayout(new BoxLayout(tipsContentPanel, BoxLayout.Y_AXIS));
        tipsContentPanel.setOpaque(false);
        
        // Add some sample tips (in real app these would come from AI)
        String[] tips = {
            "Set clear financial goals for better planning",
            "Create an emergency fund for unexpected expenses",
            "Track your daily expenses to identify spending patterns",
            "Consider setting up automatic transfers to savings accounts",
            "Review your subscriptions regularly to avoid unnecessary expenses"
        };
        
        for (String tip : tips) {
            JPanel tipPanel = createTipPanel(tip);
            tipsContentPanel.add(tipPanel);
            tipsContentPanel.add(Box.createVerticalStrut(8));
        }
        
        JScrollPane scrollPane = new JScrollPane(tipsContentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Get more tips button
        JButton refreshTipsButton = new JButton("Refresh Tips");
        refreshTipsButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshTipsButton.setBackground(SECONDARY_COLOR);
        refreshTipsButton.setForeground(Color.WHITE);
        refreshTipsButton.setFocusPainted(false);
        refreshTipsButton.setBorderPainted(false);
        refreshTipsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshTipsButton.addActionListener(e -> refreshTips());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(refreshTipsButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create a panel for a single tip
     */
    private JPanel createTipPanel(String tip) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        
        // Bullet point icon
        JLabel bulletLabel = new JLabel("•");
        bulletLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bulletLabel.setForeground(PRIMARY_COLOR);
        panel.add(bulletLabel, BorderLayout.WEST);
        
        // Tip text
        JLabel tipLabel = new JLabel("<html>" + tip + "</html>");
        tipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tipLabel.setForeground(TEXT_COLOR);
        panel.add(tipLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create robot icon for AI advisor
     */
    private Icon createRobotIcon() {
        // In a real application, you would load an actual icon
        // This creates a simple colored square as a placeholder
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(PRIMARY_COLOR);
                g2d.fillRoundRect(x, y, getIconWidth(), getIconHeight(), 5, 5);
                g2d.setColor(Color.WHITE);
                
                // Draw simple robot face
                int centerX = x + getIconWidth() / 2;
                int centerY = y + getIconHeight() / 2;
                
                // Eyes
                g2d.fillOval(centerX - 7, centerY - 5, 4, 4);
                g2d.fillOval(centerX + 3, centerY - 5, 4, 4);
                
                // Mouth
                g2d.drawLine(centerX - 5, centerY + 5, centerX + 5, centerY + 5);
                
                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 24;
            }

            @Override
            public int getIconHeight() {
                return 24;
            }
        };
    }
    
    /**
     * Create a card panel with title and consistent styling
     */
    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        title,
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        new Font("Segoe UI", Font.BOLD, 16),
                        PRIMARY_COLOR
                    ),
                    BorderFactory.createEmptyBorder(10, 15, 15, 15)
                )
            )
        ));
        return panel;
    }
    
    /**
     * Create an action button with hover effects
     */
    private JButton createActionButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(SECONDARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setToolTipText(tooltip);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(SECONDARY_COLOR.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SECONDARY_COLOR);
            }
        });
        
        return button;
    }
    
    /**
     * Custom border that adds a shadow effect to panels
     */
    private class ShadowBorder extends AbstractBorder {
        private static final int SHADOW_SIZE = 5;
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Bottom shadow
            g2.setColor(new Color(0, 0, 0, 20));
            for (int i = 0; i < SHADOW_SIZE; i++) {
                g2.drawRoundRect(x + i, y + i, width - i * 2, height - i * 2, 10, 10);
            }
            
            g2.dispose();
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
     * Update dashboard with latest data
     */
    public void updateDashboard() {
        try {
            transactionController.loadTransactions();
            List<Transaction> transactions = transactionController.getTransactions();
            
            // Update financial overview
            updateFinancialOverview(transactions);
            
            // Update recent transactions
            updateRecentTransactions(transactions);
            
            // Refresh tips
            refreshTips();
            
            // Update expense alerts
            updateExpenseAlerts();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error updating dashboard: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Update financial overview based on transaction data
     */
    private void updateFinancialOverview(List<Transaction> transactions) {
        // Get current month transactions
        YearMonth currentMonth = YearMonth.now();
        List<Transaction> monthlyTransactions = new ArrayList<>();
        
        for (Transaction t : transactions) {
            if (YearMonth.from(t.getDate()).equals(currentMonth)) {
                monthlyTransactions.add(t);
            }
        }
        
        // Calculate totals
        double totalIncome = 0;
        double totalExpenses = 0;
        
        for (Transaction t : monthlyTransactions) {
            double amount = t.getAmount();
            if (amount > 0) {
                totalIncome += amount;
            } else {
                totalExpenses += Math.abs(amount);
            }
        }
        
        double balance = totalIncome - totalExpenses;
        
        // Update UI
        totalIncomeLabel.setText(String.format("¥%.2f", totalIncome));
        totalExpensesLabel.setText(String.format("¥%.2f", totalExpenses));
        balanceLabel.setText(String.format("¥%.2f", balance));
        
        // Update budget status
        if (balance < 0) {
            monthlyBudgetLabel.setText("Over Budget");
            monthlyBudgetLabel.setForeground(ERROR_COLOR);
        } else if (totalExpenses > totalIncome * 0.8) {
            monthlyBudgetLabel.setText("Approaching Limit");
            monthlyBudgetLabel.setForeground(WARNING_COLOR);
        } else {
            monthlyBudgetLabel.setText("On Track");
            monthlyBudgetLabel.setForeground(SUCCESS_COLOR);
        }
    }
    
    /**
     * Update recent transactions table
     */
    private void updateRecentTransactions(List<Transaction> allTransactions) {
        // Get the table from the scroll pane
        JScrollPane scrollPane = (JScrollPane) recentTransactionsPanel.getComponent(0);
        JTable table = (JTable) scrollPane.getViewport().getView();
        
        // Sort transactions by date (newest first)
        List<Transaction> sortedTransactions = new ArrayList<>(allTransactions);
        sortedTransactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));
        
        // Take only the 5 most recent transactions
        List<Transaction> recentTransactions = sortedTransactions.size() > 5 
                ? sortedTransactions.subList(0, 5) 
                : sortedTransactions;
        
        // Create table model
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        // Add columns
        model.addColumn("Date");
        model.addColumn("Category");
        model.addColumn("Amount");
        model.addColumn("Description");
        
        // Add rows
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Transaction t : recentTransactions) {
            Object[] row = {
                t.getDate().format(dateFormatter),
                t.getCategory(),
                String.format("¥%.2f", t.getAmount()),
                t.getDescription()
            };
            model.addRow(row);
        }
        
        // Set the new model
        table.setModel(model);
    }
    
    /**
     * Get time-based greeting message
     */
    private String getTimeBasedGreeting() {
        int hour = LocalDate.now().atStartOfDay().getHour();
        
        if (hour >= 5 && hour < 12) {
            return "Good morning";
        } else if (hour >= 12 && hour < 18) {
            return "Good afternoon";
        } else {
            return "Good evening";
        }
    }
    
    /**
     * Show transactions tab
     */
    private void showTransactionTab() {
        JTabbedPane tabbedPane = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
        if (tabbedPane != null) {
            tabbedPane.setSelectedIndex(1); // Transaction Entry tab
        }
    }
    
    /**
     * Show categories tab
     */
    private void showCategoriesTab() {
        JTabbedPane tabbedPane = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
        if (tabbedPane != null) {
            tabbedPane.setSelectedIndex(2); // Category Management tab
        }
    }
    
    /**
     * Show statistics tab
     */
    private void showStatisticsTab() {
        JTabbedPane tabbedPane = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
        if (tabbedPane != null) {
            tabbedPane.setSelectedIndex(3); // Statistics View tab
        }
    }
    
    /**
     * Show AI Advisor tab
     */
    private void showAIAdvisorTab() {
        JTabbedPane tabbedPane = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
        if (tabbedPane != null) {
            tabbedPane.setSelectedIndex(4); // AI Advisor tab
        }
    }
    
    /**
     * Import CSV data (redirects to transaction panel)
     */
    private void importCSV() {
        showTransactionTab();
        // Note: The actual import action should be triggered on the TransactionPanel
    }
    
    /**
     * Refresh financial tips
     */
    private void refreshTips() {
        // In a real application, this would fetch new tips from an AI service
        JOptionPane.showMessageDialog(this,
                "Financial tips refreshed!",
                "Tips Updated",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Navigate to transactions tab and show all transactions
     */
    private void showTransactionsTab() {
        showTransactionTab();
    }
    
    /**
     * Create a primary styled button
     */
    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        
        return button;
    }
    
    /**
     * Create expense alerts panel
     */
    private JPanel createAlertsPanel() {
        JPanel panel = createCardPanel("Expense Alerts");
        panel.setLayout(new BorderLayout(0, 10));
        
        // Create alerts content panel
        JPanel alertsContent = new JPanel();
        alertsContent.setLayout(new BoxLayout(alertsContent, BoxLayout.Y_AXIS));
        alertsContent.setOpaque(false);
        
        // Add "View All" button
        JButton viewAllButton = new JButton("View All Alerts");
        viewAllButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewAllButton.setBackground(ERROR_COLOR);
        viewAllButton.setForeground(Color.WHITE);
        viewAllButton.setFocusPainted(false);
        viewAllButton.setBorderPainted(false);
        viewAllButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllButton.addActionListener(e -> showExpenseAlertsTab());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(viewAllButton);
        
        // Add content and button to panel
        panel.add(new JScrollPane(alertsContent), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Update expense alerts panel
     */
    private void updateExpenseAlerts() {
        try {
            // Get current month expenses
            Map<String, Double> currentExpenses = transactionController.getCurrentMonthExpenses();
            
            // Get historical average
            Map<String, Double> historicalAverage = transactionController.getHistoricalAverage();
            
            // Clear existing content
            JPanel alertsContent = (JPanel) ((JScrollPane) alertsPanel.getComponent(0)).getViewport().getView();
            alertsContent.removeAll();
            
            // Alerts list
            List<String> alerts = new ArrayList<>();
            
            // Check if total expenses exceed historical average
            double totalCurrent = currentExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
            double totalHistorical = historicalAverage.values().stream().mapToDouble(Double::doubleValue).sum();
            
            if (totalCurrent > totalHistorical * 1.2) {
                alerts.add(String.format("Total expenses (¥%.2f) are 20%% higher than historical average (¥%.2f)!", 
                        totalCurrent, totalHistorical));
            }
            
            // Check each category
            for (Map.Entry<String, Double> entry : currentExpenses.entrySet()) {
                String category = entry.getKey();
                double amount = entry.getValue();
                Double historicalAmount = historicalAverage.get(category);
                
                if (historicalAmount != null && amount > historicalAmount * 1.2) {
                    alerts.add(String.format("'%s' expenses (¥%.2f) are 20%% higher than historical average (¥%.2f)", 
                            category, amount, historicalAmount));
                }
            }
            
            // Display alerts
            if (alerts.isEmpty()) {
                JLabel noAlertsLabel = new JLabel("No expense alerts at the moment", JLabel.CENTER);
                noAlertsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                noAlertsLabel.setForeground(SUCCESS_COLOR);
                alertsContent.add(noAlertsLabel);
            } else {
                // Show up to 3 alerts
                int count = Math.min(alerts.size(), 3);
                for (int i = 0; i < count; i++) {
                    String alert = alerts.get(i);
                    JPanel alertPanel = createAlertItem(alert);
                    alertsContent.add(alertPanel);
                    if (i < count - 1) {
                        alertsContent.add(Box.createVerticalStrut(5));
                    }
                }
                
                // Show count if there are more alerts
                if (alerts.size() > 3) {
                    JLabel moreLabel = new JLabel("There are " + (alerts.size() - 3) + " more alerts...", JLabel.CENTER);
                    moreLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    moreLabel.setForeground(ERROR_COLOR);
                    alertsContent.add(Box.createVerticalStrut(5));
                    alertsContent.add(moreLabel);
                }
            }
            
            alertsContent.revalidate();
            alertsContent.repaint();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Create alert item panel
     */
    private JPanel createAlertItem(String alertText) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(new Color(255, 235, 230));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        // Warning icon
        JLabel iconLabel = new JLabel("⚠");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        iconLabel.setForeground(ERROR_COLOR);
        panel.add(iconLabel, BorderLayout.WEST);
        
        // Alert text
        JLabel textLabel = new JLabel(alertText);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textLabel.setForeground(ERROR_COLOR);
        panel.add(textLabel, BorderLayout.CENTER);
        
        return panel;
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
} 