package com.financeapp.view;

import com.financeapp.controller.TransactionController;
import com.financeapp.util.CSVHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;


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
        SHOPPING_FESTIVALS.put("618", "June 18");
        SHOPPING_FESTIVALS.put("Double 12", "December 12");
        SHOPPING_FESTIVALS.put("Women's Day", "March 8");
        SHOPPING_FESTIVALS.put("Valentine's Day", "February 14");
        SHOPPING_FESTIVALS.put("Dragon Boat ", "May/June (Lunar Calendar)");
        SHOPPING_FESTIVALS.put("Mid-Autumn", "September/October (Lunar Calendar)");
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
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.WEST);
        
        JLabel subtitleLabel = new JLabel("Analysis of modern Chinese spending habits");
        subtitleLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        subtitleLabel.setForeground(SECONDARY_COLOR);
        panel.add(subtitleLabel, BorderLayout.CENTER);
        
        JButton refreshButton = new JButton("Refresh Analysis");
        refreshButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
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

        // 数据准备
        final int[] years = IntStream.rangeClosed(2015, 2025).toArray();
        final double[] mobileAmounts = {45, 58, 73, 92, 115, 140, 168, 205, 245, 290, 340}; // 单位：十亿
        final double[] traditionalAmounts = {85, 82, 78, 72, 65, 58, 52, 47, 42, 38, 35};
        final Color TRADITIONAL_COLOR = new Color(255, 153, 51);

        // 图表面板
        JPanel chartPanel = new JPanel() {
            private String tooltipText;
            private Point tooltipPoint;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int chartX = 60, chartY = 30;
                int chartWidth = width - 100, chartHeight = height - 80;

                // 新增Y轴刻度绘制
                drawYAxisLabels(g2d, chartX, chartY, chartHeight, 350); // 最大值为350B

                // 绘制背景
                g2d.setColor(Color.WHITE);
                g2d.fillRect(chartX, chartY, chartWidth, chartHeight);

                // 绘制网格线
                g2d.setColor(new Color(220, 220, 220));
                for(int i=0; i<=10; i++){ // Y轴网格
                    int y = chartY + (int)(i * (chartHeight/10.0));
                    g2d.drawLine(chartX, y, chartX + chartWidth, y);
                }

                // 绘制坐标轴
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawLine(chartX, chartY + chartHeight, chartX + chartWidth, chartY + chartHeight); // X轴
                g2d.drawLine(chartX, chartY, chartX, chartY + chartHeight); // Y轴

                // 绘制X轴标签
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
                for(int i=0; i<years.length; i++){
                    int x = chartX + (int)(i * (chartWidth/(years.length-1.0)));
                    String label = String.valueOf(years[i]);
                    if(years[i] % 5 == 0){ // 每5年显示标签
                        g2d.drawString(label, x-10, chartY + chartHeight + 15);
                    }
                }

                // 绘制折线
                drawTrendLine(g2d, chartX, chartY, chartWidth, chartHeight,
                        mobileAmounts, PRIMARY_COLOR, "Mobile Shopping");
                drawTrendLine(g2d, chartX, chartY, chartWidth, chartHeight,
                        traditionalAmounts, TRADITIONAL_COLOR, "Traditional Shopping");

                // 绘制图例
                drawLegend(g2d, width - 150, 40);

                // 绘制工具提示
                if(tooltipText != null){
                    g2d.setColor(new Color(255, 255, 204, 220));
                    g2d.fillRoundRect(tooltipPoint.x, tooltipPoint.y - 25, 120, 20, 5, 5);
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(tooltipText, tooltipPoint.x + 5, tooltipPoint.y - 10);
                }
            }

            private void drawYAxisLabels(Graphics2D g2d, int chartX, int chartY, int chartHeight, double maxValue) {
                g2d.setColor(Color.DARK_GRAY);
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));

                // 计算刻度间隔
                int majorStep = 50; // 每50B一个主刻度
                int minorStep = 10; // 每10B一个次刻度

                // 绘制主刻度和标签
                for (int value = 0; value <= maxValue; value += minorStep) {
                    int yPos = chartY + chartHeight - (int)((value / maxValue) * chartHeight);

                    // 绘制刻度线
                    if (value % majorStep == 0) {
                        // 主刻度线
                        g2d.setStroke(new BasicStroke(1.5f));
                        g2d.drawLine(chartX - 5, yPos, chartX, yPos);
                        // 主刻度标签
                        String label = value + "B";
                        int labelWidth = g2d.getFontMetrics().stringWidth(label);
                        g2d.drawString(label, chartX - labelWidth - 10, yPos + 4);
                    } else {
                        // 次刻度线
                        g2d.setStroke(new BasicStroke(1.0f));
                        g2d.drawLine(chartX - 3, yPos, chartX, yPos);
                    }
                }

                // 添加Y轴标题
                g2d.rotate(Math.toRadians(-90), chartX - 40, chartY + chartHeight/2);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2d.drawString("Amount (Billion RMB)", chartX - 40, chartY + chartHeight/2);
                g2d.rotate(Math.toRadians(90), chartX - 40, chartY + chartHeight/2);
            }

            private void drawTrendLine(Graphics2D g2d, int chartX, int chartY,
                                       int chartWidth, int chartHeight, double[] data,
                                       Color color, String label) {
                // 计算坐标点
                List<Point> points = new ArrayList<>();
                double maxValue = 350; // 最大显示值

                for(int i=0; i<data.length; i++){
                    int x = chartX + (int)(i * (chartWidth/(years.length-1.0)));
                    int y = chartY + chartHeight - (int)((data[i]/maxValue) * chartHeight);
                    points.add(new Point(x, y));
                }

                // 绘制折线
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for(int i=0; i<points.size()-1; i++){
                    Point p1 = points.get(i);
                    Point p2 = points.get(i+1);
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                }

                // 绘制数据点
                g2d.setStroke(new BasicStroke(1.5f));
                for(Point p : points){
                    g2d.fillOval(p.x-3, p.y-3, 6, 6);
                    g2d.setColor(new Color(0, 0, 0, 50));
                    g2d.drawOval(p.x-3, p.y-3, 6, 6);
                    g2d.setColor(color);
                }
            }

            private void drawLegend(Graphics2D g2d, int x, int y) {
                g2d.setColor(PRIMARY_COLOR);
                g2d.fillRect(x, y, 12, 12);
                g2d.drawString("Mobile Shopping", x + 20, y + 10);

                g2d.setColor(TRADITIONAL_COLOR);
                g2d.fillRect(x, y + 20, 12, 12);
                g2d.drawString("Traditional Shopping", x + 20, y + 30);
            }

            @Override
            public void addNotify() {
                super.addNotify();
                addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        int x = e.getX();
                        int y = e.getY();
                        tooltipText = null;

                        // 检测移动购物数据点
                        checkPoints(x, y, mobileAmounts, "Mobile: ¥%.1fB");
                        // 检测传统购物数据点
                        checkPoints(x, y, traditionalAmounts, "Traditional: ¥%.1fB");

                        tooltipPoint = new Point(x + 15, y);
                        repaint();
                    }

                    private void checkPoints(int mouseX, int mouseY, double[] data, String format) {
                        int chartX = 60, chartY = 30;
                        int chartWidth = getWidth() - 100;

                        for(int i=0; i<data.length; i++){
                            int pointX = chartX + (int)(i * (chartWidth/(years.length-1.0)));
                            int pointY = chartY + (int)((350 - data[i])/350.0 * (getHeight() - 80));

                            if(Math.abs(mouseX - pointX) < 8 && Math.abs(mouseY - pointY) < 8) {
                                tooltipText = String.format(format, data[i]);
                                return;
                            }
                        }
                    }
                });
            }
        };

        chartPanel.setPreferredSize(new Dimension(600, 400));
        chartPanel.setBackground(CARD_BACKGROUND);

        // Create trend summary
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);

        JLabel trendLabel = new JLabel("Mobile Shopping Dominance");
        trendLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        summaryPanel.add(trendLabel);

        JLabel growthLabel = new JLabel("Mobile Shopping Growth: +24.8% annually");
        growthLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        growthLabel.setForeground(SUCCESS_COLOR);
        summaryPanel.add(growthLabel);

        JLabel statsLabel = new JLabel("Mobile accounts for 85% of all transactions");
        statsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
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
        categoryTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        categoryTable.setRowHeight(25);
        categoryTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
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

        final Color[] colors = {
                new Color(52, 152, 219), new Color(46, 204, 113),
                new Color(155, 89, 182), new Color(241, 196, 15),
                new Color(230, 126, 34), new Color(253, 180, 98),
                new Color(230, 230, 230), new Color(192, 57, 43)
        };

        final List<Object[]> sectors = new ArrayList<>();
        final double[] total = {0};

        JPanel chartPanel = new JPanel() {
            int hoverIndex = -1;
            Point hoverPoint = new Point();

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

                sectors.clear();
                total[0] = PAYMENT_METHODS.values().stream().mapToDouble(Double::doubleValue).sum();

                // 绘制饼图
                double startAngle = 0;
                int index = 0;
                for (Map.Entry<String, Double> entry : PAYMENT_METHODS.entrySet()) {
                    double angle = 360 * (entry.getValue() / total[0]);
                    sectors.add(new Object[]{startAngle, angle, entry.getKey(), entry.getValue()});

                    g2d.setColor(hoverIndex == index ? colors[index].brighter() : colors[index]);
                    g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2,
                            (int) startAngle, (int) angle);

                    startAngle += angle;
                    index++;
                }

                // 先绘制图例
                drawModernLegend(g2d, width);

                // 最后绘制提示框
                if (hoverIndex != -1 && !sectors.isEmpty()) {
                    drawHoverTooltip(g2d,
                            (String)sectors.get(hoverIndex)[2],
                            (Double)sectors.get(hoverIndex)[3],
                            hoverPoint);
                }
            }

            private void drawModernLegend(Graphics2D g2d, int panelWidth) {
                // 动态计算图例尺寸
                int maxTextWidth = calculateMaxTextWidth(g2d);
                int legendWidth = Math.min(maxTextWidth + 50, panelWidth / 3); // 最大不超过1/3面板宽度
                int rightMargin = 20;

                // 智能定位（右侧不足时换到左侧）
                int legendX;
                if (panelWidth - (getWidth()/2 + Math.min(getWidth(), getHeight())/3) > legendWidth + rightMargin) {
                    legendX = (getWidth()/2) + Math.min(getWidth(), getHeight())/3 + 20; // 右侧
                } else {
                    legendX = 30; // 左侧
                }

                int legendY = 40;
                int itemHeight = 22;
                int colorBlockSize = 16;

                // 背景尺寸自适应
                int legendHeight = PAYMENT_METHODS.size() * itemHeight + 30;
                g2d.setColor(new Color(255, 255, 255, 230));
                g2d.fillRoundRect(legendX - 10, legendY - 10, legendWidth + 20, legendHeight, 15, 15);

                // 标题优化
                g2d.setColor(new Color(51, 51, 51));
                g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2d.drawString("Payment Methods", legendX, legendY);

                // 图例项布局
                int index = 0;
                for (Map.Entry<String, Double> entry : PAYMENT_METHODS.entrySet()) {
                    int yPos = legendY + 30 + index * itemHeight;

                    // 颜色块
                    g2d.setColor(colors[index]);
                    g2d.fillRoundRect(legendX, yPos, 20, 20, 5, 5);

                    // 文本
                    g2d.setColor(new Color(60, 60, 60));
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    String legendText = String.format("%s (%.1f%%)",
                            entry.getKey(),
                            entry.getValue());
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    g2d.setColor(new Color(60, 60, 60));
                    drawTruncatedText(g2d, legendText, legendX + colorBlockSize + 12, yPos + 12, legendWidth - 30);

                    index++;
                }
            }

            // 辅助方法：计算最大文本宽度
            private int calculateMaxTextWidth(Graphics2D g2d) {
                FontMetrics fm = g2d.getFontMetrics();
                int maxWidth = 0;
                for (Map.Entry<String, Double> entry : PAYMENT_METHODS.entrySet()) {
                    String text = String.format("%s %.1f%%", entry.getKey(), entry.getValue());
                    maxWidth = Math.max(maxWidth, fm.stringWidth(text));
                }
                return maxWidth;
            }

            // 辅助方法：文本截断
            private String truncateText(String text, int maxLength) {
                return text.length() > maxLength ? text.substring(0, maxLength-2) + ".." : text;
            }

            // 辅助方法：安全绘制文本
            private void drawTruncatedText(Graphics2D g2d, String text, int x, int y, int maxWidth) {
                FontMetrics fm = g2d.getFontMetrics();
                if (fm.stringWidth(text) > maxWidth) {
                    int charNum = 0;
                    while (fm.stringWidth(text.substring(0, charNum+1) + "...") < maxWidth) {
                        charNum++;
                    }
                    text = text.substring(0, charNum) + "...";
                }
                g2d.drawString(text, x, y);
            }

            // 统一参数为四个的提示框方法
            private void drawHoverTooltip(Graphics2D g2d, String label, double percent, Point pos) {
                String text = String.format("%s: %.1f%%", label, percent);
                FontMetrics fm = g2d.getFontMetrics();

                // 动态定位
                int textWidth = fm.stringWidth(text);
                int tipX = Math.min(pos.x, getWidth() - textWidth - 20);
                int tipY = Math.max(pos.y - 30, 20);

                // 背景和文字绘制
                g2d.setColor(new Color(255, 255, 225, 220));
                g2d.fillRoundRect(tipX, tipY, textWidth + 15, 28, 6, 6);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString(text, tipX + 8, tipY + 18);
            }

            @Override
            public void addNotify() {
                super.addNotify();
                addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        checkHover(e.getX(), e.getY());
                        hoverPoint = e.getPoint();
                        repaint();
                    }
                });
            }

            private void checkHover(int x, int y) {
                // 保持原有检测逻辑不变
            }
        };

        chartPanel.setPreferredSize(new Dimension(400, 300));
        chartPanel.setBackground(CARD_BACKGROUND);

        // 底部说明面板
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);

        // 添加说明标签（保持原有代码不变）
        JLabel methodLabel = new JLabel("Mobile Payment");
        methodLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        summaryPanel.add(methodLabel);

        JLabel mobileLabel = new JLabel("Alipay & WeChat Pay: 85% of transactions");
        mobileLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        mobileLabel.setForeground(PRIMARY_COLOR);
        summaryPanel.add(mobileLabel);

        JLabel cashLabel = new JLabel("Cash usage declined by 15% this year");
        cashLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
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
            private int tooltipX, tooltipY;
            private String tooltipText;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                // Draw chart background
                g2d.setColor(CARD_BACKGROUND);
                g2d.fillRect(40, 30, width - 80, height - 60);

                // Draw axes
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawLine(40, height - 30, width - 40, height - 30); // X-axis
                g2d.drawLine(40, 30, 40, height - 30); // Y-axis

                // Sample data for festival spending (billions)
                String[] festivals = SHOPPING_FESTIVALS.keySet().toArray(new String[0]);
                double[] amounts = {84.5, 58.2, 30.8, 25.5, 45.2, 60.0, 70.5, 35.0, 50.0, 40.0, 90.0, 55.0, 65.0};

                // Find max for scaling
                double maxAmount = 90.0; // Fixed max for better visualization

                // Draw bars
                int barWidth = (width - 120) / festivals.length;
                int spacing = 5;
                int barX = 60;

                for (int i = 0; i < festivals.length; i++) {
                    // Bar height relative to max
                    int barHeight = (int) ((amounts[i] / maxAmount) * (height - 80));

                    // Festival-specific colors
                    if (festivals[i].equals("Double 11") || festivals[i].equals("618")) {
                        g2d.setColor(FESTIVAL_RED);
                    } else if (festivals[i].equals("Women's Day") || festivals[i].equals("Mid-Autumn")) {
                        g2d.setColor(FESTIVAL_GOLD);
                    } else {
                        g2d.setColor(PRIMARY_COLOR);
                    }

                    // Draw bar
                    g2d.fillRect(barX, height - 30 - barHeight, barWidth - spacing, barHeight);

                    // Draw label
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawString(festivals[i], barX + (barWidth - spacing) / 2 - g2d.getFontMetrics().stringWidth(festivals[i]) / 2, height - 15);

                    // Draw value
                    g2d.drawString(String.format("¥%.1fB", amounts[i]),
                            barX + (barWidth - spacing) / 2 - g2d.getFontMetrics().stringWidth(String.format("¥%.1fB", amounts[i])) / 2, height - 35 - barHeight);

                    barX += barWidth;
                }

                // Draw tooltip
                if (tooltipText != null) {
                    g2d.setColor(new Color(255, 255, 204));
                    g2d.fillRect(tooltipX, tooltipY, 100, 20);
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(tooltipText, tooltipX + 5, tooltipY + 15);
                }
            }

            @Override
            public void addNotify() {
                super.addNotify();

                // Add mouse motion listener for tooltips
                addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        int x = e.getX();
                        int y = e.getY();

                        // Check if the mouse is over a bar
                        int barWidth = (getWidth() - 120) / SHOPPING_FESTIVALS.size();
                        int spacing = 5;
                        int barX = 60;

                        for (int i = 0; i < SHOPPING_FESTIVALS.size(); i++) {
                            if (x >= barX && x <= barX + barWidth - spacing) {
                                double amount = 84.5; // Sample data for demonstration
                                tooltipX = x + 10;
                                tooltipY = y - 10;
                                tooltipText = String.format("¥%.1fB", amount);
                                repaint();
                                return;
                            }
                            barX += barWidth;
                        }

                        // Mouse is not over a bar, clear tooltip
                        tooltipText = null;
                        repaint();
                    }
                });
            }
        };
        chartPanel.setPreferredSize(new Dimension(600, 400));
        chartPanel.setBackground(CARD_BACKGROUND);

        // Create festival summary
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);

        JLabel festivalLabel = new JLabel("Shopping Festival Analysis");
        festivalLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        summaryPanel.add(festivalLabel);

        JLabel growthLabel = new JLabel("Total Spending: ¥450B annually");
        growthLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        growthLabel.setForeground(SUCCESS_COLOR);
        summaryPanel.add(growthLabel);

        panel.add(chartPanel, BorderLayout.CENTER);
        panel.add(summaryPanel, BorderLayout.SOUTH);

        return panel;
    }

    private static final Map<String, ImageIcon> PLATFORM_ICONS = new HashMap<>();
    static {
        PLATFORM_ICONS.put("Taobao/Tmall", new ImageIcon("src/main/resources/icons/taobao.png")); // 请替换为实际的图标路径
        PLATFORM_ICONS.put("JD.com", new ImageIcon("src/main/resources/icons/jingdong.png")); // 请替换为实际的图标路径
        PLATFORM_ICONS.put("Pinduoduo", new ImageIcon("src/main/resources/icons/pinduoduo.png")); // 请替换为实际的图标路径
        PLATFORM_ICONS.put("Douyin", new ImageIcon("src/main/resources/icons/douyin.png")); // 请替换为实际的图标路径
        PLATFORM_ICONS.put("Others", new ImageIcon("src/main/resources/icons/other.png")); // 请替换为实际的图标路径
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
                {"Others", "7%", "+5.5%", "Niche markets"}
        };

        // Create platform components
        for (int i = 0; i < platformData.length; i++) {
            String[] platform = platformData[i];
            JPanel platformPanel = new JPanel(new BorderLayout(10, 0));
            platformPanel.setOpaque(false);
            platformPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

            JLabel nameLabel = new JLabel(platform[0]);
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            nameLabel.setForeground(PRIMARY_COLOR);
            nameLabel.setIcon(PLATFORM_ICONS.get(platform[0])); // 添加图标
            nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0)); // 调整图标和文字的间距

            JPanel detailsPanel = new JPanel(new GridLayout(2, 2, 10, 2));
            detailsPanel.setOpaque(false);
            detailsPanel.setBackground(new Color(255, 255, 255, 50)); // 添加背景色
            detailsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 调整内边距

            JLabel marketShareLabel = new JLabel("Market Share: " + platform[1]);
            marketShareLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            marketShareLabel.setForeground(PRIMARY_COLOR);
            marketShareLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // 调整间距

            JLabel growthLabel = new JLabel("Growth: " + platform[2]);
            growthLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            if (platform[2].startsWith("+")) {
                growthLabel.setForeground(SUCCESS_COLOR);
            } else {
                growthLabel.setForeground(Color.RED); // 如果增长率为负数，则显示为红色
            }
            growthLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // 调整间距

            JLabel categoryLabel = new JLabel("Focus: " + platform[3]);
            categoryLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
            categoryLabel.setForeground(Color.GRAY); // 设置为灰色，以突出显示其他信息
            categoryLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // 调整间距

            detailsPanel.add(marketShareLabel);
            detailsPanel.add(growthLabel);
            detailsPanel.add(categoryLabel);

            platformPanel.add(nameLabel, BorderLayout.WEST);
            platformPanel.add(detailsPanel, BorderLayout.CENTER);

            // Add some spacing
            dataPanel.add(platformPanel);
            dataPanel.add(Box.createVerticalStrut(10)); // 增加垂直间距
        }

        JScrollPane scrollPane = new JScrollPane(dataPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);
        scrollPane.addMouseListener(new MouseAdapter() { // 添加鼠标监听器，用于处理鼠标点击事件（可选）
            @Override
            public void mouseClicked(MouseEvent e) {
                // 处理鼠标点击事件，例如显示详细信息窗口或执行其他操作（可选）
            }
        });

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
        summaryLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        summaryPanel.add(summaryLabel);

        JLabel statsLabel = new JLabel("Average user accesses 4.2 digital services daily");
        statsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
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
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel usageLabel = new JLabel("Usage: " + penetration);
        usageLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JLabel frequencyLabel = new JLabel("Freq: " + frequency);
        frequencyLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JLabel descriptionLabel = new JLabel("Description: " + description);
        descriptionLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
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
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
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