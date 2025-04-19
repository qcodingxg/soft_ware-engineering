package com.financeapp.view;

import com.financeapp.controller.AuthController;
import com.financeapp.controller.TransactionController;
import com.financeapp.model.Transaction;
import com.financeapp.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.MouseAdapter;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.ui.FlatUIUtils;

/**
 * Main Window Frame
 * Using JTabbedPane to display different functional modules
 */
public class MainFrame extends JFrame implements LoginPanel.LoginCallback, RegisterPanel.RegisterCallback {
    
    private final TransactionController transactionController;
    private final AuthController authController;
    private JLabel statusLabel;
    private JTabbedPane tabbedPane;
    
    private DashboardPanel dashboardPanel;
    private TransactionPanel transactionPanel;
    private CategoryManagementPanel categoryManagementPanel;
    private StatisticsPanel statisticsPanel;
    private ExpenseAlertPanel expenseAlertPanel;
    private LocalConsumptionPanel localConsumptionPanel;
    private AIChatPanel aiChatPanel;
    
    private JPanel cardPanel;
    private CardLayout cardLayout;
    
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    
    private static final String LOGIN_CARD = "LOGIN";
    private static final String REGISTER_CARD = "REGISTER";
    private static final String MAIN_CARD = "MAIN";
    
    // 颜色 (与FlatLaf主题兼容)
    private static final Color PRIMARY_COLOR = new Color(24, 115, 204);
    private static final Color SECONDARY_COLOR = new Color(75, 148, 220);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color ERROR_COLOR = new Color(211, 47, 47);
    private static final Color SUCCESS_COLOR = new Color(46, 174, 96);
    
    /**
     * Constructor
     */
    public MainFrame() {
        this.transactionController = new TransactionController();
        this.authController = new AuthController();
        
        initUI();
    }
    
    /**
     * Initialize UI
     */
    private void initUI() {
        // Setup window
        setTitle("Personal Finance Management System");
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create card layout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        add(cardPanel);
        
        // Create login and register panels
        loginPanel = new LoginPanel(authController, this);
        registerPanel = new RegisterPanel(authController, this);
        
        // Create main application panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        // Create custom tabbed pane
        tabbedPane = createStyledTabbedPane();
        
        // Create function panels
        dashboardPanel = new DashboardPanel(transactionController, authController);
        transactionPanel = new TransactionPanel(transactionController);
        categoryManagementPanel = new CategoryManagementPanel(transactionController);
        statisticsPanel = new StatisticsPanel(transactionController);
        expenseAlertPanel = new ExpenseAlertPanel(transactionController);
        localConsumptionPanel = new LocalConsumptionPanel(transactionController);
        
        // Create AI chat panel and add to container
        aiChatPanel = new AIChatPanel();
        JPanel aiChatContainer = new JPanel(new BorderLayout());
        aiChatContainer.add(aiChatPanel, BorderLayout.CENTER);
        
        // Add tabs
        addStyledTab(tabbedPane, "Dashboard", dashboardPanel, "Welcome to your financial dashboard", "dashboard");
        addStyledTab(tabbedPane, "Transactions", transactionPanel, "Add and import transaction records", "transaction"); 
        addStyledTab(tabbedPane, "Categories", categoryManagementPanel, "View and manage transaction categories", "category");
        addStyledTab(tabbedPane, "Statistics", statisticsPanel, "View spending statistics and budget suggestions", "statistics");
        addStyledTab(tabbedPane, "Alerts", expenseAlertPanel, "Monitor and get alerted when expenses exceed thresholds", "alert");
        addStyledTab(tabbedPane, "Local Spending", localConsumptionPanel, "Analyze local consumption patterns", "local");
        addStyledTab(tabbedPane, "AI Assistant", aiChatContainer, "Chat with AI assistant for financial advice", "chat");
        
        // Add to main panel
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Create status bar
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(UIConstants.SMALL_FONT);
        statusLabel.setForeground(UIConstants.TEXT_COLOR);
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(218, 220, 224)),
                new EmptyBorder(UIConstants.PADDING_SMALL, UIConstants.PADDING, UIConstants.PADDING_SMALL, UIConstants.PADDING)));
        statusLabel.setBackground(UIConstants.CARD_BACKGROUND_COLOR);
        statusLabel.setOpaque(true);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Add menu bar
        setJMenuBar(createMenuBar());
        
        // Add panels to card layout
        cardPanel.add(loginPanel, LOGIN_CARD);
        cardPanel.add(registerPanel, REGISTER_CARD);
        cardPanel.add(mainPanel, MAIN_CARD);
        
        // Show login panel first
        cardLayout.show(cardPanel, LOGIN_CARD);
    }
    
    /**
     * Create styled tabbed pane
     */
    private JTabbedPane createStyledTabbedPane() {
        JTabbedPane pane = new JTabbedPane();
        pane.setFont(UIConstants.BODY_FONT);
        pane.setBackground(UIConstants.BACKGROUND_COLOR);
        pane.setForeground(UIConstants.TEXT_COLOR);
        pane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        
        // Use FlatLaf flat UI
        pane.putClientProperty("JTabbedPane.tabHeight", 36);
        pane.putClientProperty("JTabbedPane.tabInsets", new Insets(8, 12, 8, 12));
        pane.putClientProperty("JTabbedPane.selectedBackground", UIConstants.CARD_BACKGROUND_COLOR);
        pane.putClientProperty("JTabbedPane.selectedForeground", UIConstants.PRIMARY_COLOR);
        pane.putClientProperty("JTabbedPane.tabSeparatorsFullHeight", true);
        pane.putClientProperty("JTabbedPane.showTabSeparators", true);
        pane.putClientProperty("JTabbedPane.tabAreaAlignment", "fill");
        pane.putClientProperty("JTabbedPane.minimumTabWidth", 80);
        pane.putClientProperty("JTabbedPane.tabSelectionHeight", 3);
        
        return pane;
    }
    
    /**
     * Add styled tab to tabbed pane
     */
    private void addStyledTab(JTabbedPane pane, String title, Component component, String tooltip, String iconName) {
        // Special handling for AI Assistant panel
        if (title.equals("AI Assistant")) {
            try {
                // Create a regular panel as container for AIChatPanel
                JPanel containerPanel = new JPanel(new BorderLayout());
                containerPanel.add(component, BorderLayout.CENTER);
                pane.addTab(title, containerPanel);
                int index = pane.indexOfTab(title);
                pane.setToolTipTextAt(index, tooltip);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Normal handling for other panels
        pane.addTab(title, component);
        int index = pane.indexOfTab(title);
        pane.setToolTipTextAt(index, tooltip);
        
        // Create custom tab component
        JPanel tabComponent = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        tabComponent.setOpaque(false);
        
        // Add icon (if available)
        try {
            // Try to use FlatLaf's SVG icons (in resources directory)
            // Assumes icon files are stored in resources/icons/
            Icon icon = new FlatSVGIcon("icons/" + iconName + ".svg", 16, 16);
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
            tabComponent.add(iconLabel);
        } catch (Exception e) {
            // If icon loading fails, use text only
            System.out.println("Icon loading failed: " + iconName);
        }
        
        // Add title text
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIConstants.BODY_FONT);
        titleLabel.setForeground(UIConstants.TEXT_COLOR);
        tabComponent.add(titleLabel);
        
        pane.setTabComponentAt(index, tabComponent);
    }
    
    /**
     * Custom TabbedPane UI
     */
    private class StyledTabbedPaneUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabAreaInsets.set(4, 4, 2, 4);
            contentBorderInsets.set(4, 4, 4, 4);
            tabInsets = new Insets(6, 12, 6, 12);
            selectedTabPadInsets = new Insets(2, 2, 2, 2);
        }
        
        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (isSelected) {
                g2d.setColor(PRIMARY_COLOR);
                g2d.fillRoundRect(x, y, w, h, 10, 10);
                
                // Draw bottom line for selected tab
                g2d.setColor(PRIMARY_COLOR.darker());
                g2d.fillRect(x, y + h - 3, w, 3);
            } else {
                g2d.setColor(SECONDARY_COLOR.brighter());
                g2d.fillRoundRect(x, y + 2, w, h - 2, 10, 10);
            }
            
            g2d.dispose();
        }
        
        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            // Skip default background painting as we do it in paintTabBorder
        }
        
        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            int width = tabPane.getWidth();
            int height = tabPane.getHeight();
            Insets insets = tabPane.getInsets();
            Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
            
            int x = insets.left;
            int y = insets.top;
            int w = width - insets.right - insets.left;
            int h = height - insets.top - insets.bottom;
            
            switch (tabPlacement) {
                case LEFT:
                    x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                    w -= x - insets.left;
                    break;
                case RIGHT:
                    w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
                    break;
                case BOTTOM:
                    h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                    break;
                case TOP:
                default:
                    y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                    h -= y - insets.top;
            }
            
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw content border
            g2d.setColor(PRIMARY_COLOR);
            g2d.drawRoundRect(x, y, w - 1, h - 1, 10, 10);
            
            // Fill content area with lighter color
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(x + 1, y + 1, w - 3, h - 3, 8, 8);
            
            g2d.dispose();
        }
        
        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            // Don't paint focus indicator
        }
        
        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            if (isSelected) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
            } else {
                g2d.setColor(TEXT_COLOR);
                g2d.setFont(font);
            }
            
            g2d.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            g2d.dispose();
        }
    }
    
    /**
     * Create menu bar
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PRIMARY_COLOR);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, PRIMARY_COLOR.darker()));
        
        // File menu
        JMenu fileMenu = createStyledMenu("File");
        
        JMenuItem refreshItem = createStyledMenuItem("Refresh Data", e -> loadData());
        fileMenu.add(refreshItem);
        
        fileMenu.addSeparator();
        
        JMenuItem logoutItem = createStyledMenuItem("Logout", e -> logout());
        fileMenu.add(logoutItem);
        
        JMenuItem exitItem = createStyledMenuItem("Exit", e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // Help menu
        JMenu helpMenu = createStyledMenu("Help");
        
        JMenuItem aboutItem = createStyledMenuItem("About", e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    /**
     * Create styled menu
     */
    private JMenu createStyledMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        menu.setForeground(Color.BLACK);
        menu.setBackground(PRIMARY_COLOR);
        menu.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        
        // Add hover effect
        menu.addChangeListener(e -> {
            JMenu source = (JMenu) e.getSource();
            if (source.isSelected()) {
                source.setBackground(PRIMARY_COLOR.darker());
            } else {
                source.setBackground(PRIMARY_COLOR);
            }
        });
        
        return menu;
    }
    
    /**
     * Create styled menu item
     */
    private JMenuItem createStyledMenuItem(String text, ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        menuItem.setForeground(Color.BLACK);
        menuItem.setBackground(PRIMARY_COLOR);
        menuItem.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        menuItem.addActionListener(listener);
        
        // Add hover effect
        menuItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                menuItem.setBackground(PRIMARY_COLOR.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                menuItem.setBackground(PRIMARY_COLOR);
            }
        });
        
        return menuItem;
    }
    
    /**
     * Load application data
     */
    private void loadData() {
        try {
            // Check data directory
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdir();
            }
            
            // Load transaction data
            transactionController.loadTransactions();
            
            // Update status bar
            updateStatusBar("Data loaded successfully");
            
            // Refresh all panels
            dashboardPanel.updateDashboard();
            transactionPanel.updateTransactionList();
            categoryManagementPanel.updateCategorySummary();
            statisticsPanel.updateStatistics();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            updateStatusBar("Error loading data: " + e.getMessage());
        }
    }
    
    /**
     * Update status bar with message and current time
     */
    private void updateStatusBar(String message) {
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        StringBuilder status = new StringBuilder(message);
        status.append(" | ").append(timeStamp);
        
        // Add user info if logged in
        if (authController.isLoggedIn()) {
            status.append(" | User: ").append(authController.getCurrentUser().getUsername());
        }
        
        statusLabel.setText(status.toString());
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
                
        if (choice == JOptionPane.YES_OPTION) {
            authController.logout();
            cardLayout.show(cardPanel, LOGIN_CARD);
            statusLabel.setText("Logged out");
        }
    }
    
    /**
     * Show about dialog
     */
    private void showAboutDialog() {
        JPanel aboutPanel = new JPanel(new BorderLayout(20, 20));
        aboutPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        aboutPanel.setBackground(Color.WHITE);
        
        // App icon/logo
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(new ImageIcon(getClass().getResource("/icons/app_icon.png")));
        if (iconLabel.getIcon() == null) {
            // Fallback if icon not found
            JPanel colorPanel = new JPanel();
            colorPanel.setPreferredSize(new Dimension(64, 64));
            colorPanel.setBackground(PRIMARY_COLOR);
            iconLabel.add(colorPanel);
        }
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        aboutPanel.add(iconLabel, BorderLayout.NORTH);
        
        // App info
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        infoPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Personal Finance Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JLabel versionLabel = new JLabel("Version 1.0.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JLabel copyrightLabel = new JLabel("© 2023 Finance App Team");
        copyrightLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JLabel descLabel = new JLabel("A simple application to manage personal finances");
        descLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        
        infoPanel.add(titleLabel);
        infoPanel.add(versionLabel);
        infoPanel.add(copyrightLabel);
        infoPanel.add(descLabel);
        
        aboutPanel.add(infoPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeButton.setBackground(PRIMARY_COLOR);
        closeButton.setForeground(Color.WHITE);
        buttonPanel.add(closeButton);
        
        aboutPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Create dialog
        JDialog aboutDialog = new JDialog(this, "About", true);
        aboutDialog.setContentPane(aboutPanel);
        aboutDialog.setSize(400, 300);
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setResizable(false);
        
        // Add action to close button
        closeButton.addActionListener(e -> aboutDialog.dispose());
        
        // Show dialog
        aboutDialog.setVisible(true);
    }
    
    /**
     * LoginCallback implementation: called on successful login
     */
    @Override
    public void onLoginSuccess() {
        // Show main application
        cardLayout.show(cardPanel, MAIN_CARD);
        
        // Load application data
        loadData();
        
        // Update status bar
        updateStatusBar("Logged in successfully");
    }
    
    /**
     * Login callback implementation: called when registration is requested
     */
    @Override
    public void onRegisterRequest() {
        cardLayout.show(cardPanel, REGISTER_CARD);
    }
    
    /**
     * RegisterCallback implementation: called on successful registration
     */
    @Override
    public void onRegisterSuccess() {
        cardLayout.show(cardPanel, LOGIN_CARD);
        updateStatusBar("Registration successful. Please login.");
    }
    
    /**
     * RegisterCallback implementation: called on back button click
     */
    @Override
    public void onBackToLogin() {
        cardLayout.show(cardPanel, LOGIN_CARD);
    }
    
    /**
     * Main method
     */
    public static void main(String[] args) {
        try {
            // Use FlatLaf modern theme
            com.formdev.flatlaf.FlatLightLaf.setup();
            
            // Apply global UI settings
            UIManager.put("Button.arc", 8); // Button corner radius
            UIManager.put("Component.arc", 8); // Component corner radius
            UIManager.put("ProgressBar.arc", 8); // Progress bar corner radius
            UIManager.put("TextComponent.arc", 8); // Text component corner radius
            
            // Set default font
            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 13);
            UIManager.put("defaultFont", defaultFont);
            
            // Set anti-aliasing for better text rendering
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to system look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        // Launch application
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
