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
    
    // Add to class variables
    private JPanel chartPanel;
    private Map<String, Double> categoryExpenses = new HashMap<>();
    private Color[] categoryColors = {
        PRIMARY_COLOR,
        SECONDARY_COLOR,
        new Color(46, 204, 113),
        new Color(241, 196, 15),
        new Color(231, 76, 60),
        new Color(155, 89, 182)
    };
    
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
        
        // Create main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setOpaque(false);
        
        // Create left column (2/3 width)
        JPanel leftPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        leftPanel.setOpaque(false);
        
        // Create overview and quick actions in a panel
        JPanel topLeftPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        topLeftPanel.setOpaque(false);
        
        // Create individual panels
        overviewPanel = createOverviewPanel();
        quickActionsPanel = createQuickActionsPanel();
        recentTransactionsPanel = createRecentTransactionsPanel();
        
        // Add to top left panel
        topLeftPanel.add(overviewPanel);
        topLeftPanel.add(quickActionsPanel);
        
        // Add to left panel
        leftPanel.add(topLeftPanel);
        leftPanel.add(recentTransactionsPanel);
        
        // Create summary panel
        JPanel summaryPanel = createSummaryPanel();
        leftPanel.add(summaryPanel);
        
        // Create right column (1/3 width)
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        rightPanel.setOpaque(false);
        
        // Create individual right panels
        tipsPanel = createTipsPanel();
        alertsPanel = createAlertsPanel();
        
        // Add to right panel
        rightPanel.add(tipsPanel);
        rightPanel.add(alertsPanel);
        
        // Add columns to main panel
        mainPanel.add(leftPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        // Set preferred size for right panel (1/3 of width)
        rightPanel.setPreferredSize(new Dimension(300, 0));
        
        // Add main panel to dashboard
        add(mainPanel, BorderLayout.CENTER);
        
        // Initial data update
        updateDashboard();
        
        // Verify tab availability
        verifyTabAvailability();
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
        
        // Create date and stats panel
        JPanel rightPanel = new JPanel(new BorderLayout(0, 5));
        rightPanel.setOpaque(false);
        
        // Create date display
        LocalDate today = LocalDate.now();
        JLabel dateLabel = new JLabel(today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dateLabel.setForeground(TEXT_COLOR);
        rightPanel.add(dateLabel, BorderLayout.NORTH);
        
        // Add stats summary
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);
        
        try {
            List<Transaction> transactions = transactionController.getTransactions();
            int transactionCount = transactions.size();
            
            // Extract unique categories from transactions
            Set<String> uniqueCategories = new HashSet<>();
            for (Transaction t : transactions) {
                uniqueCategories.add(t.getCategory());
            }
            int categoryCount = uniqueCategories.size();
            
            JLabel transactionLabel = new JLabel("Transactions: " + transactionCount);
            transactionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            transactionLabel.setForeground(SECONDARY_COLOR);
            
            JLabel categoryLabel = new JLabel("Categories: " + categoryCount);
            categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            categoryLabel.setForeground(SECONDARY_COLOR);
            
            statsPanel.add(transactionLabel);
            statsPanel.add(categoryLabel);
        } catch (Exception e) {
            // Ignore if data isn't available yet
        }
        
        rightPanel.add(statsPanel, BorderLayout.SOUTH);
        panel.add(rightPanel, BorderLayout.EAST);
        
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
        panel.setLayout(new GridLayout(5, 1, 5, 10));
        
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
        
        // Local spending indicator
        JPanel localSpendingPanel = new JPanel(new BorderLayout(5, 0));
        localSpendingPanel.setOpaque(false);
        JLabel localSpendingTitle = new JLabel("Local Spending:");
        localSpendingTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        localSpendingTitle.setForeground(TEXT_COLOR);
        localSpendingPanel.add(localSpendingTitle, BorderLayout.WEST);
        
        JLabel localSpendingValue = new JLabel("View Analysis");
        localSpendingValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        localSpendingValue.setForeground(SECONDARY_COLOR);
        localSpendingValue.setCursor(new Cursor(Cursor.HAND_CURSOR));
        localSpendingValue.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showLocalConsumptionTab();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                localSpendingValue.setText("<html><u>View Analysis</u></html>");
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                localSpendingValue.setText("View Analysis");
            }
        });
        localSpendingPanel.add(localSpendingValue, BorderLayout.EAST);
        
        // Add all components to panel
        panel.add(balancePanel);
        panel.add(incomePanel);
        panel.add(expensesPanel);
        panel.add(budgetPanel);
        panel.add(localSpendingPanel);
        
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
        panel.setLayout(new GridLayout(2, 3, 10, 10));
        
        // Create action buttons
        JButton addTransactionButton = createActionButton("Add Transaction", "Add a new transaction record");
        addTransactionButton.addActionListener(e -> showTransactionTab());
        
        JButton importButton = createActionButton("Import CSV", "Import transactions from CSV file");
        importButton.addActionListener(e -> importCSV());
        
        JButton viewStatsButton = createActionButton("View Statistics", "Check your spending patterns");
        viewStatsButton.addActionListener(e -> showStatisticsTab());
        
        JButton aiAnalysisButton = createActionButton("AI Assistant", "Get AI-powered financial advice");
        aiAnalysisButton.addActionListener(e -> showAIAdvisorTab());
        
        JButton categoryButton = createActionButton("Categories", "Manage your transaction categories");
        categoryButton.addActionListener(e -> showCategoriesTab());
        
        JButton localSpendingButton = createActionButton("Local Spending", "Analyze local consumption patterns");
        localSpendingButton.addActionListener(e -> showLocalConsumptionTab());
        
        // Add buttons to panel
        panel.add(addTransactionButton);
        panel.add(importButton);
        panel.add(viewStatsButton);
        panel.add(aiAnalysisButton);
        panel.add(categoryButton);
        panel.add(localSpendingButton);
        
        return panel;
    }
    
    /**
     * Create tips panel with AI-generated financial tips
     */
    private JPanel createTipsPanel() {
        JPanel panel = createCardPanel("AI Financial Tips");
        panel.setLayout(new BorderLayout(0, 10));
        
        // Create tips content
        JPanel tipsContentPanel = new JPanel();
        tipsContentPanel.setLayout(new BoxLayout(tipsContentPanel, BoxLayout.Y_AXIS));
        tipsContentPanel.setOpaque(false);
        
        // Add a small AI icon
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        iconPanel.setOpaque(false);
        JLabel aiIconLabel = new JLabel(createRobotIcon());
        JLabel aiLabel = new JLabel("AI Financial Advisor");
        aiLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        aiLabel.setForeground(PRIMARY_COLOR);
        iconPanel.add(aiIconLabel);
        iconPanel.add(aiLabel);
        
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
        
        // Add components
        panel.add(iconPanel, BorderLayout.NORTH);
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
        
        JButton chatButton = new JButton("Chat with AI Assistant");
        chatButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chatButton.setBackground(PRIMARY_COLOR);
        chatButton.setForeground(Color.WHITE);
        chatButton.setFocusPainted(false);
        chatButton.setBorderPainted(false);
        chatButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chatButton.addActionListener(e -> showAIAdvisorTab());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(chatButton);
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
            
            // Update category expenses for pie chart
            updateCategoryExpenses(transactions);
            
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
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        if (hour >= 5 && hour < 12) {
            return "Good morning";
        } else if (hour >= 12 && hour < 18) {
            return "Good afternoon";
        } else {
            return "Good evening";
        }
    }
    
    /**
     * Generic method to navigate to a specific tab by index
     * @param tabIndex the index of the tab to navigate to
     */
    private void navigateToTab(int tabIndex) {
        JTabbedPane tabbedPane = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
        if (tabbedPane != null && tabIndex >= 0 && tabIndex < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(tabIndex);
        }
    }

    /**
     * Show transactions tab (index 1)
     */
    private void showTransactionTab() {
        navigateToTab(1);
    }

    /**
     * Show categories tab (index 2)
     */
    private void showCategoriesTab() {
        navigateToTab(2);
    }

    /**
     * Show statistics tab (index 3)
     */
    private void showStatisticsTab() {
        navigateToTab(3);
    }

    /**
     * Show expense alerts tab (index 4)
     */
    private void showExpenseAlertsTab() {
        navigateToTab(4);
    }

    /**
     * Show local consumption tab (index 5)
     */
    private void showLocalConsumptionTab() {
        navigateToTab(5);
    }

    /**
     * Show AI Advisor tab (index 6)
     */
    private void showAIAdvisorTab() {
        navigateToTab(6);
    }

    /**
     * Navigate to transactions tab and show all transactions
     */
    private void showTransactionsTab() {
        showTransactionTab();
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
        // Silent refresh without showing popup dialog
        // Refresh tips content here
        updateTipsContent();
    }
    
    /**
     * Update the contents of the tips panel with new financial tips
     */
    private void updateTipsContent() {
        try {
            // Clear existing content in tips panel
            // 由于我们添加了一个iconPanel，滚动面板现在是第二个组件(index 1)
            JPanel tipsContent = (JPanel) ((JScrollPane) tipsPanel.getComponent(1)).getViewport().getView();
            tipsContent.removeAll();
            
            // Add new financial tips
            String[] tips = {
                "Save at least 20% of your monthly income for future goals.",
                "Track your expenses daily to stay aware of your spending habits.",
                "Consider setting up an emergency fund covering 3-6 months of expenses.",
                "Review your subscriptions regularly and cancel unused services.",
                "When making large purchases, follow the 24-hour rule to avoid impulse buying."
            };
            
            // Display a random selection of tips
            Random random = new Random();
            int numTips = Math.min(3, tips.length);
            Set<Integer> selectedIndices = new HashSet<>();
            
            while (selectedIndices.size() < numTips) {
                selectedIndices.add(random.nextInt(tips.length));
            }
            
            boolean first = true;
            for (Integer index : selectedIndices) {
                if (!first) {
                    tipsContent.add(Box.createVerticalStrut(10));
                }
                tipsContent.add(createTipPanel(tips[index]));
                first = false;
            }
            
            // Refresh UI
            tipsContent.revalidate();
            tipsContent.repaint();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        
        // Add title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        JLabel alertIconLabel = new JLabel("⚠");
        alertIconLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        alertIconLabel.setForeground(ERROR_COLOR);
        JLabel alertStatusLabel = new JLabel("Active alerts will appear here");
        alertStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        alertStatusLabel.setForeground(TEXT_COLOR);
        titlePanel.add(alertIconLabel);
        titlePanel.add(alertStatusLabel);
        
        // Add to alerts panel
        alertsContent.add(titlePanel);
        alertsContent.add(Box.createVerticalStrut(10));
        
        // Add initial empty state message
        JLabel initialLabel = new JLabel("Loading alerts...", JLabel.CENTER);
        initialLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        initialLabel.setForeground(SECONDARY_COLOR);
        alertsContent.add(initialLabel);
        
        // Add "View All" button and AI analysis button
        JButton viewAllButton = new JButton("View All Alerts");
        viewAllButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewAllButton.setBackground(ERROR_COLOR);
        viewAllButton.setForeground(Color.WHITE);
        viewAllButton.setFocusPainted(false);
        viewAllButton.setBorderPainted(false);
        viewAllButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllButton.addActionListener(e -> showExpenseAlertsTab());
        
        JButton aiAnalysisButton = new JButton("AI Spending Analysis");
        aiAnalysisButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        aiAnalysisButton.setBackground(PRIMARY_COLOR);
        aiAnalysisButton.setForeground(Color.WHITE);
        aiAnalysisButton.setFocusPainted(false);
        aiAnalysisButton.setBorderPainted(false);
        aiAnalysisButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        aiAnalysisButton.addActionListener(e -> showAIAdvisorTab());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(aiAnalysisButton);
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
     * Create summary panel showing categorical breakdown
     */
    private JPanel createSummaryPanel() {
        JPanel panel = createCardPanel("Expense Categories");
        panel.setLayout(new BorderLayout(10, 10));
        
        // Create pie chart placeholder
        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int size = Math.min(width, height) - 40;
                int x = (width - size) / 2;
                int y = (height - size) / 2;
                
                // If no data, show empty chart
                if (categoryExpenses.isEmpty()) {
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.fillOval(x, y, size, size);
                    
                    // Draw center circle for donut chart
                    int innerSize = size / 2;
                    g2d.setColor(CARD_BG_COLOR);
                    g2d.fillOval(x + size/4, y + size/4, innerSize, innerSize);
                    
                    // Draw "No Data" text
                    g2d.setColor(TEXT_COLOR);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    FontMetrics fm = g2d.getFontMetrics();
                    String noDataText = "No Data";
                    int textWidth = fm.stringWidth(noDataText);
                    g2d.drawString(noDataText, x + (size - textWidth) / 2, y + size / 2 + fm.getAscent() / 2);
                    
                    return;
                }
                
                // Calculate total
                double total = categoryExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
                if (total <= 0) {
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.fillOval(x, y, size, size);
                    return;
                }
                
                // Draw pie chart
                int startAngle = 0;
                int colorIndex = 0;
                for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
                    int arcAngle = (int) (360.0 * entry.getValue() / total);
                    if (arcAngle > 0) {
                        g2d.setColor(categoryColors[colorIndex % categoryColors.length]);
                        g2d.fillArc(x, y, size, size, startAngle, arcAngle);
                        startAngle += arcAngle;
                    }
                    colorIndex++;
                }
                
                // Draw center circle (for donut chart effect)
                int innerSize = size / 2;
                g2d.setColor(CARD_BG_COLOR);
                g2d.fillOval(x + size/4, y + size/4, innerSize, innerSize);
            }
        };
        chartPanel.setPreferredSize(new Dimension(0, 150));
        chartPanel.setBackground(CARD_BG_COLOR);
        
        // Make chart clickable to navigate to categories
        chartPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chartPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showCategoriesTab();
            }
        });
        
        // Add legend
        JPanel legendPanel = new JPanel(new GridLayout(1, 5, 5, 0));
        legendPanel.setOpaque(false);
        
        String[] categories = {"Food", "Transport", "Shopping", "Bills", "Other"};
        
        for (int i = 0; i < categories.length; i++) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            item.setOpaque(false);
            
            JPanel colorBox = new JPanel();
            colorBox.setPreferredSize(new Dimension(12, 12));
            colorBox.setBackground(categoryColors[i % categoryColors.length]);
            
            JLabel label = new JLabel(categories[i]);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            
            // Make each legend item clickable
            final int categoryIndex = i;
            item.setCursor(new Cursor(Cursor.HAND_CURSOR));
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showCategoriesTab();
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    label.setForeground(PRIMARY_COLOR);
                    label.setText("<html><u>" + categories[categoryIndex] + "</u></html>");
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    label.setForeground(TEXT_COLOR);
                    label.setText(categories[categoryIndex]);
                }
            });
            
            item.add(colorBox);
            item.add(label);
            legendPanel.add(item);
        }
        
        // Add view statistics and manage categories buttons
        JButton viewStatsButton = new JButton("Detailed Statistics");
        viewStatsButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewStatsButton.setBackground(SECONDARY_COLOR);
        viewStatsButton.setForeground(Color.WHITE);
        viewStatsButton.setFocusPainted(false);
        viewStatsButton.setBorderPainted(false);
        viewStatsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewStatsButton.addActionListener(e -> showStatisticsTab());
        
        JButton manageCategoriesButton = new JButton("Manage Categories");
        manageCategoriesButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        manageCategoriesButton.setBackground(PRIMARY_COLOR);
        manageCategoriesButton.setForeground(Color.WHITE);
        manageCategoriesButton.setFocusPainted(false);
        manageCategoriesButton.setBorderPainted(false);
        manageCategoriesButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        manageCategoriesButton.addActionListener(e -> showCategoriesTab());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(manageCategoriesButton);
        buttonPanel.add(viewStatsButton);
        
        // Add components to panel
        panel.add(chartPanel, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(legendPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(southPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * Update category expenses for pie chart
     */
    private void updateCategoryExpenses(List<Transaction> transactions) {
        // Clear previous data
        categoryExpenses.clear();
        
        // Process only expenses (negative amounts)
        for (Transaction t : transactions) {
            double amount = t.getAmount();
            if (amount < 0) { // It's an expense
                String category = t.getCategory();
                double absAmount = Math.abs(amount);
                
                // Update category total
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + absAmount);
            }
        }
        
        // Merge small categories into "Other"
        double total = categoryExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
        double threshold = total * 0.05; // Categories less than 5% go to "Other"
        
        Map<String, Double> mergedCategories = new HashMap<>();
        double otherTotal = 0;
        
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            if (entry.getValue() < threshold) {
                otherTotal += entry.getValue();
            } else {
                mergedCategories.put(entry.getKey(), entry.getValue());
            }
        }
        
        if (otherTotal > 0) {
            mergedCategories.put("Other", otherTotal);
        }
        
        // Sort by amount (descending)
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(mergedCategories.entrySet());
        sortedEntries.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        
        // Update categoryExpenses with sorted data
        categoryExpenses.clear();
        for (Map.Entry<String, Double> entry : sortedEntries) {
            categoryExpenses.put(entry.getKey(), entry.getValue());
        }
        
        // Request repaint of chart panel
        if (chartPanel != null) {
            chartPanel.repaint();
        }
    }

    /**
     * Check if a tab at the specified index exists in the parent tabbed pane
     * @param tabIndex the index to check
     * @return true if the tab exists
     */
    private boolean tabExists(int tabIndex) {
        JTabbedPane tabbedPane = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
        return tabbedPane != null && tabIndex >= 0 && tabIndex < tabbedPane.getTabCount();
    }

    /**
     * Verify if all required tabs are present and update UI accordingly
     * Should be called after the dashboard is fully initialized
     */
    private void verifyTabAvailability() {
        // Check if essential tabs exist
        boolean transactionsTabExists = tabExists(1);
        boolean categoriesTabExists = tabExists(2);
        boolean statisticsTabExists = tabExists(3);
        boolean alertsTabExists = tabExists(4);
        boolean localConsumptionTabExists = tabExists(5);
        boolean aiAssistantTabExists = tabExists(6);
        
        // Update UI based on tab availability
        // You might want to disable buttons or show error messages if tabs are missing
        
        // This is just a notification in console for debugging purposes
        if (!transactionsTabExists || !categoriesTabExists || !statisticsTabExists || 
                !alertsTabExists || !localConsumptionTabExists || !aiAssistantTabExists) {
            System.out.println("Warning: Some tabs are missing in the dashboard. Navigation may not work properly.");
        } else {
            System.out.println("All dashboard navigation tabs are available.");
        }
    }
} 