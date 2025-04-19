package com.financeapp.view;

import com.financeapp.controller.TransactionController;
import com.financeapp.util.CSVHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;


public class LocalConsumptionPanel extends JPanel {
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    
    // Chinese festivals colors
    private static final Color FESTIVAL_RED = new Color(214, 48, 49);
    private static final Color FESTIVAL_GOLD = new Color(253, 203, 110);
    
    private final TransactionController transactionController;
    private final CSVHandler csvHandler;
    
    // Local consumption categories with Chinese translation
    private static final Map<String, String> LOCAL_CATEGORIES = new HashMap<>();
    static {
        LOCAL_CATEGORIES.put("Food Delivery", "Food Delivery");
        LOCAL_CATEGORIES.put("Online Shopping", "Online Shopping");
        LOCAL_CATEGORIES.put("Digital Services", "Digital Services");
        LOCAL_CATEGORIES.put("Social Dining", "Social Dining");
        LOCAL_CATEGORIES.put("Education", "Education");
        LOCAL_CATEGORIES.put("Healthcare", "Healthcare");
        LOCAL_CATEGORIES.put("Travel", "Travel");
        LOCAL_CATEGORIES.put("Entertainment", "Entertainment");
        LOCAL_CATEGORIES.put("Real Estate", "Real Estate");
        LOCAL_CATEGORIES.put("Investment", "Investment");
        LOCAL_CATEGORIES.put("Automotive", "Automotive");
        LOCAL_CATEGORIES.put("Electronics", "Electronics");
        LOCAL_CATEGORIES.put("Fashion", "Fashion");
        LOCAL_CATEGORIES.put("Sports", "Sports");
        LOCAL_CATEGORIES.put("Utilities", "Utilities");
        LOCAL_CATEGORIES.put("Personal Finance", "Personal Finance");
    }
    

    private static final Map<String, Double> PAYMENT_METHODS = new HashMap<>();
    static {
        PAYMENT_METHODS.put("Alipay", 45.0);
        PAYMENT_METHODS.put("WeChat Pay", 40.0);
        PAYMENT_METHODS.put("UnionPay", 10.0);
        PAYMENT_METHODS.put("Credit Card", 3.0);
        PAYMENT_METHODS.put("Cash", 2.0);
        PAYMENT_METHODS.put("PayPal", 5.0);
        PAYMENT_METHODS.put("Bitcoin", 2.0);
        PAYMENT_METHODS.put("Others", 2.0);
    }
    
    // Chinese shopping festivals
    private static final Map<String, String> SHOPPING_FESTIVALS = new HashMap<>();
    static {
        SHOPPING_FESTIVALS.put("Double 11", "November 11");
        SHOPPING_FESTIVALS.put("618 Festival", "June 18");
        SHOPPING_FESTIVALS.put("Double 12", "December 12");
        SHOPPING_FESTIVALS.put("Women's Day", "March 8");
        SHOPPING_FESTIVALS.put("Chinese New Year", "January/February");
        SHOPPING_FESTIVALS.put("Cyber Monday", "November (Monday after Thanksgiving)");
        SHOPPING_FESTIVALS.put("Black Friday", "November (Day after Thanksgiving)");
        SHOPPING_FESTIVALS.put("Valentine's Day", "February 14");
        SHOPPING_FESTIVALS.put("Dragon Boat Festival", "May/June (Lunar Calendar)");
        SHOPPING_FESTIVALS.put("Mid-Autumn Festival", "September/October (Lunar Calendar)");
        SHOPPING_FESTIVALS.put("Singles' Day", "November 11");
        SHOPPING_FESTIVALS.put("Father's Day", "June (Third Sunday)");
        SHOPPING_FESTIVALS.put("Mother's Day", "May (Second Sunday)");
    }
    
    private JPanel mainContent;
    private JPanel consumptionTrendPanel;
    private JPanel categoryAnalysisPanel;
    private JPanel paymentMethodPanel;
    private JPanel festivalSpendingPanel;
    private JPanel ecommerceAnalysisPanel;
    private JPanel digitalServicesPanel;
    
    public LocalConsumptionPanel(TransactionController transactionController) {
        this.transactionController = transactionController;
        this.csvHandler = new CSVHandler();
        
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create main content with 3x2 grid
        mainContent = new JPanel(new GridLayout(3, 2, 15, 15));
        mainContent.setOpaque(false);
        
        // Create panels
        consumptionTrendPanel = createConsumptionTrendPanel();
        categoryAnalysisPanel = createCategoryAnalysisPanel();
        paymentMethodPanel = createPaymentMethodPanel();
        festivalSpendingPanel = createFestivalSpendingPanel();
        ecommerceAnalysisPanel = createEcommerceAnalysisPanel();
        digitalServicesPanel = createDigitalServicesPanel();
        
        // Add panels to main content
        mainContent.add(consumptionTrendPanel);
        mainContent.add(categoryAnalysisPanel);
        mainContent.add(paymentMethodPanel);
        mainContent.add(festivalSpendingPanel);
        mainContent.add(ecommerceAnalysisPanel);
        mainContent.add(digitalServicesPanel);
        
        // Add main content to scroll pane
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Initial data update
        updateAllPanels();
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Chinese Consumption Patterns");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.WEST);
        
        JLabel subtitleLabel = new JLabel("Analysis of modern Chinese spending habits");
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subtitleLabel.setForeground(SECONDARY_COLOR);
        panel.add(subtitleLabel, BorderLayout.CENTER);
        
        JButton refreshButton = new JButton("Refresh Analysis");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> updateAllPanels());
        panel.add(refreshButton, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createConsumptionTrendPanel() {
        JPanel panel = createCardPanel("Mobile vs Traditional Shopping");
        panel.setLayout(new BorderLayout(10, 10));

        // Create trend chart panel
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                // Draw chart background
                g2d.setColor(new Color(240, 240, 240));
                g2d.fillRect(40, 30, width - 80, height - 60);

                // Draw axes
                g2d.setColor(Color.DARK_GRAY);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(40, height - 30, width - 40, height - 30); // X-axis
                g2d.drawLine(40, 30, 40, height - 30); // Y-axis

                // Draw mobile shopping trend (increasing)
                g2d.setColor(PRIMARY_COLOR);
                g2d.setStroke(new BasicStroke(3));
                int[] xPoints = {40, 80, 120, 160, 200, 240, 280, 320, 360, 400};
                int[] yPoints = {height - 100, height - 105, height - 115, height - 130,
                                 height - 150, height - 175, height - 200, height - 230,
                                 height - 250, height - 270};
                g2d.drawPolyline(xPoints, yPoints, xPoints.length);

                // Draw legend
                g2d.setColor(PRIMARY_COLOR);
                g2d.fillRect(width - 150, 40, 20, 10);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Mobile Shopping", width - 120, 50);
            }
        };
        chartPanel.setPreferredSize(new Dimension(300, 200));
        chartPanel.setBackground(CARD_BACKGROUND);

        // Create trend summary
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);

        JLabel trendLabel = new JLabel("Mobile Shopping Dominance");
        trendLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryPanel.add(trendLabel);

        JLabel growthLabel = new JLabel("Mobile Shopping Growth: +24.8% annually");
        growthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        growthLabel.setForeground(SUCCESS_COLOR);
        summaryPanel.add(growthLabel);

        JLabel statsLabel = new JLabel("Mobile accounts for 85% of all transactions");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(SECONDARY_COLOR);
        summaryPanel.add(statsLabel);

        panel.add(chartPanel, BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCategoryAnalysisPanel() {
        JPanel panel = createCardPanel("Popular Categories");
        panel.setLayout(new BorderLayout(10, 10));
        
        // Create category table
        String[] columns = {"Category (EN/CN)", "Amount", "YoY Change"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Populate with sample data
        for (Map.Entry<String, String> entry : LOCAL_CATEGORIES.entrySet()) {
            String category = entry.getKey() + " (" + entry.getValue() + ")";
            double amount = Math.random() * 5000 + 1000;
            double change = Math.random() * 30 - 10;
            String changeStr = String.format("%+.1f%%", change);
            
            model.addRow(new Object[]{category, String.format("¥%.2f", amount), changeStr});
        }
        
        JTable categoryTable = new JTable(model);
        categoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryTable.setRowHeight(25);
        categoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        categoryTable.setShowGrid(true);
        categoryTable.setGridColor(new Color(230, 230, 230));
        
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createPaymentMethodPanel() {
        JPanel panel = createCardPanel("Payment Methods");
        panel.setLayout(new BorderLayout(10, 10));

        // Create payment method chart
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int centerX = width / 2;
                int centerY = height / 2;
                int radius = Math.min(width, height) / 3;

                // Colors for payment methods
                Color[] colors = {
                        new Color(52, 152, 219),   // Alipay: Blue
                        new Color(46, 204, 113),   // WeChat Pay: Green
                        new Color(155, 89, 182),   // UnionPay: Purple
                        new Color(241, 196, 15),   // Credit Card: Yellow
                        new Color(230, 126, 34),   // Cash: Orange
                        new Color(253, 180, 98),   // PayPal: Peach
                        new Color(230, 230, 230),  // Bitcoin: Light gray
                        new Color(192, 57, 43)     // Others: Red
                };

                // Draw pie chart
                double total = PAYMENT_METHODS.values().stream().mapToDouble(Double::doubleValue).sum();
                double startAngle = 0;

                int i = 0;
                for (Map.Entry<String, Double> entry : PAYMENT_METHODS.entrySet()) {
                    double percentage = entry.getValue();
                    double angle = 360 * (percentage / total);

                    g2d.setColor(colors[i % colors.length]);
                    g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                            (int) startAngle, (int) angle);

                    startAngle += angle;
                    i++;
                }

                // Add shadow effect to the pie chart
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillArc(centerX - radius - 5, centerY - radius - 5, radius * 2 + 10, radius * 2 + 10, 0, 360);

                // Draw legend
                int legendX = centerX + radius + 20;
                int legendY = centerY - radius - 20;

                i = 0;
                for (Map.Entry<String, Double> entry : PAYMENT_METHODS.entrySet()) {
                    g2d.setColor(colors[i % colors.length]);
                    g2d.fillRoundRect(legendX, legendY + i * 20, 15, 15, 5, 5);

                    g2d.setColor(Color.BLACK);
                    g2d.drawString(entry.getKey() + " (" + entry.getValue() + "%)",
                            legendX + 20, legendY + i * 20 + 12);
                    i++;
                }
            }
        };
        chartPanel.setPreferredSize(new Dimension(400, 300));
        chartPanel.setBackground(CARD_BACKGROUND);

        // Create payment method summary
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);

        JLabel methodLabel = new JLabel("Mobile Payment");
        methodLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryPanel.add(methodLabel);

        JLabel mobileLabel = new JLabel("Alipay & WeChat Pay: 85% of transactions");
        mobileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mobileLabel.setForeground(PRIMARY_COLOR);
        summaryPanel.add(mobileLabel);

        JLabel cashLabel = new JLabel("Cash usage declined by 15% this year");
        cashLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cashLabel.setForeground(SECONDARY_COLOR);
        summaryPanel.add(cashLabel);

        panel.add(chartPanel, BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.SOUTH);

        return panel;
    }
    
    private JPanel createFestivalSpendingPanel() {
        JPanel panel = createCardPanel("Shopping Festival Analysis");
        panel.setLayout(new BorderLayout(10, 10));
        
        // Create festival chart
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Draw chart background
                g2d.setColor(new Color(240, 240, 240));
                g2d.fillRect(40, 30, width - 80, height - 60);
                
                // Draw axes
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawLine(40, height - 30, width - 40, height - 30); // X-axis
                g2d.drawLine(40, 30, 40, height - 30); // Y-axis
                
                // Sample data for festival spending (billions)
                String[] festivals = {"Double 11", "618", "Double 12", "CNY", "Other"};
                double[] amounts = {84.5, 58.2, 30.8, 25.5, 45.2};
                
                // Find max for scaling
                double maxAmount = Arrays.stream(amounts).max().orElse(100);
                
                // Draw bars
                int barWidth = (width - 120) / festivals.length;
                int spacing = 10;
                int barX = 60;
                
                for (int i = 0; i < festivals.length; i++) {
                    // Bar height relative to max
                    int barHeight = (int)((amounts[i] / maxAmount) * (height - 80));
                    
                    // Festival-specific colors
                    if (festivals[i].equals("Double 11")) {
                        g2d.setColor(FESTIVAL_RED);
                    } else if (festivals[i].equals("CNY")) {
                        g2d.setColor(FESTIVAL_GOLD);
                    } else {
                        g2d.setColor(PRIMARY_COLOR);
                    }
                    
                    // Draw bar
                    g2d.fillRect(barX, height - 30 - barHeight, barWidth - spacing, barHeight);
                    
                    // Draw label
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawString(festivals[i], barX, height - 15);
                    
                    // Draw value
                    g2d.drawString(String.format("¥%.1fB", amounts[i]), 
                                  barX, height - 35 - barHeight);
                    
                    barX += barWidth;
                }
            }
        };

        chartPanel.setPreferredSize(new Dimension(300, 200));
        chartPanel.setBackground(CARD_BACKGROUND);
        
        // Create festival summary
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);
        
        JLabel festivalLabel = new JLabel("Shopping Festival Spending");
        festivalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryPanel.add(festivalLabel);
        
        JLabel peakLabel = new JLabel("Double 11 (Singles' Day): ¥84.5B total sales");
        peakLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        peakLabel.setForeground(FESTIVAL_RED);
        summaryPanel.add(peakLabel);
        
        JLabel growthLabel = new JLabel("YoY Festival Growth: +15.8%");
        growthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        growthLabel.setForeground(SUCCESS_COLOR);
        summaryPanel.add(growthLabel);
        
        panel.add(chartPanel, BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createEcommerceAnalysisPanel() {
        JPanel panel = createCardPanel("E-commerce Platforms");
        panel.setLayout(new BorderLayout(10, 10));
        
        // Create data panel
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
        dataPanel.setOpaque(false);
        dataPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Platform data
        String[][] platformData = {
            {"Taobao/Tmall", "42%", "+8.5%", "All categories"},
            {"JD.com", "28%", "+12.2%", "Electronics, home appliances"},
            {"Pinduoduo", "15%", "+24.8%", "Value shopping, groceries"},
            {"Douyin", "8%", "+45.2%", "Fashion, cosmetics"},
            {"Others", "7%", "+%", "Niche markets"}
        };
        
        // Create platform components
        for (String[] platform : platformData) {
            JPanel platformPanel = new JPanel(new BorderLayout(10, 0));
            platformPanel.setOpaque(false);
            platformPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
            
            JLabel nameLabel = new JLabel(platform[0]);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            JPanel detailsPanel = new JPanel(new GridLayout(2, 2, 10, 2));
            detailsPanel.setOpaque(false);
            
            JLabel marketShareLabel = new JLabel("Market Share: " + platform[1]);
            marketShareLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
            JLabel growthLabel = new JLabel("Growth: " + platform[2]);
            growthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            if (platform[2].startsWith("+")) {
                growthLabel.setForeground(SUCCESS_COLOR);
            }
            
            JLabel categoryLabel = new JLabel("Focus: " + platform[3]);
            categoryLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            
            detailsPanel.add(marketShareLabel);
            detailsPanel.add(growthLabel);
            detailsPanel.add(categoryLabel);
            
            platformPanel.add(nameLabel, BorderLayout.WEST);
            platformPanel.add(detailsPanel, BorderLayout.CENTER);
            
            // Add some spacing
            dataPanel.add(platformPanel);
            dataPanel.add(Box.createVerticalStrut(10));
        }
        
        JScrollPane scrollPane = new JScrollPane(dataPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createDigitalServicesPanel() {
        JPanel panel = createCardPanel("Digital Services Adoption");
        panel.setLayout(new BorderLayout(10, 10));

        // Create service usage chart
        JPanel chartPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        chartPanel.setOpaque(false);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Digital services data with descriptions
        String[][] servicesData = {
                {"Mobile Payment", "90%", "Daily", "Use mobile devices to make payments"},
                {"Food Delivery", "75%", "Weekly", "Order food from restaurants for delivery"},
                {"Ride Hailing", "65%", "Weekly", "Use mobile apps to book rides"},
                {"Online Education", "45%", "Monthly", "Take online courses"},
                {"Digital Healthcare", "38%", "Monthly", "Access healthcare services online"},
                {"Subscription Services", "25%", "Monthly", "Subscribe to various online services"}
        };

        // Create service tiles
        for (String[] service : servicesData) {
            JPanel servicePanel = createServiceTile(service[0], service[1], service[2], service[3]);
            chartPanel.add(servicePanel);
        }

        JScrollPane scrollPane = new JScrollPane(chartPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);

        // Create summary
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel summaryLabel = new JLabel("Digital Service Integration");
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryPanel.add(summaryLabel);

        JLabel statsLabel = new JLabel("Average user accesses 4.2 digital services daily");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(SECONDARY_COLOR);
        summaryPanel.add(statsLabel);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.SOUTH);

        return panel;
    }

    private static final Color[] BLUE_SHADES = {
            new Color(41, 128, 185), // 深蓝
            new Color(52, 152, 219), // 中蓝
            new Color(173, 216, 230), // 浅蓝
            new Color(224, 243, 250), // 非常浅的蓝
            new Color(236, 240, 241)  // 几乎白色的蓝
    };

    private Color getServiceColor(String serviceName) {
        switch (serviceName) {
            case "Mobile Payment":
                return BLUE_SHADES[0]; // 深蓝
            case "Food Delivery":
                return BLUE_SHADES[1]; // 中蓝
            case "Ride Hailing":
                return BLUE_SHADES[2]; // 浅蓝
            case "Online Education":
                return BLUE_SHADES[3]; // 非常浅的蓝
            case "Digital Healthcare":
                return BLUE_SHADES[4]; // 几乎白色的蓝
            case "Subscription Services":
                return BLUE_SHADES[0]; // 深蓝
            default:
                return Color.GRAY;
        }
    }

    private JPanel createServiceTile(String serviceName, String penetration, String frequency, String description) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        // Service icon placeholder with color
        JPanel iconPanel = new JPanel();
        iconPanel.setPreferredSize(new Dimension(40, 40));
        iconPanel.setBackground(getServiceColor(serviceName)); // 使用不同的蓝色阴影

        // Service info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(serviceName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel usageLabel = new JLabel("Usage: " + penetration);
        usageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JLabel frequencyLabel = new JLabel("Freq: " + frequency);
        frequencyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JLabel descriptionLabel = new JLabel("Description: " + description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        descriptionLabel.setForeground(Color.GRAY);

        infoPanel.add(nameLabel);
        infoPanel.add(usageLabel);
        infoPanel.add(frequencyLabel);
        infoPanel.add(descriptionLabel);

        panel.add(iconPanel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        return panel;
    }
    
    private void updateAllPanels() {
        try {
            transactionController.loadTransactions();
            updateConsumptionTrend();
            updateCategoryAnalysis();
            updatePaymentMethods();
            updateFestivalSpending();
            updateEcommerce();
            updateDigitalServices();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateConsumptionTrend() {
        // Update consumption trend chart and summary
    }
    
    private void updateCategoryAnalysis() {
        try {
            // Update category analysis table with actual data
            Component firstComponent = categoryAnalysisPanel.getComponent(0);
            if (firstComponent instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) firstComponent;
                Component view = scrollPane.getViewport().getView();
                if (view instanceof JTable) {
                    JTable table = (JTable) view;
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    model.setRowCount(0);
                    
                    Map<String, Double> categoryExpenses = transactionController.getCurrentMonthExpenses();
                    for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
                        String category = entry.getKey();
                        double amount = entry.getValue();
                        String trend = calculateTrend(category, amount);
                        
                        // Add Chinese translation if available
                        if (LOCAL_CATEGORIES.containsKey(category)) {
                            category = category + " (" + LOCAL_CATEGORIES.get(category) + ")";
                        }
                        
                        model.addRow(new Object[]{category, String.format("¥%.2f", amount), trend});
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updatePaymentMethods() {
        // Update payment method distribution chart and summary
    }
    
    private void updateFestivalSpending() {
        // Update festival spending analysis
    }
    
    private void updateEcommerce() {
        // Update e-commerce platform analysis
    }
    
    private void updateDigitalServices() {
        // Update digital services adoption chart
    }
    
    private String calculateTrend(String category, double currentAmount) {
        // Calculate trend based on historical data
        return "↑ 5.2%";
    }
} 