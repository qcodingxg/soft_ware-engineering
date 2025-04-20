package com.financeapp.view;

import com.financeapp.controller.TransactionController;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;

/**
 * Statistics View Panel
 * Display monthly expense statistics and budget suggestions
 */
public class StatisticsPanel extends JPanel {
    
    private final TransactionController controller;
    
    // UI components
    private JPanel chartPanel;
    private JTextArea suggestionsArea;
    private JComboBox<Integer> yearComboBox;
    //private JComboBox<String> monthComboBox;
    //将monthComboBox改为JList,实现多选
    private JList<String> monthList;

    // Colors (matching login panel)
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color[] CHART_COLORS = {
            new Color(41, 128, 185),  // Primary Blue
            new Color(46, 204, 113),  // Green
            new Color(230, 126, 34),  // Orange
            new Color(155, 89, 182),  // Purple
            new Color(52, 152, 219),  // Secondary Blue
            new Color(26, 188, 156),  // Turquoise
            new Color(231, 76, 60),   // Red
            new Color(243, 156, 18),  // Yellow
            new Color(142, 68, 173),  // Dark Purple
            new Color(22, 160, 133)   // Dark Turquoise
    };
    
    /**
     * Constructor
     * @param controller Transaction controller
     */
    public StatisticsPanel(TransactionController controller) {
        this.controller = controller;
        initUI();
    }
    
    /**
     * Initialize UI
     */
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create top control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Create chart panel (center area)
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                "Monthly Expense Statistics", 
                TitledBorder.LEFT, 
                TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 12),
                PRIMARY_COLOR));
        chartPanel.setBackground(BACKGROUND_COLOR);
        add(chartPanel, BorderLayout.CENTER);
        
        // Create suggestions panel (bottom area)
        JPanel suggestionsPanel = createSuggestionsPanel();
        add(suggestionsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Create control panel
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                "Date Selection", 
                TitledBorder.LEFT, 
                TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 12),
                PRIMARY_COLOR));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Year selection
        JLabel yearLabel = new JLabel("Year:");
        yearLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        yearLabel.setForeground(TEXT_COLOR);
        panel.add(yearLabel);
        
        yearComboBox = new JComboBox<>();
        yearComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        yearComboBox.setBackground(Color.WHITE);
        yearComboBox.setForeground(TEXT_COLOR);
        yearComboBox.setPreferredSize(new Dimension(80, 25));
        
        // Add years (current year and previous 2 years)
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear; year >= currentYear - 2; year--) {
            yearComboBox.addItem(year);
        }
        
        panel.add(yearComboBox);
        
        // Month selection
        JLabel monthLabel = new JLabel("Month:");
        monthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        monthLabel.setForeground(TEXT_COLOR);
        panel.add(monthLabel);
        
//        monthComboBox = new JComboBox<>();
//        monthComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//        monthComboBox.setBackground(Color.WHITE);
//        monthComboBox.setForeground(TEXT_COLOR);
//        monthComboBox.setPreferredSize(new Dimension(120, 25));
//
//        // Add custom renderer to fix display issues
//        monthComboBox.setRenderer(new DefaultListCellRenderer() {
//            @Override
//            public Component getListCellRendererComponent(JList<?> list, Object value,
//                    int index, boolean isSelected, boolean cellHasFocus) {
//                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//                setOpaque(true);
//                if (isSelected) {
//                    setBackground(SECONDARY_COLOR);
//                    setForeground(Color.WHITE);
//                } else {
//                    setBackground(Color.WHITE);
//                    setForeground(TEXT_COLOR);
//                }
//                return this;
//            }
//        });
        
        // Add all months with English names
        String[] englishMonths = {
            "January", "February", "March", "April", "May", "June", 
            "July", "August", "September", "October", "November", "December"
        };
        DefaultListModel<String> monthListModel = new DefaultListModel<>();
        for (String month : englishMonths) {
            monthListModel.addElement(month);
        }
        // Create JList for month selection
        monthList = new JList<>(monthListModel);
        monthList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        monthList.setVisibleRowCount(4);
        monthList.setFont(new Font("Segue UI", Font.PLAIN, 12));
        monthList.setForeground(TEXT_COLOR);
        monthList.setBackground(Color.WHITE);
        monthList.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        JScrollPane monthScrollPane = new JScrollPane(monthList);
        monthScrollPane.setPreferredSize(new Dimension(120, 80));
        panel.add(monthScrollPane);
//        for (String month : englishMonths) {
//            monthComboBox.addItem(month);
//        }
//
//        // Set current month as default
//        monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
//
//        panel.add(monthComboBox);
        
        // Query button
        JButton queryButton = createStyledButton("Search", PRIMARY_COLOR);
        queryButton.addActionListener(e -> updateStatistics());
        //我想要在按钮上添加鼠标悬停效果，显示“使用ctrl键加单击选择多个选项”
        queryButton.setToolTipText("Use Ctrl + Click to select multiple months");
        panel.add(queryButton);
        
        return panel;
    }
    
    /**
     * Create suggestions panel
     */
    private JPanel createSuggestionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                "Budget Suggestions", 
                TitledBorder.LEFT, 
                TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 12),
                PRIMARY_COLOR));
        panel.setBackground(BACKGROUND_COLOR);
        
        suggestionsArea = new JTextArea(5, 40);
        suggestionsArea.setEditable(false);
        suggestionsArea.setLineWrap(true);
        suggestionsArea.setWrapStyleWord(true);
        suggestionsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        suggestionsArea.setForeground(TEXT_COLOR);
        suggestionsArea.setBackground(Color.WHITE);
        suggestionsArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SECONDARY_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        JScrollPane scrollPane = new JScrollPane(suggestionsArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create styled button
     * @param text Button text
     * @param color Button color
     * @return Styled button
     */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(color.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    /**
     * Update statistics
     */
    public void updateStatistics() {
        // Get selected year and month
        int selectedYear = (Integer) yearComboBox.getSelectedItem();
        //int selectedMonth = monthComboBox.getSelectedIndex() + 1; // Index starts from 0, month starts from 1
        //修改为List
        List<String> selectedMonths = monthList.getSelectedValuesList();

        // Clear previous data
        chartPanel.removeAll();
        suggestionsArea.setText("");
        
//        // Get expense data for selected month
//        Map<String, Double> expenses = controller.getMonthlyExpenses(selectedYear, selectedMonth);
//
//        if (expenses.isEmpty()) {
//            // No data for selected month
//            JLabel noDataLabel = new JLabel("No expense data for selected month", SwingConstants.CENTER);
//            noDataLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
//            noDataLabel.setForeground(TEXT_COLOR);
//            chartPanel.add(noDataLabel, BorderLayout.CENTER);
//        } else {
//            // Draw bar chart
//            drawBarChart(expenses);
//
//            // Update suggestions
//            List<String> suggestions = controller.getBudgetSuggestions();
//            StringBuilder sb = new StringBuilder();
//            for (String suggestion : suggestions) {
//                sb.append("• ").append(suggestion).append("\n");
//            }
//            suggestionsArea.setText(sb.toString());
//        }
        if (selectedMonths.isEmpty()) {
            JLabel noSelection = new JLabel("Please select at least one month.", SwingConstants.CENTER);
            noSelection.setFont(new Font("Segue UI", Font.ITALIC, 14));
            noSelection.setForeground(ERROR_COLOR);
            chartPanel.add(noSelection, BorderLayout.CENTER);
        }
        else {
            Map<String, Map<String, Double>> multiMonthData = new HashMap<>();
            for (String monthName : selectedMonths) {
                int monthIndex = Month.valueOf(monthName.toUpperCase()).getValue();
                Map<String, Double> monthExpenses = controller.getMonthlyExpenses(selectedYear, monthIndex);
                if (!monthExpenses.isEmpty()) {
                    multiMonthData.put(monthName, monthExpenses);
                }
            }
            //若只选择一个月，则直接绘制柱状图
            if (multiMonthData.size() == 1) {
                Map.Entry<String, Map<String, Double>> entry = multiMonthData.entrySet().iterator().next();
                drawBarChart(entry.getValue());
            }
            //若选择多个，则绘制分组柱状图
            else if (multiMonthData.size() > 1) {
                drawGroupedBarChart(multiMonthData);
            }
            else {
                JLabel noDataLabel = new JLabel("No data for selected months.", SwingConstants.CENTER);
                noDataLabel.setFont(new Font("Segue UI", Font.ITALIC, 14));
                noDataLabel.setForeground(TEXT_COLOR);
                chartPanel.add(noDataLabel, BorderLayout.CENTER);
            }
        }
        // Refresh UI
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    /**
     * Draw grouped bar chart
     */
    private void drawGroupedBarChart(Map<String, Map<String, Double>> expenses) {
        // Create chart component
        GroupedBarChartComponent chart = new GroupedBarChartComponent(expenses);
        chartPanel.add(chart, BorderLayout.CENTER);

        // Create legend
        JPanel legendPanel = new JPanel(new GridLayout(0, 2, 5, 2));
        legendPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1),
                "Legend",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                PRIMARY_COLOR));
        legendPanel.setBackground(BACKGROUND_COLOR);

        // Add total to legend
        double total = expenses.values().stream()
                .flatMap(map -> map.values().stream())
                .mapToDouble(Double::doubleValue)
                .sum();
        JLabel totalLabel = new JLabel("Total:");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalLabel.setForeground(TEXT_COLOR);
        legendPanel.add(totalLabel);

        JLabel totalValueLabel = new JLabel(String.format("%.2f", total));
        totalValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalValueLabel.setForeground(TEXT_COLOR);
        legendPanel.add(totalValueLabel);

        // Add categories to legend
        int index = 0;
        for (Map.Entry<String, Map<String, Double>> entry : expenses.entrySet()) {
            String month = entry.getKey();
            Map<String, Double> monthData = entry.getValue();
            for (Map.Entry<String, Double> categoryEntry : monthData.entrySet()) {
                String category = categoryEntry.getKey();
                double value = categoryEntry.getValue();
                Color color = CHART_COLORS[index % CHART_COLORS.length];

                // Create colored square icon
                JLabel categoryLabel = new JLabel(month + " - " + category + ":");
                categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                categoryLabel.setForeground(TEXT_COLOR);
                categoryLabel.setIcon(createColorIcon(color, 12, 12));
                legendPanel.add(categoryLabel);

                // Create value label
                JLabel valueLabel = new JLabel(String.format("%.2f (%.1f%%)",
                        value, value / total * 100));
                valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                valueLabel.setForeground(TEXT_COLOR);
                legendPanel.add(valueLabel);

                index++;
            }
        }
        chartPanel.add(legendPanel, BorderLayout.SOUTH);
    }
    /**
     * Draw bar chart
     */
    private void drawBarChart(Map<String, Double> expenses) {
        // Create chart component
        BarChartComponent chart = new BarChartComponent(expenses);
        chartPanel.add(chart, BorderLayout.CENTER);
        
        // Create legend
        JPanel legendPanel = new JPanel(new GridLayout(0, 2, 5, 2));
        legendPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1),
                "Legend", 
                TitledBorder.LEFT, 
                TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 12),
                PRIMARY_COLOR));
        legendPanel.setBackground(BACKGROUND_COLOR);
        
        // Add total to legend
        double total = expenses.values().stream().mapToDouble(Double::doubleValue).sum();
        JLabel totalLabel = new JLabel("Total:");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalLabel.setForeground(TEXT_COLOR);
        legendPanel.add(totalLabel);
        
        JLabel totalValueLabel = new JLabel(String.format("%.2f", total));
        totalValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalValueLabel.setForeground(TEXT_COLOR);
        legendPanel.add(totalValueLabel);
        
        // Add categories to legend
        int index = 0;
        for (Map.Entry<String, Double> entry : expenses.entrySet()) {
            Color color = CHART_COLORS[index % CHART_COLORS.length];
            
            // Create colored square icon
            JLabel categoryLabel = new JLabel(entry.getKey() + ":");
            categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            categoryLabel.setForeground(TEXT_COLOR);
            categoryLabel.setIcon(createColorIcon(color, 12, 12));
            legendPanel.add(categoryLabel);
            
            // Create value label
            JLabel valueLabel = new JLabel(String.format("%.2f (%.1f%%)", 
                    entry.getValue(), entry.getValue() / total * 100));
            valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            valueLabel.setForeground(TEXT_COLOR);
            legendPanel.add(valueLabel);
            
            index++;
        }
        
        chartPanel.add(legendPanel, BorderLayout.SOUTH);
    }
    /**
     * Create color icon for legend
     */
    private Icon createColorIcon(Color color, int width, int height) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.fillRect(x, y, width, height);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, width, height);
            }
            
            @Override
            public int getIconWidth() {
                return width;
            }
            
            @Override
            public int getIconHeight() {
                return height;
            }
        };
    }
    private static class GroupedBarChartComponent extends JPanel {
        private final Map<String, Map<String, Double>> multiMonthData; // month -> category -> value

        public GroupedBarChartComponent(Map<String, Map<String, Double>> data) {
            this.multiMonthData = data;
            setPreferredSize(new Dimension(600, 350));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (multiMonthData.isEmpty()) return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Collect all unique categories
            Set<String> allCategories = new HashSet<>();
            for (Map<String, Double> monthMap : multiMonthData.values()) {
                allCategories.addAll(monthMap.keySet());
            }

            List<String> categories = new ArrayList<>(allCategories);
            List<String> months = new ArrayList<>(multiMonthData.keySet());

            int width = getWidth();
            int height = getHeight();
            int margin = 40;
            int barSpacing = 5;
            int groupWidth = 60;
            int barWidth = (groupWidth - (months.size() - 1) * barSpacing) / months.size();

            // Find max value
            double maxValue = 1.0;
            for (Map<String, Double> map : multiMonthData.values()) {
                for (double val : map.values()) {
                    if (val > maxValue) maxValue = val;
                }
            }

            // Draw bars
            int startX = margin;
            int startY = height - margin;

            for (int i = 0; i < categories.size(); i++) {
                String category = categories.get(i);
                int groupX = startX + i * (groupWidth + 30);

                for (int j = 0; j < months.size(); j++) {
                    String month = months.get(j);
                    Map<String, Double> monthData = multiMonthData.getOrDefault(month, new HashMap<>());
                    double value = monthData.getOrDefault(category, 0.0);
                    int barHeight = (int) (value / maxValue * (height - 2 * margin - 20));
                    int x = groupX + j * (barWidth + barSpacing);
                    int y = startY - barHeight;

                    // Draw bar
                    g2d.setColor(CHART_COLORS[j % CHART_COLORS.length]);
                    g2d.fillRect(x, y, barWidth, barHeight);
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawRect(x, y, barWidth, barHeight);

                    // Draw value
                    g2d.setColor(TEXT_COLOR);
                    g2d.setFont(new Font("Segue UI", Font.PLAIN, 10));
                    String valStr = String.format("%.0f", value);
                    int textW = g2d.getFontMetrics().stringWidth(valStr);
                    g2d.drawString(valStr, x + (barWidth - textW) / 2, y - 3);
                }

                // Draw category label
                g2d.setColor(TEXT_COLOR);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String label = category.length() > 10 ? category.substring(0, 8) + "…" : category;
                int labelWidth = g2d.getFontMetrics().stringWidth(label);
                g2d.drawString(label, groupX + (groupWidth - labelWidth) / 2, startY + 15);
            }

            // Draw axes
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawLine(margin - 10, startY, width - margin / 2, startY); // x-axis

            // Draw legend
            int legendX = width - 150;
            int legendY = margin;

            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2d.setColor(TEXT_COLOR);
            g2d.drawString("Legend:", legendX, legendY);

            for (int j = 0; j < months.size(); j++) {
                Color color = CHART_COLORS[j % CHART_COLORS.length];
                g2d.setColor(color);
                g2d.fillRect(legendX, legendY + 10 + j * 18, 12, 12);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawRect(legendX, legendY + 10 + j * 18, 12, 12);

                g2d.setColor(TEXT_COLOR);
                g2d.drawString(months.get(j), legendX + 18, legendY + 20 + j * 18);
            }
        }
    }
    /**
     * Bar chart component for drawing expense data
     */
    private class BarChartComponent extends JPanel {
        private final Map<String, Double> data;
        
        /**
         * Constructor
         */
        public BarChartComponent(Map<String, Double> data) {
            this.data = new HashMap<>(data);
            setPreferredSize(new Dimension(400, 300));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (data.isEmpty()) {
                return;
            }
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            int barWidth = width / (data.size() * 2);
            
            // Find maximum value
            double maxValue = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
            
            // Draw bars
            int index = 0;
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                int x = index * barWidth * 2 + barWidth / 2;
                int barHeight = (int) (entry.getValue() / maxValue * (height - 50));
                int y = height - barHeight - 30;
                
                // Draw bar
                g2d.setColor(CHART_COLORS[index % CHART_COLORS.length]);
                g2d.fillRect(x, y, barWidth, barHeight);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawRect(x, y, barWidth, barHeight);
                
                // Draw category name
                g2d.setColor(TEXT_COLOR);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                FontMetrics fm = g2d.getFontMetrics();
                String category = entry.getKey();
                if (category.length() > 10) {
                    category = category.substring(0, 7) + "...";
                }
                int textWidth = fm.stringWidth(category);
                g2d.drawString(category, x + (barWidth - textWidth) / 2, height - 10);
                
                // Draw value
                g2d.setColor(TEXT_COLOR);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String value = String.format("%.0f", entry.getValue());
                textWidth = g2d.getFontMetrics().stringWidth(value);
                g2d.drawString(value, x + (barWidth - textWidth) / 2, y - 5);
                
                index++;
            }
            
            // Draw axes
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawLine(barWidth / 2 - 10, height - 30, width - barWidth / 2, height - 30); // X-axis
        }
    }
} 