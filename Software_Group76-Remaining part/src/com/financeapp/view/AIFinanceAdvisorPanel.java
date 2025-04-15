package com.financeapp.view;

import com.financeapp.controller.TransactionController;
import com.financeapp.model.Transaction;
import com.financeapp.service.AIFinanceAdvisorService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * AI Finance Advisor Panel
 * Displays AI-generated monthly financial analysis and next month's recommendations
 */
public class AIFinanceAdvisorPanel extends JPanel {
    
    private final TransactionController controller;
    
    // UI Components
    private JPanel contentPanel;
    private JPanel analysisPanel;
    private JPanel recommendationsPanel;
    private JButton generateButton;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JLabel lastUpdatedLabel;
    
    // 新增: AI服务
    private AIFinanceAdvisorService aiService;
    private String apiKey;
    private JPasswordField apiKeyField;
    private JButton saveApiKeyButton;
    private ExecutorService executorService;
    
    // 新增: API设置相关
    private String apiUrl;
    private String model;
    private JTextField apiUrlField;
    private JTextField modelField;
    
    // Colors (matching app theme)
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color HIGHLIGHT_COLOR = new Color(243, 156, 18);
    private static final Color LIGHT_ACCENT = new Color(214, 234, 248);
    
    /**
     * Constructor
     * @param controller Transaction controller
     */
    public AIFinanceAdvisorPanel(TransactionController controller) {
        this.controller = controller;
        this.executorService = Executors.newSingleThreadExecutor();
        
        // 初始化API配置
        this.apiUrl = "https://api.openai.com/v1/chat/completions";
        this.model = "gpt-3.5-turbo";
        
        initUI();
        
        // 尝试加载示例分析或生成新分析
        generateSampleAnalysis();
    }
    
    /**
     * Initialize UI
     */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Content Panel (Center)
        contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setOpaque(false);
        
        // Analysis Panel (Left)
        analysisPanel = createAnalysisPanel();
        contentPanel.add(analysisPanel);
        
        // Recommendations Panel (Right)
        recommendationsPanel = createRecommendationsPanel();
        contentPanel.add(recommendationsPanel);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Footer Panel
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
        
        // 新增: 添加 API Key 设置面板
        JPanel apiKeyPanel = createApiKeyPanel();
        add(apiKeyPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create header panel
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        
        // Title
        JLabel titleLabel = new JLabel("AI Financial Advisor");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setIcon(createRobotIcon());
        panel.add(titleLabel, BorderLayout.WEST);
        
        // Status and actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        
        LocalDate currentDate = LocalDate.now();
        String monthName = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        
        lastUpdatedLabel = new JLabel("Last updated: " + currentDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        lastUpdatedLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lastUpdatedLabel.setForeground(TEXT_COLOR);
        actionPanel.add(lastUpdatedLabel);
        
        generateButton = createPrimaryButton("Generate New Analysis");
        generateButton.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        generateButton.addActionListener(e -> generateAnalysis());
        actionPanel.add(generateButton);
        
        panel.add(actionPanel, BorderLayout.EAST);
        
        // Description
        JLabel descLabel = new JLabel("<html>Get personalized financial insights and recommendations based on your transaction history and spending patterns.</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(TEXT_COLOR);
        panel.add(descLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create analysis panel
     */
    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(createPanelBorder("Current Month Analysis", PRIMARY_COLOR));
        
        // Month header
        LocalDate currentDate = LocalDate.now();
        String monthName = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        JLabel monthLabel = new JLabel(monthName + " " + currentDate.getYear(), SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        monthLabel.setForeground(Color.WHITE);
        monthLabel.setOpaque(true);
        monthLabel.setBackground(PRIMARY_COLOR);
        monthLabel.setBorder(new EmptyBorder(8, 15, 8, 15));
        
        panel.add(monthLabel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // Empty placeholder - will be filled by generateSampleAnalysis()
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create recommendations panel
     */
    private JPanel createRecommendationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(createPanelBorder("Next Month Recommendations", SECONDARY_COLOR));
        
        // Month header
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        String monthName = nextMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        JLabel monthLabel = new JLabel(monthName + " " + nextMonth.getYear(), SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        monthLabel.setForeground(Color.WHITE);
        monthLabel.setOpaque(true);
        monthLabel.setBackground(SECONDARY_COLOR);
        monthLabel.setBorder(new EmptyBorder(8, 15, 8, 15));
        
        panel.add(monthLabel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // Empty placeholder - will be filled by generateSampleAnalysis()
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create footer panel
     */
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setForeground(PRIMARY_COLOR);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setValue(100);
        progressBar.setVisible(false);
        panel.add(progressBar, BorderLayout.CENTER);
        
        // Status
        statusLabel = new JLabel("Ready to generate financial insights");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(TEXT_COLOR);
        panel.add(statusLabel, BorderLayout.WEST);
        
        // Disclaimer
        JLabel disclaimerLabel = new JLabel("Advice is based on historical data. Not financial advice.");
        disclaimerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        disclaimerLabel.setForeground(TEXT_COLOR.brighter());
        panel.add(disclaimerLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Create panel border with title
     */
    private Border createPanelBorder(String title, Color color) {
        return new CompoundBorder(
                new LineBorder(color, 2, true),
                new CompoundBorder(
                        new EmptyBorder(0, 0, 0, 0),
                        new TitledBorder(
                                new EmptyBorder(0, 15, 0, 15),
                                title,
                                TitledBorder.DEFAULT_JUSTIFICATION,
                                TitledBorder.DEFAULT_POSITION,
                                new Font("Segoe UI", Font.BOLD, 14),
                                color
                        )
                )
        );
    }
    
    /**
     * Create a primary styled button
     */
    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(PRIMARY_COLOR.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        
        return button;
    }
    
    /**
     * Create card component for insights
     */
    private JPanel createInsightCard(String title, String content, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(accentColor, 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(accentColor);
        card.add(titleLabel, BorderLayout.NORTH);
        
        // Content
        JLabel contentLabel = new JLabel("<html>" + content + "</html>");
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        contentLabel.setForeground(TEXT_COLOR);
        card.add(contentLabel, BorderLayout.CENTER);
        
        // Make it maintain some space
        card.setPreferredSize(new Dimension(400, 120));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 120));
        
        return card;
    }
    
    /**
     * Create recommendation card
     */
    private JPanel createRecommendationCard(String title, String content, Color accentColor, boolean isHighPriority) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(15, 10));
        card.setBackground(isHighPriority ? new Color(255, 250, 240) : Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(accentColor, isHighPriority ? 2 : 1, true),
                new EmptyBorder(12, 15, 12, 15)
        ));
        
        // Priority indicator
        if (isHighPriority) {
            JPanel priorityIndicator = new JPanel();
            priorityIndicator.setBackground(HIGHLIGHT_COLOR);
            priorityIndicator.setPreferredSize(new Dimension(5, 0));
            card.add(priorityIndicator, BorderLayout.WEST);
        }
        
        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(5, 8));
        contentPanel.setOpaque(false);
        
        // Title with icon
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(isHighPriority ? HIGHLIGHT_COLOR : accentColor);
        if (isHighPriority) {
            titleLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
        }
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Content
        JLabel contentLabel = new JLabel("<html>" + content + "</html>");
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        contentLabel.setForeground(TEXT_COLOR);
        contentPanel.add(contentLabel, BorderLayout.CENTER);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        // Make it maintain some space
        card.setPreferredSize(new Dimension(400, 130));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 130));
        
        return card;
    }
    
    /**
     * Create robot icon for header
     */
    private Icon createRobotIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Robot head
                g2d.setColor(PRIMARY_COLOR);
                g2d.fillRoundRect(x, y, 30, 30, 8, 8);
                
                // Antenna
                g2d.setColor(SECONDARY_COLOR);
                g2d.fillRect(x + 13, y - 5, 4, 6);
                g2d.setColor(HIGHLIGHT_COLOR);
                g2d.fillOval(x + 12, y - 9, 6, 6);
                
                // Eyes
                g2d.setColor(Color.WHITE);
                g2d.fillOval(x + 7, y + 10, 6, 6);
                g2d.fillOval(x + 18, y + 10, 6, 6);
                
                // Mouth
                g2d.setColor(LIGHT_ACCENT);
                g2d.fillRoundRect(x + 8, y + 20, 14, 3, 2, 2);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 36;
            }
            
            @Override
            public int getIconHeight() {
                return 36;
            }
        };
    }
    
    /**
     * Create API Key panel
     */
    private JPanel createApiKeyPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // API Key 面板
        JPanel apiKeyRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        apiKeyRow.setOpaque(false);
        
        JLabel apiKeyLabel = new JLabel("OpenAI API Key:");
        apiKeyLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        apiKeyLabel.setForeground(TEXT_COLOR);
        apiKeyRow.add(apiKeyLabel);
        
        apiKeyField = new JPasswordField(30);
        apiKeyField.setEchoChar('*');
        apiKeyRow.add(apiKeyField);
        
        panel.add(apiKeyRow);
        
        // API URL 面板
        JPanel apiUrlRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        apiUrlRow.setOpaque(false);
        
        JLabel apiUrlLabel = new JLabel("API URL:");
        apiUrlLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        apiUrlLabel.setForeground(TEXT_COLOR);
        apiUrlRow.add(apiUrlLabel);
        
        apiUrlField = new JTextField(30);
        apiUrlField.setText("https://api.openai.com/v1/chat/completions");
        apiUrlRow.add(apiUrlField);
        
        panel.add(apiUrlRow);
        
        // 模型面板
        JPanel modelRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modelRow.setOpaque(false);
        
        JLabel modelLabel = new JLabel("Model Name:");
        modelLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        modelLabel.setForeground(TEXT_COLOR);
        modelRow.add(modelLabel);
        
        modelField = new JTextField(15);
        modelField.setText("gpt-3.5-turbo");
        modelRow.add(modelField);
        
        panel.add(modelRow);
        
        // 保存按钮面板
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonRow.setOpaque(false);
        
        saveApiKeyButton = new JButton("Save Settings");
        saveApiKeyButton.addActionListener(e -> {
            apiKey = new String(apiKeyField.getPassword()).trim();
            apiUrl = apiUrlField.getText().trim();
            model = modelField.getText().trim();
            
            if (aiService == null) {
                aiService = new AIFinanceAdvisorService(apiKey, apiUrl, model);
            } else {
                aiService.setApiKey(apiKey);
                aiService.setApiUrl(apiUrl);
                aiService.setModel(model);
            }
            
            JOptionPane.showMessageDialog(this, 
                "API settings saved successfully!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        buttonRow.add(saveApiKeyButton);
        
        panel.add(buttonRow);
        
        return panel;
    }
    
    /**
     * 处理 AI 分析生成
     */
    private void generateAnalysis() {
        if (apiKey == null || apiKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter your API key first.", 
                "API Key Required", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 显示进度条
        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setString("Starting analysis...");
        statusLabel.setText("Preparing data...");
        generateButton.setEnabled(false);
        
        executorService.submit(() -> {
            try {
                // 更新进度
                updateProgress(20, "Loading CSV data...");
                
                // 创建AI服务
                if (aiService == null) {
                    aiService = new AIFinanceAdvisorService(apiKey, apiUrl, model);
                }
                
                // 加载CSV文件数据
                List<Transaction> transactions = loadTransactionsFromCSV();
                if (transactions.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setVisible(false);
                        statusLabel.setText("No transaction data found.");
                        generateButton.setEnabled(true);
                        JOptionPane.showMessageDialog(this, 
                            "No transaction data found in CSV files.", 
                            "Data Error", 
                            JOptionPane.ERROR_MESSAGE);
                    });
                    return;
                }
                
                updateProgress(40, "Analyzing transaction data...");
                
                // 获取分析结果
                Map<String, Object> analysis = aiService.analyzeCurrentMonth(transactions);
                
                updateProgress(60, "Generating AI recommendations...");
                
                // 获取AI建议
                String aiRecommendations = aiService.getAIRecommendations(transactions);
                
                updateProgress(80, "Updating UI...");
                
                // 更新UI
                SwingUtilities.invokeLater(() -> {
                    displayAnalysisResults(analysis);
                    displayAIRecommendations(aiRecommendations);
                    lastUpdatedLabel.setText("Last updated: " + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
                });
                
                updateProgress(100, "Analysis complete!");
                
                // 完成后
                SwingUtilities.invokeLater(() -> {
                    progressBar.setString("Complete");
                    statusLabel.setText("Analysis complete!");
                    generateButton.setEnabled(true);
                    
                    // 3秒后隐藏进度条
                    Timer timer = new Timer(3000, evt -> progressBar.setVisible(false));
                    timer.setRepeats(false);
                    timer.start();
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(false);
                    statusLabel.setText("Error: " + e.getMessage());
                    generateButton.setEnabled(true);
                    JOptionPane.showMessageDialog(this, 
                        "Error generating analysis: " + e.getMessage(), 
                        "Analysis Error", 
                        JOptionPane.ERROR_MESSAGE);
                });
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 从 data 目录加载 CSV 交易数据
     */
    private List<Transaction> loadTransactionsFromCSV() throws IOException {
        // 确定要使用的CSV文件
        File dataDir = new File("data");
        File csvFile = new File(dataDir, "transactions.csv");
        
        if (!csvFile.exists()) {
            throw new IOException("Transaction CSV file not found: " + csvFile.getAbsolutePath());
        }
        
        return aiService.loadTransactionsFromCSV(csvFile.getAbsolutePath());
    }
    
    /**
     * 更新进度条
     */
    private void updateProgress(int value, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            progressBar.setString(message);
            statusLabel.setText(message);
        });
    }
    
    /**
     * 显示分析结果
     */
    private void displayAnalysisResults(Map<String, Object> analysis) {
        // 清除旧内容
        Component[] components = analysisPanel.getComponents();
        JScrollPane scrollPane = null;
        
        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                scrollPane = (JScrollPane) comp;
                break;
            }
        }
        
        if (scrollPane != null) {
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);
            contentPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
            
            // 添加总收入和支出
            double totalIncome = (Double) analysis.get("totalIncome");
            double totalExpense = (Double) analysis.get("totalExpense");
            double balance = (Double) analysis.get("balance");
            
            contentPanel.add(createInsightCard("Total Income", 
                    String.format("$%.2f", totalIncome), 
                    SUCCESS_COLOR));
            
            contentPanel.add(Box.createVerticalStrut(10));
            
            contentPanel.add(createInsightCard("Total Expenses", 
                    String.format("$%.2f", totalExpense), 
                    ERROR_COLOR));
            
            contentPanel.add(Box.createVerticalStrut(10));
            
            contentPanel.add(createInsightCard("Balance", 
                    String.format("$%.2f", balance), 
                    balance >= 0 ? SUCCESS_COLOR : ERROR_COLOR));
            
            contentPanel.add(Box.createVerticalStrut(15));
            
            // 添加顶级支出类别
            String topCategory = (String) analysis.get("topExpenseCategory");
            double topAmount = (Double) analysis.get("topExpenseAmount");
            
            contentPanel.add(createInsightCard("Top Expense Category", 
                    topCategory + " - $" + String.format("%.2f", topAmount), 
                    SECONDARY_COLOR));
            
            contentPanel.add(Box.createVerticalStrut(15));
            
            // 添加类别支出详情
            JLabel categoryTitle = new JLabel("Expense Breakdown");
            categoryTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            categoryTitle.setForeground(PRIMARY_COLOR);
            categoryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(categoryTitle);
            
            contentPanel.add(Box.createVerticalStrut(5));
            
            @SuppressWarnings("unchecked")
            Map<String, Double> categoryExpenses = (Map<String, Double>) analysis.get("categoryExpenses");
            
            if (categoryExpenses != null) {
                categoryExpenses.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // 降序排序
                    .forEach(entry -> {
                        JPanel categoryPanel = new JPanel(new BorderLayout(10, 0));
                        categoryPanel.setBackground(LIGHT_ACCENT);
                        categoryPanel.setBorder(new EmptyBorder(5, 8, 5, 8));
                        categoryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                        categoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        
                        JLabel nameLabel = new JLabel(entry.getKey());
                        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        categoryPanel.add(nameLabel, BorderLayout.WEST);
                        
                        JLabel amountLabel = new JLabel("$" + String.format("%.2f", entry.getValue()));
                        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        categoryPanel.add(amountLabel, BorderLayout.EAST);
                        
                        contentPanel.add(categoryPanel);
                        contentPanel.add(Box.createVerticalStrut(2));
                    });
            }
            
            scrollPane.setViewportView(contentPanel);
        }
    }
    
    /**
     * 显示AI生成的建议
     */
    private void displayAIRecommendations(String recommendations) {
        // 清除旧内容
        Component[] components = recommendationsPanel.getComponents();
        JScrollPane scrollPane = null;
        
        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                scrollPane = (JScrollPane) comp;
                break;
            }
        }
        
        if (scrollPane != null) {
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setOpaque(false);
            contentPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
            
            // 拆分建议为段落
            String[] paragraphs = recommendations.split("\n\n");
            int index = 0;
            
            for (String paragraph : paragraphs) {
                // 跳过空段落
                if (paragraph.trim().isEmpty()) {
                    continue;
                }
                
                // 根据段落内容判断是否为标题
                boolean isHeader = paragraph.trim().length() < 50 && 
                                  (paragraph.contains("SUMMARY") || 
                                   paragraph.contains("RECOMMENDATIONS") || 
                                   paragraph.contains("INSIGHTS") || 
                                   paragraph.contains("NEXT STEPS") ||
                                   paragraph.contains("CONCERNS") ||
                                   paragraph.toUpperCase().equals(paragraph));
                
                if (isHeader) {
                    // 添加标题
                    JLabel headerLabel = new JLabel(paragraph.trim());
                    headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    headerLabel.setForeground(PRIMARY_COLOR);
                    headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    headerLabel.setBorder(new EmptyBorder(10, 0, 5, 0));
                    contentPanel.add(headerLabel);
                } else {
                    // 根据位置判断重要性
                    boolean isHighPriority = paragraph.contains("recommend") || 
                                           paragraph.contains("important") || 
                                           paragraph.contains("critical") ||
                                           paragraph.contains("immediate");
                    
                    // 使用不同颜色标识不同重要级别
                    Color accentColor = (index % 3 == 0) ? PRIMARY_COLOR : 
                                     ((index % 3 == 1) ? SECONDARY_COLOR : HIGHLIGHT_COLOR);
                    
                    // 添加建议卡片
                    JPanel card = createRecommendationCard(
                        null, paragraph.trim(), accentColor, isHighPriority);
                    card.setAlignmentX(Component.LEFT_ALIGNMENT);
                    contentPanel.add(card);
                    contentPanel.add(Box.createVerticalStrut(10));
                }
                
                index++;
            }
            
            scrollPane.setViewportView(contentPanel);
        }
    }
    
    /**
     * Generate sample analysis for demonstration
     */
    private void generateSampleAnalysis() {
        // Clear existing content
        analysisPanel.removeAll();
        recommendationsPanel.removeAll();
        
        // Re-create structure
        JPanel analysisContent = new JPanel();
        analysisContent.setLayout(new BoxLayout(analysisContent, BoxLayout.Y_AXIS));
        analysisContent.setOpaque(false);
        analysisContent.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JPanel recommendationsContent = new JPanel();
        recommendationsContent.setLayout(new BoxLayout(recommendationsContent, BoxLayout.Y_AXIS));
        recommendationsContent.setOpaque(false);
        recommendationsContent.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // === CURRENT MONTH ANALYSIS ===
        
        // Spending summary
        JPanel summaryPanel = createInsightCard(
                "Monthly Spending Summary",
                "Your total spending this month is <b>$2,350.75</b>, which is <font color='#e74c3c'><b>12.5% higher</b></font> " +
                "than your monthly average of $2,089.42.",
                PRIMARY_COLOR
        );
        analysisContent.add(summaryPanel);
        analysisContent.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Top spending categories
        JPanel topCategoriesPanel = createInsightCard(
                "Top Spending Categories",
                "Your highest spending categories this month:<br>" +
                "1. <b>Housing</b>: $950.00 (40.4%)<br>" +
                "2. <b>Food</b>: $435.25 (18.5%)<br>" +
                "3. <b>Transportation</b>: $325.50 (13.8%)",
                SECONDARY_COLOR
        );
        analysisContent.add(topCategoriesPanel);
        analysisContent.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Unusual spending
        JPanel unusualPanel = createInsightCard(
                "Unusual Spending Detected",
                "Your <b>Entertainment</b> spending of $220.35 is <font color='#e74c3c'><b>85% higher</b></font> " +
                "than your usual monthly average of $119.25 in this category.",
                ERROR_COLOR
        );
        analysisContent.add(unusualPanel);
        analysisContent.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Savings analysis
        JPanel savingsPanel = createInsightCard(
                "Savings Analysis",
                "You saved <b>$450.00</b> this month, achieving <font color='#27ae60'><b>90%</b></font> " +
                "of your monthly savings goal of $500.",
                SUCCESS_COLOR
        );
        analysisContent.add(savingsPanel);
        analysisContent.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Recurring expenses
        JPanel recurringPanel = createInsightCard(
                "Recurring Expenses",
                "You have <b>8 recurring expenses</b> totaling <b>$1,245.75</b> this month, " +
                "accounting for <b>53%</b> of your total spending.",
                PRIMARY_COLOR.darker()
        );
        analysisContent.add(recurringPanel);
        
        // === NEXT MONTH RECOMMENDATIONS ===
        
        // Budget adjustment (high priority)
        JPanel budgetPanel = createRecommendationCard(
                "Adjust Your Food Budget",
                "Consider reducing your <b>Food</b> expenses by <b>15%</b> next month. " +
                "You've consistently exceeded your budget in this category for the past 3 months. " +
                "Try meal planning and reducing dining out to 2 times per week.",
                ERROR_COLOR,
                true
        );
        recommendationsContent.add(budgetPanel);
        recommendationsContent.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Savings opportunity
        JPanel savingsRecPanel = createRecommendationCard(
                "Increase Emergency Fund",
                "You're <b>$2,500</b> away from your 3-month emergency fund goal. " +
                "Try to add an extra <b>$200</b> to your savings next month to stay on track.",
                SUCCESS_COLOR,
                false
        );
        recommendationsContent.add(savingsRecPanel);
        recommendationsContent.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Upcoming expenses
        JPanel upcomingPanel = createRecommendationCard(
                "Prepare for Upcoming Expenses",
                "Based on your spending patterns, you typically have higher <b>Utilities</b> expenses in the " +
                "coming month. Budget an extra <b>$75-$100</b> for this category.",
                SECONDARY_COLOR,
                false
        );
        recommendationsContent.add(upcomingPanel);
        recommendationsContent.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Subscription review
        JPanel subscriptionPanel = createRecommendationCard(
                "Review Unused Subscriptions",
                "We've identified <b>3 subscription services</b> ($45.97 monthly) with little to no usage " +
                "in the past 2 months. Consider canceling: <b>Premium Music</b>, <b>MovieFlix</b>, <b>NewsDaily</b>.",
                HIGHLIGHT_COLOR,
                true
        );
        recommendationsContent.add(subscriptionPanel);
        recommendationsContent.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Saving opportunity
        JPanel opportunityPanel = createRecommendationCard(
                "Shopping Optimization",
                "Your <b>Groceries</b> spending is 22% higher than similar households. " +
                "Try shopping at discount stores and using a grocery list to save approximately <b>$85</b> next month.",
                PRIMARY_COLOR,
                false
        );
        recommendationsContent.add(opportunityPanel);
        
        // Add scrollable content to panels
        JScrollPane analysisScroll = new JScrollPane(analysisContent);
        analysisScroll.setBorder(null);
        analysisScroll.getVerticalScrollBar().setUnitIncrement(16);
        analysisScroll.setOpaque(false);
        analysisScroll.getViewport().setOpaque(false);
        
        JScrollPane recommendationsScroll = new JScrollPane(recommendationsContent);
        recommendationsScroll.setBorder(null);
        recommendationsScroll.getVerticalScrollBar().setUnitIncrement(16);
        recommendationsScroll.setOpaque(false);
        recommendationsScroll.getViewport().setOpaque(false);
        
        // Add scroll panes to panels
        analysisPanel.add(analysisScroll, BorderLayout.CENTER);
        recommendationsPanel.add(recommendationsScroll, BorderLayout.CENTER);
        
        // Refresh UI
        revalidate();
        repaint();
    }
}

/**
 * Custom rounded border for panels
 */
class RoundedLineBorder extends LineBorder {
    private int radius;
    
    public RoundedLineBorder(Color color, int thickness, int radius) {
        super(color, thickness, false);
        this.radius = radius;
    }
    
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(lineColor);
        g2d.setStroke(new BasicStroke(thickness));
        g2d.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
        g2d.dispose();
    }
} 