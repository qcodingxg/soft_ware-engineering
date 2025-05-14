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
    private final AlertService alertService;

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

        // 顶部警报显示区
        topAlertPanel = createTopAlertPanel();
        add(topAlertPanel, BorderLayout.NORTH);

        // 底部设置区
        bottomSettingsPanel = createBottomSettingsPanel();
        add(bottomSettingsPanel, BorderLayout.CENTER);

        // 右上角刷新按钮
        refreshButton = createStyledButton("Refresh Alerts");
        refreshButton.addActionListener(e -> updateExpenseAlerts());
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshPanel.setOpaque(false);
        refreshPanel.add(refreshButton);

        // 新增历史警报按钮
        historyAlarmsButton = createStyledButton("History Alarms");
        historyAlarmsButton.addActionListener(e -> showHistoryAlarms());
        refreshPanel.add(historyAlarmsButton);

        // 新增Details按钮
        detailsButton = createStyledButton("Details");
        detailsButton.addActionListener(e -> showDetails());
        refreshPanel.add(detailsButton);

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
        viewAllAlertsButton = createStyledButton("View All Alerts");
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
        saveSettingsButton = createStyledButton("Save Settings");
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
        enableAlertsCheckbox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        enableAlertsCheckbox.setOpaque(false);
        panel.add(enableAlertsCheckbox);

        // 全局支出预警阈值设置
        JLabel globalThresholdLabel = new JLabel("Global Threshold (¥):");
        globalThresholdLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(globalThresholdLabel);

        globalThresholdField = new JTextField(String.valueOf(globalThreshold), 10);
        globalThresholdField.setFont(new Font("SansSerif", Font.PLAIN, 14));
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
        categoryThresholdsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        categoryThresholdsTable.getTableHeader().setBackground(SECONDARY_COLOR);
        categoryThresholdsTable.getTableHeader().setForeground(Color.WHITE);
        categoryThresholdsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column widths
        categoryThresholdsTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Category
        categoryThresholdsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Threshold

        // 设置单元格居中显示
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        categoryThresholdsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        categoryThresholdsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(categoryThresholdsTable);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.add(scrollPane, BorderLayout.CENTER);

        // 添加和删除类别按钮
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
            remainingLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
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
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 24));
        iconLabel.setForeground(ERROR_COLOR);
        panel.add(iconLabel);

        // 预警文本
        JLabel messageLabel = new JLabel(alert.getMessage());
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageLabel.setForeground(ERROR_COLOR);
        panel.add(messageLabel);

        // 日期
        JLabel dateLabel = new JLabel(alert.getTimestamp().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
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

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "All Alerts", false);
        dialog.add(scrollPane);

        // 设置对话框宽度为内容的最佳宽度，高度固定
        dialog.setSize(scrollPane.getPreferredSize().width + 50, 300);
        dialog.setResizable(false); // 禁止调整宽度

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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

        // 设置对话框宽度为内容的最佳宽度，高度固定
        dialog.setSize(scrollPane.getPreferredSize().width + 50, 300);
        dialog.setResizable(false); // 禁止调整宽度

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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

        // 更新categoryThresholds映射
        for (int i = 0; i < categoryThresholdsModel.getRowCount(); i++) {
            String category = (String) categoryThresholdsModel.getValueAt(i, 0);
            Object thresholdObj = categoryThresholdsModel.getValueAt(i, 1);

            // 处理未设置的阈值
            if ("未设置".equals(thresholdObj) || thresholdObj == null || thresholdObj.toString().trim().isEmpty()) {
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

        // 保存到文件
        saveCategoryThresholds();

        // 刷新警报
        updateExpenseAlerts();

        // 显示保存成功
        JOptionPane.showMessageDialog(this,
                "Settings saved successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 添加类别
     */
    private void addCategory() {
        List<Transaction> transactions = transactionController.getTransactions();
        Set<String> allCategories = transactions.stream()
                .map(Transaction::getCategory)
                .filter(Objects::nonNull)
                .filter(category -> !category.isEmpty())
                .collect(Collectors.toSet());

        // 排除已经添加过的类别
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
                // 为每个选中的类别设置阈值
                for (String category : selectedCategories) {
                    // 弹出对话框设置阈值
                    String input = JOptionPane.showInputDialog(
                            this,
                            "Set threshold for category '" + category + "':",
                            "Set Threshold",
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (input != null) { // 用户点击了OK
                        input = input.trim();
                        if (input.isEmpty()) {
                            categoryThresholds.put(category, null); // 未设置阈值
                        } else {
                            try {
                                double threshold = Double.parseDouble(input);
                                // 保留两位小数
                                threshold = Math.round(threshold * 100.0) / 100.0;
                                categoryThresholds.put(category, threshold);
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(
                                        this,
                                        "Invalid threshold value. Using 0.00 as default.",
                                        "Input Error",
                                        JOptionPane.WARNING_MESSAGE
                                );
                                categoryThresholds.put(category, 0.0);
                            }
                        }
                    } else { // 用户点击了Cancel
                        categoryThresholds.put(category, null); // 未设置阈值
                    }
                }

                // 更新阈值表格
                updateThresholdTable();

                // 提示用户保存设置
                JOptionPane.showMessageDialog(this,
                        "Categories added. Please save settings to apply changes.",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * 删除类别
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
     * 检测预警
     */
    private List<Alert> checkAlerts() {
        List<Alert> alerts = new ArrayList<>();

        // 检测总支出是否超过历史平均值的20%
        double totalExpense = getCurrentMonthExpense();
        double historicalAverage = getHistoricalMonthlyAverageExcludingCurrentMonth();
        if (alertsEnabled && totalExpense > historicalAverage * 1.2) {
            // 使用String.format确保两位小数
            String message = "Current month expense (" + String.format("%.2f", totalExpense) +
                    ") exceeds historical monthly average (" + String.format("%.2f", historicalAverage) +
                    ") by over 20%!";
            alerts.add(alertService.createAlert(message, AlertLevel.CRITICAL, "Total Expense"));
        }

        // 检测支出是否超过设定的阈值
        if (alertsEnabled && totalExpense > globalThreshold) {
            // 使用String.format确保两位小数
            String message = "Total expense (" + String.format("%.2f", totalExpense) +
                    ") exceeds global threshold (" + String.format("%.2f", globalThreshold) + ")!";
            alerts.add(alertService.createAlert(message, AlertLevel.CRITICAL, "Total Expense"));
        }

        // 检查类别阈值
        for (Map.Entry<String, Double> entry : categoryThresholds.entrySet()) {
            String category = entry.getKey();
            Double threshold = entry.getValue();

            // 跳过未设置阈值的类别
            if (threshold == null || threshold <= 0) continue;

            double expense = getCategoryExpensesCurrentMonth().getOrDefault(category, 0.0);
            if (expense > threshold) {
                // 使用String.format确保两位小数
                String message = "Expense for category " + category + " (" +
                        String.format("%.2f", expense) + ") exceeds the threshold (" +
                        String.format("%.2f", threshold) + ")!";
                alerts.add(alertService.createAlert(message, AlertLevel.CRITICAL, category));
            }
        }

        return alerts;
    }

    /**
     * 获取当前月的总支出
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
     * 获取历史月度平均支出，不包含当前月
     */
    private double getHistoricalMonthlyAverageExcludingCurrentMonth() {
        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();
        int currentYear = currentDate.getYear();

        List<Transaction> transactions = transactionController.getTransactions();

        // 过滤掉当前月的交易
        List<Transaction> historicalTransactions = transactions.stream()
                .filter(t -> !(t.getDate().getMonth() == currentMonth && t.getDate().getYear() == currentYear))
                .collect(Collectors.toList());

        if (historicalTransactions.isEmpty()) {
            return 0;
        }

        // 计算每个月的总支出
        Map<String, Double> monthlyExpenses = new HashMap<>();
        for (Transaction transaction : historicalTransactions) {
            LocalDate date = transaction.getDate();
            String monthKey = date.getYear() + "-" + date.getMonthValue();
            monthlyExpenses.put(monthKey, monthlyExpenses.getOrDefault(monthKey, 0.0) + transaction.getAmount());
        }

        // 计算月度平均支出
        if (monthlyExpenses.isEmpty()) {
            return 0;
        }
        return monthlyExpenses.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    /**
     * 获取当前月各类别的支出
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
     * 加载设置
     */
    private void loadSettings() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
            properties.load(input);

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

        } catch (IOException | NumberFormatException e) {
            // 如果文件不存在或格式错误，使用默认设置
            e.printStackTrace();
        }
    }

    /**
     * 保存类别阈值设置
     */
    private void saveCategoryThresholds() {
        Properties properties = new Properties();

        // 格式化全局阈值
        properties.setProperty("globalThreshold", String.format("%.2f", globalThreshold));
        properties.setProperty("alertsEnabled", String.valueOf(alertsEnabled));

        // 格式化类别阈值
        for (Map.Entry<String, Double> entry : categoryThresholds.entrySet()) {
            String category = entry.getKey();
            Double threshold = entry.getValue();

            // 确保保存为两位小数格式
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
     * 加载类别阈值
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

                    // 处理未设置的阈值
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

            // 更新表格显示
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

            // 直接在显示时格式化
            String displayValue = threshold != null ?
                    String.format("%.2f", threshold) : "Not Set";
            categoryThresholdsModel.addRow(new Object[]{category, displayValue});
        }
    }

    /**
     * 创建美化后的按钮
     * @param text 按钮文本
     * @return 美化后的按钮
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(SECONDARY_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        return button;
    }

    /**
     * 显示详细信息
     */
    private void showDetails() {
        Map<String, Double> categoryExpenses = getCategoryExpensesCurrentMonth();
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Category", "Threshold", "Actual Value", "Exceeded Amount"}, 0);

        for (Map.Entry<String, Double> entry : categoryThresholds.entrySet()) {
            String category = entry.getKey();
            Double threshold = entry.getValue();
            double actualValue = categoryExpenses.getOrDefault(category, 0.0);

            // 直接格式化所有显示值
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

        // 设置对话框宽度为内容的最佳宽度，高度固定
        dialog.setSize(scrollPane.getPreferredSize().width + 50, 400);
        dialog.setResizable(false); // 禁止调整宽度

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
