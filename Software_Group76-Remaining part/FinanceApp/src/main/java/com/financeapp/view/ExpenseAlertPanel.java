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
    private JPanel topAlertPanel;
    private JPanel bottomSettingsPanel;
    private JPanel globalSettingsPanel;
    private JPanel categoryThresholdsPanel;
    private JButton viewAllAlertsButton;
    private JButton saveSettingsButton;
    private JButton refreshButton;
    private JButton historyAlarmsButton; // 新增历史警报按钮
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

        // 顶部警报显示区
        topAlertPanel = createTopAlertPanel();
        add(topAlertPanel, BorderLayout.NORTH);

        // 底部设置区
        bottomSettingsPanel = createBottomSettingsPanel();
        add(bottomSettingsPanel, BorderLayout.CENTER);

        // 右上角刷新按钮
        refreshButton = new JButton("Refresh Alerts");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setBackground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> updateExpenseAlerts());
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshPanel.setOpaque(false);
        refreshPanel.add(refreshButton);

        // 新增历史警报按钮
        historyAlarmsButton = new JButton("History Alarms");
        historyAlarmsButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyAlarmsButton.setForeground(Color.BLACK);
        historyAlarmsButton.setBackground(Color.WHITE);
        historyAlarmsButton.setFocusPainted(false);
        historyAlarmsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        historyAlarmsButton.addActionListener(e -> showHistoryAlarms());
        refreshPanel.add(historyAlarmsButton);

        add(refreshPanel, BorderLayout.SOUTH);

        // Initial data update
        updateExpenseAlerts();
    }

    /**
     * 创建顶部警报显示区
     */
    private JPanel createTopAlertPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        // 警报内容面板
        JPanel alertsContent = new JPanel();
        alertsContent.setLayout(new BoxLayout(alertsContent, BoxLayout.Y_AXIS));
        alertsContent.setOpaque(false);

        JScrollPane alertsScrollPane = new JScrollPane(alertsContent);
        alertsScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.add(alertsScrollPane, BorderLayout.CENTER);

        // 右下角查看全部警报按钮
        viewAllAlertsButton = new JButton("View All Alerts");
        viewAllAlertsButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewAllAlertsButton.setBackground(Color.WHITE);
        viewAllAlertsButton.setForeground(Color.BLACK);
        viewAllAlertsButton.setFocusPainted(false);
        viewAllAlertsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllAlertsButton.addActionListener(e -> showAllAlerts());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(viewAllAlertsButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建底部设置区
     */
    private JPanel createBottomSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);

        // 全局设置部分
        globalSettingsPanel = createGlobalSettingsPanel();
        panel.add(globalSettingsPanel, BorderLayout.NORTH);

        // 类别阈值设置部分
        categoryThresholdsPanel = createCategoryThresholdsPanel();
        panel.add(categoryThresholdsPanel, BorderLayout.CENTER);

        // 右下角保存设置按钮
        saveSettingsButton = new JButton("Save Settings");
        saveSettingsButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        saveSettingsButton.setBackground(Color.WHITE);
        saveSettingsButton.setForeground(Color.BLACK);
        saveSettingsButton.setFocusPainted(false);
        saveSettingsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveSettingsButton.addActionListener(e -> saveSettings());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(saveSettingsButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建全局设置部分
     */
    private JPanel createGlobalSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        // 启用/禁用预警功能的复选框
        enableAlertsCheckbox = new JCheckBox("Enable Alerts", alertsEnabled);
        enableAlertsCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        enableAlertsCheckbox.setOpaque(false);
        panel.add(enableAlertsCheckbox);

        // 全局支出预警阈值设置
        JLabel globalThresholdLabel = new JLabel("Global Threshold (¥):");
        globalThresholdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(globalThresholdLabel);

        globalThresholdField = new JTextField(String.valueOf(globalThreshold), 10);
        globalThresholdField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(globalThresholdField);

        return panel;
    }

    /**
     * 创建类别阈值设置部分
     */
    private JPanel createCategoryThresholdsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(15, 15));
        panel.setOpaque(false);

        // 表格形式展示各类别的预警阈值
        String[] columns = {"Category", "Threshold (¥)"};
        categoryThresholdsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // 只有阈值列可编辑
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        categoryThresholdsTable = new JTable(categoryThresholdsModel);
        categoryThresholdsTable.setRowHeight(25);
        categoryThresholdsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        categoryThresholdsTable.getTableHeader().setBackground(SECONDARY_COLOR);
        categoryThresholdsTable.getTableHeader().setForeground(Color.WHITE);
        categoryThresholdsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column widths
        categoryThresholdsTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Category
        categoryThresholdsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Threshold

        JScrollPane scrollPane = new JScrollPane(categoryThresholdsTable);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.add(scrollPane, BorderLayout.CENTER);

        // 添加和删除类别按钮
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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(addCategoryButton);
        buttonPanel.add(removeCategoryButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 更新支出预警信息
     */
    private void updateExpenseAlerts() {
        // 检测预警
        List<Alert> alerts = checkAlerts();

        // 清空顶部警报显示区
        JPanel alertsContent = (JPanel) ((JScrollPane) topAlertPanel.getComponent(0)).getViewport().getView();
        alertsContent.removeAll();

        // 最多显示3条最新预警
        int count = 0;
        for (Alert alert : alerts) {
            if (alert != null && count < 3) {
                JPanel alertPanel = createAlertPanel(alert);
                alertsContent.add(alertPanel);
                alertsContent.add(Box.createVerticalStrut(10));
                count++;
            }
        }

        // 如果预警超过3条，显示剩余预警数量
        int nonNullAlertsCount = (int) alerts.stream().filter(Objects::nonNull).count();
        if (nonNullAlertsCount > 3) {
            JLabel remainingLabel = new JLabel("+" + (nonNullAlertsCount - 3) + " more alerts");
            remainingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            remainingLabel.setForeground(ERROR_COLOR);
            alertsContent.add(remainingLabel);
        }

        alertsContent.revalidate();
        alertsContent.repaint();
    }

    /**
     * 创建单个预警面板
     */
    private JPanel createAlertPanel(Alert alert) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(CARD_BG_COLOR);
        panel.setBorder(BorderFactory.createLineBorder(ERROR_COLOR));

        // 警告图标
        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        iconLabel.setForeground(ERROR_COLOR);
        panel.add(iconLabel);

        // 预警文本
        JLabel messageLabel = new JLabel(alert.getMessage());
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(ERROR_COLOR);
        panel.add(messageLabel);

        // 日期
        JLabel dateLabel = new JLabel(alert.getTimestamp().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(TEXT_COLOR);
        panel.add(dateLabel);

        return panel;
    }

    /**
     * 查看全部警报
     */
    private void showAllAlerts() {
        // 实现查看全部警报的逻辑
        List<Alert> alerts = checkAlerts();
        JTextArea textArea = new JTextArea();
        for (Alert alert : alerts) {
            if (alert != null) {
                textArea.append(alert.getTimestamp().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " - " + alert.getMessage() + "\n");
            }
        }
        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(this, scrollPane, "All Alerts", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 显示历史警报
     */
    private void showHistoryAlarms() {
        List<Alert> allAlerts = alertService.getAllAlerts();
        JTextArea textArea = new JTextArea();
        for (Alert alert : allAlerts) {
            textArea.append(alert.getTimestamp().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " - " + alert.getMessage() + "\n");
        }
        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(this, scrollPane, "History Alarms", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 保存设置
     */
    private void saveSettings() {
        // 保存全局设置
        alertsEnabled = enableAlertsCheckbox.isSelected();
        try {
            globalThreshold = Double.parseDouble(globalThresholdField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid global threshold value", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 保存类别阈值设置
        categoryThresholds.clear();
        for (int i = 0; i < categoryThresholdsModel.getRowCount(); i++) {
            String category = (String) categoryThresholdsModel.getValueAt(i, 0);
            try {
                double threshold = Double.parseDouble((String) categoryThresholdsModel.getValueAt(i, 1));
                categoryThresholds.put(category, threshold);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid threshold value for " + category, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // 保存设置后自动更新预警显示
        updateExpenseAlerts();
    }

    /**
     * 添加类别
     */
    private void addCategory() {
        String category = JOptionPane.showInputDialog(this, "Enter category name:");
        if (category != null && !category.isEmpty()) {
            categoryThresholdsModel.addRow(new Object[]{category, 0.0});
        }
    }

    /**
     * 删除类别
     */
    private void removeCategory() {
        int selectedRow = categoryThresholdsTable.getSelectedRow();
        if (selectedRow != -1) {
            categoryThresholdsModel.removeRow(selectedRow);
        }
    }

    /**
     * 检测预警
     */
    private List<Alert> checkAlerts() {
        List<Alert> alerts = new ArrayList<>();

        // 检测总支出是否超过历史平均值的20%
        double totalExpense = getTotalExpense();
        double historicalAverage = getHistoricalAverage();
        if (totalExpense > historicalAverage * 1.2) {
            String message = "Total expense (" + totalExpense + ") exceeds historical average (" + historicalAverage + ") by 20%!";
            alerts.add(alertService.createAlert(message, AlertLevel.CRITICAL, "Total Expense"));
        }

        // 检测各类别支出是否超过历史平均值的20%
        Map<String, Double> categoryExpenses = getCategoryExpenses();
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            String category = entry.getKey();
            double expense = entry.getValue();
            double categoryHistoricalAverage = getCategoryHistoricalAverage(category);
            if (expense > categoryHistoricalAverage * 1.2) {
                String message = "Category " + category + " expense (" + expense + ") exceeds historical average (" + categoryHistoricalAverage + ") by 20%!";
                alerts.add(alertService.createAlert(message, AlertLevel.CRITICAL, category));
            }
        }

        // 检测支出是否超过设定的阈值
        if (totalExpense > globalThreshold) {
            String message = "Total expense (" + totalExpense + ") exceeds global threshold (" + globalThreshold + ")!";
            alerts.add(alertService.createAlert(message, AlertLevel.CRITICAL, "Total Expense"));
        }
        for (Map.Entry<String, Double> entry : categoryThresholds.entrySet()) {
            String category = entry.getKey();
            double threshold = entry.getValue();
            double expense = categoryExpenses.getOrDefault(category, 0.0);
            if (expense > threshold) {
                String message = "Category " + category + " expense (" + expense + ") exceeds threshold (" + threshold + ")!";
                alerts.add(alertService.createAlert(message, AlertLevel.CRITICAL, category));
            }
        }

        return alerts;
    }

    /**
     * 获取总支出
     * 借助 transactionController 获取所有交易，然后对交易金额求和得出总支出。
     * @return 总支出
     */
    private double getTotalExpense() {
        List<Transaction> transactions = transactionController.getTransactions();
        return transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /**
     * 获取历史平均支出
     * 利用 transactionController 获取所有交易，计算这些交易金额的平均值得到历史平均支出。
     * @return 历史平均支出
     */
    private double getHistoricalAverage() {
        List<Transaction> transactions = transactionController.getTransactions();
        if (transactions.isEmpty()) {
            return 0;
        }
        return transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .average()
                .orElse(0);
    }

    /**
     * 获取各类别的支出
     * 通过 transactionController 获取所有交易，按交易类别分组并对金额求和，得到各类别的支出。
     * @return 各类别的支出
     */
    private Map<String, Double> getCategoryExpenses() {
        List<Transaction> transactions = transactionController.getTransactions();
        return transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)));
    }

    /**
     * 获取某类别的历史平均支出
     * 依据传入的类别，从 transactionController 获取所有交易中该类别的交易，计算这些交易金额的平均值得到该类别的历史平均支出。
     * @param category 类别
     * @return 某类别的历史平均支出
     */
    private double getCategoryHistoricalAverage(String category) {
        List<Transaction> transactions = transactionController.getTransactions();
        List<Transaction> categoryTransactions = transactions.stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
        if (categoryTransactions.isEmpty()) {
            return 0;
        }
        return categoryTransactions.stream()
                .mapToDouble(Transaction::getAmount)
                .average()
                .orElse(0);
    }

    /**
     * 加载设置
     * 此方法可用于从配置文件或数据库加载设置，这里简单示例从配置文件加载
     */
    private void loadSettings() {
        try {
            // 假设使用 Properties 读取配置文件
            Properties properties = new Properties();
            properties.load(getClass().getResourceAsStream("/config.properties"));

            // 加载全局阈值
            String globalThresholdStr = properties.getProperty("globalThreshold");
            if (globalThresholdStr != null) {
                globalThreshold = Double.parseDouble(globalThresholdStr);
                globalThresholdField.setText(String.valueOf(globalThreshold));
            }

            // 加载类别阈值
            categoryThresholds.clear();
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith("categoryThreshold.")) {
                    String category = key.substring("categoryThreshold.".length());
                    double threshold = Double.parseDouble(properties.getProperty(key));
                    categoryThresholds.put(category, threshold);
                }
            }

            // 重新填充表格数据
            categoryThresholdsModel.setRowCount(0);
            for (Map.Entry<String, Double> entry : categoryThresholds.entrySet()) {
                categoryThresholdsModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}