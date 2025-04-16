package com.financeapp.view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.json.JSONArray;

public class AIChatPanel extends JPanel {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private static final String API_KEY = "sk-92f9dba0310242988bafce610d4664be"; // 请替换为您的API密钥
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String SYSTEM_PROMPT = "You are a professional personal financial advisor named 'Cai Zhi Zhu Shou'. Your expertise includes: " +
            "1. Personal Budget Planning and Expense Tracking\n" +
            "2. Debt Management and Repayment Strategy\n" +
            "3. Savings Goal Setting and Achieving Method\n" +
            "4. Basic Investment Advice (Stocks, Funds, Fixed Deposits, etc.)\n" +
            "5. Tax Planning and Optimization\n" +
            "6. Retirement Planning\n" +
            "7. Insurance Configuration Advice\n\n" +
            "When answering user questions, please follow these principles: \n" +
            "- Use professional and easy-to-understand language\n" +
            "- Provide specific and actionable advice\n" +
            "- Explain financial concepts using simple analogies\n" +
            "- Respect the user's financial situation, do not make excessive assumptions\n" +
            "- Encourage healthy financial habits and long-term planning\n" +
            "- Remind users that important financial decisions should be consulted with professionals\n" +
            "- All answers are in English\n\n" +
            "When users ask non-financial questions, politely guide the conversation back to the financial field.";
    
    // 用于存储当前的回复内容
    private StringBuilder currentResponse;
    
    // 交易数据文件路径
    private static final String TRANSACTIONS_CSV_PATH = "data/transactions.csv";
    
    // 存储交易数据内容
    private String transactionsData;
    
    // 界面颜色定义
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185); // 深蓝色
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219); // 蓝色
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241); // 浅灰色
    private static final Color TEXT_COLOR = new Color(44, 62, 80); // 深灰色
    private static final Color USER_BUBBLE_COLOR = new Color(230, 230, 230); // 浅灰色
    private static final Color BOT_BUBBLE_COLOR = new Color(212, 230, 241); // 浅蓝色
    private static final Color ERROR_COLOR = new Color(231, 76, 60); // 红色
    private static final Color HINT_COLOR = new Color(189, 195, 199); // 中灰色
    
    // 进度条组件
    private JProgressBar progressBar;
    private Timer progressTimer;
    private JPanel progressPanel;
    private JLabel progressLabel;

    public AIChatPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);

        // 创建聊天区域
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(BACKGROUND_COLOR);
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(Color.WHITE);
        chatArea.setForeground(TEXT_COLOR);
        chatArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        add(chatPanel, BorderLayout.CENTER);

        // 创建输入区域
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BACKGROUND_COLOR);
        inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        inputField = new JTextField();
        inputField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        inputField.setBackground(Color.WHITE);
        inputField.setForeground(TEXT_COLOR);
        
        sendButton = createStyledButton("Send");
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
        
        // 创建进度条面板(开始时不可见)
        progressPanel = createProgressPanel();
        progressPanel.setVisible(false);
        add(progressPanel, BorderLayout.SOUTH);

        // 添加发送按钮事件监听
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // 添加回车键发送功能
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        // 为输入框添加提示文本
        inputField.putClientProperty("JTextField.placeholderText", "Please enter your financial questions...");
        
        // 初始化当前回复
        currentResponse = new StringBuilder();
        
        // 加载交易数据
        loadTransactionsData();
        
        // 添加初始化消息
        chatArea.append("AI Financial Advisor: Hello! I'm your personal financial advisor. Whether it's budget planning, savings goals, investment advice, or debt management, I can provide professional guidance. What financial questions can I help you with?\n\n");
        
        // 发送初始交易数据到AI
        if (transactionsData != null && !transactionsData.isEmpty()) {
            sendTransactionsData();
        }
    }
    
    /**
     * 创建进度条面板
     */
    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(5, SECONDARY_COLOR),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // 创建进度条标签
        progressLabel = new JLabel("Analyzing your transaction data... 📊");
        progressLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        progressLabel.setForeground(TEXT_COLOR);
        
        // 创建进度条
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setForeground(SECONDARY_COLOR);
        progressBar.setBackground(BACKGROUND_COLOR);
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(250, 15));
        
        // 创建包含进度条和文本的面板
        JPanel progressContentPanel = new JPanel(new BorderLayout(10, 5));
        progressContentPanel.setOpaque(false);
        progressContentPanel.add(progressLabel, BorderLayout.NORTH);
        progressContentPanel.add(progressBar, BorderLayout.CENTER);
        
        panel.add(progressContentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 启动进度条动画
     */
    private void startProgressBar() {
        progressBar.setValue(0);
        
        // 切换可见性：隐藏输入面板，显示进度条面板
        remove(getComponent(2)); // 移除输入面板
        add(progressPanel, BorderLayout.SOUTH);
        progressPanel.setVisible(true);
        revalidate();
        repaint();
        
        // 创建定时器，更新进度条
        progressTimer = new Timer(80, new ActionListener() {
            private int progress = 0;
            private boolean forward = true;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (forward) {
                    progress += 2;
                    if (progress >= 90) {
                        // 进度达到90%后放慢速度
                        forward = false;
                    }
                } else {
                    // 模拟处理波动
                    progress += (Math.random() > 0.7) ? 1 : 0;
                }
                
                progressBar.setValue(Math.min(progress, 95)); // 最大进度为95%，完成时设为100%
            }
        });
        
        progressTimer.start();
    }
    
    /**
     * 停止进度条动画
     */
    private void stopProgressBar() {
        if (progressTimer != null) {
            progressTimer.stop();
            progressBar.setValue(100); // 设置为100%表示完成
            
            // 短暂延迟后切换回输入面板
            Timer completionTimer = new Timer(500, e -> {
                progressPanel.setVisible(false);
                remove(progressPanel);
                
                // 重新添加输入面板
                JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
                inputPanel.setBackground(BACKGROUND_COLOR);
                inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
                inputPanel.add(inputField, BorderLayout.CENTER);
                inputPanel.add(sendButton, BorderLayout.EAST);
                add(inputPanel, BorderLayout.SOUTH);
                
                revalidate();
                repaint();
            });
            completionTimer.setRepeats(false);
            completionTimer.start();
        }
    }
    
    /**
     * 创建标题面板
     */
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // 创建标题标签
        JLabel titleLabel = new JLabel("AI Financial Advisor");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.WEST);
        
        // 创建状态标签
        JLabel statusLabel = new JLabel("Online");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        statusLabel.setForeground(Color.WHITE);
        panel.add(statusLabel, BorderLayout.EAST);
        
        // 设置圆角
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(5, PRIMARY_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        return panel;
    }
    
    /**
     * 创建样式化按钮
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(80, 36));
        
        // 设置圆角
        button.setBorder(new RoundedBorder(5, PRIMARY_COLOR));
        
        return button;
    }
    
    /**
     * 自定义圆角边框
     */
    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        
        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius, this.radius, this.radius, this.radius);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = this.radius;
            return insets;
        }
    }
    
    /**
     * 加载交易数据从CSV文件
     */
    private void loadTransactionsData() {
        try {
            // 从项目根目录读取文件
            File file = new File(TRANSACTIONS_CSV_PATH);
            if (!file.exists()) {
                // 尝试从绝对路径读取
                String absolutePath = new File("").getAbsolutePath();
                file = new File(absolutePath + File.separator + TRANSACTIONS_CSV_PATH);
                
                if (!file.exists()) {
                    System.err.println("交易数据文件未找到: " + TRANSACTIONS_CSV_PATH);
                    return;
                }
            }
            
            // 读取文件内容
            transactionsData = Files.readAllLines(Paths.get(file.getAbsolutePath()))
                .stream()
                .collect(Collectors.joining("\n"));
                
            System.out.println("成功加载交易数据");
            
        } catch (Exception e) {
            System.err.println("加载交易数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 发送交易数据到AI作为上下文
     */
    private void sendTransactionsData() {
        new Thread(() -> {
            try {
                // 创建提示词，要求AI基于交易数据生成财务建议
                String message = "Please analyze the user's financial situation based on the following transaction data and provide 3-5 specific financial advice. These data are the user's latest transaction records. Please ensure your response includes the following parts: 1. A brief summary of the user's financial situation; 2. 3-5 specific and targeted financial advice. The format should be concise and clear.\n\n" + 
                                "Transaction data (CSV format):\n" + transactionsData;
                
                // 禁用输入框直到初始分析完成
                setInputEnabled(false);
                
                // 启动进度条动画
                SwingUtilities.invokeLater(() -> startProgressBar());
                
                // 获取AI响应（使用完整响应而非流式输出，避免显示处理过程）
                String response = getAIResponse(message);
                
                // 将分析结果存储到系统内，并显示给用户
                System.out.println("AI has analyzed the transaction data and prepared advice for the user's situation");
                
                // 停止进度条并显示结果
                SwingUtilities.invokeLater(() -> {
                    // 停止进度条动画
                    stopProgressBar();
                    
                    // 显示分析结果和财务建议
                    chatArea.append("AI Financial Advisor: Based on your transaction data, I have prepared the following financial analysis and advice:\n\n");
                    chatArea.append(response);
                    chatArea.append("\n\nWhat specific financial questions do you need help with?\n\n");
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                });
                
                // 启用输入框
                setInputEnabled(true);
                
            } catch (Exception e) {
                System.err.println("发送交易数据失败: " + e.getMessage());
                e.printStackTrace();
                
                // 停止进度条并恢复输入
                SwingUtilities.invokeLater(() -> {
                    stopProgressBar();
                    setInputEnabled(true);
                    chatArea.append("AI Financial Advisor: Sorry, there was an issue analyzing your transaction data. Please try again later.\n\n");
                });
            }
        }).start();
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            // 显示用户消息
            appendMessage("User: " + message);
            inputField.setText("");
            
            // 禁用输入框和发送按钮，直到回复完成
            setInputEnabled(false);
            
            // 添加AI助手标签但不换行，以便后续流式显示内容
            appendMessageWithoutNewline("AI Financial Advisor: ");
            
            // 重置当前回复
            currentResponse = new StringBuilder();

            // 发送到API并获取响应
            new Thread(() -> {
                try {
                    getAIResponseStreaming(message);
                    // 回复完成后添加额外的换行
                    appendNewline();
                } catch (Exception e) {
                    appendMessageFromThread("Error: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // 无论是否发生错误，都重新启用输入
                    SwingUtilities.invokeLater(() -> setInputEnabled(true));
                }
            }).start();
        }
    }
    
    private void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("User:")) {
                // 为用户消息应用边框样式
                String userMsg = message.substring(5).trim();
                StringBuilder formattedMessage = new StringBuilder();
                
                // 添加用户消息边框
                formattedMessage.append("┏━━━━━━━━━━━━━━━━━━ User Question ━━━━━━━━━━━━━━━━━━┓\n");
                
                // 分行处理用户消息
                String[] lines = userMsg.split("\n");
                for (String line : lines) {
                    // 处理长行，进行换行
                    if (line.length() > 40) {
                        int i = 0;
                        while (i < line.length()) {
                            int end = Math.min(i + 40, line.length());
                            if (end < line.length() && Character.isLetterOrDigit(line.charAt(end))) {
                                int lastSpace = line.lastIndexOf(' ', end);
                                if (lastSpace > i) {
                                    end = lastSpace;
                                }
                            }
                            formattedMessage.append("┃  ").append(line.substring(i, end))
                                           .append(new String(new char[40 - (end - i)]).replace('\0', ' '))
                                           .append("  ┃\n");
                            i = end;
                            if (i < line.length() && line.charAt(i) == ' ') {
                                i++; // 跳过空格
                            }
                        }
                    } else {
                        // 短行直接添加
                        formattedMessage.append("┃  ").append(line)
                                       .append(new String(new char[40 - line.length()]).replace('\0', ' '))
                                       .append("  ┃\n");
                    }
                }
                
                // 添加底部边框
                formattedMessage.append("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛\n\n");
                
                chatArea.append(formattedMessage.toString());
            } else {
                // 这里不处理AI回复，因为AI回复会在appendMessageWithoutNewline和appendStreamContent中处理
                chatArea.append(message + "\n\n");
            }
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private void appendMessageWithoutNewline(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("AI Financial Advisor:")) {
                // 添加AI回复的顶部边框
                chatArea.append("┏━━━━━━━━━━━━━━━━━━ AI Response ━━━━━━━━━━━━━━━━━━┓\n");
                chatArea.append("┃                                            ┃\n");
                // 不显示前缀"AI Financial Advisor:"，而是直接在边框内显示内容
            } else {
                chatArea.append(message);
            }
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private void appendNewline() {
        SwingUtilities.invokeLater(() -> {
            // 添加AI回复的底部边框
            chatArea.append("┃                                            ┃\n");
            chatArea.append("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛\n\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private void appendMessageFromThread(String message) {
        SwingUtilities.invokeLater(() -> {
            // 为错误消息应用不同样式
            if (message.startsWith("Error:")) {
                chatArea.append(message + "\n\n");
            } else {
                // 添加带有美观边框的财务建议
                if (message.contains("financial advice") || message.contains("财务建议")) {
                    String[] parts = message.split("\n");
                    StringBuilder formattedMessage = new StringBuilder();
                    
                    // 添加顶部边框
                    formattedMessage.append("┏━━━━━━━━━━━━━━━━━━ Financial Advice ━━━━━━━━━━━━━━━━━━┓\n");
                    
                    // 添加内容，每行前后加上边框
                    for (String line : parts) {
                        formattedMessage.append("┃  ").append(line).append("  ┃\n");
                    }
                    
                    // 添加底部边框
                    formattedMessage.append("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛\n\n");
                    
                    chatArea.append(formattedMessage.toString());
                } else {
                    chatArea.append(message + "\n\n");
                }
            }
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private void appendStreamContent(String content) {
        SwingUtilities.invokeLater(() -> {
            // 如果内容中包含财务建议关键词，做特殊处理
            if (content.contains("financial advice") || content.contains("财务建议")) {
                // 检查当前累积的响应是否已经包含完整的财务建议
                if (isCompleteAdviceBlock(currentResponse.toString() + content)) {
                    // 完整的建议块，应用美化格式
                    formatAndDisplayFinancialAdvice(currentResponse.toString() + content);
                } else {
                    // 仍在累积中，正常追加
                    appendFormattedContent(content);
                }
            } else {
                // 普通内容，正常追加但添加边框格式
                appendFormattedContent(content);
            }
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    /**
     * 判断是否是完整的财务建议块
     */
    private boolean isCompleteAdviceBlock(String text) {
        // 简单判断：如果包含建议序号（如"1."，"2."等）并且最后一个建议后面有完整的句子，则认为是完整的建议块
        return (text.contains("1.") && text.contains("2.") && 
                (text.endsWith(".") || text.endsWith("!") || text.endsWith("?")));
    }
    
    /**
     * 格式化并显示财务建议
     */
    private void formatAndDisplayFinancialAdvice(String advice) {
        // 清除当前已显示的内容
        int lastAdviceStart = chatArea.getText().lastIndexOf("┏━━━━━━━━━━━━━━━━━━ AI回复 ━━━━━━━━━━━━━━━━━━━┓");
        if (lastAdviceStart < 0) {
            lastAdviceStart = chatArea.getText().lastIndexOf("┏━━━━━━━━━━━━━━━━━━ AI Response ━━━━━━━━━━━━━━━━━━┓");
        }
        if (lastAdviceStart >= 0) {
            try {
                chatArea.getDocument().remove(lastAdviceStart, chatArea.getDocument().getLength() - lastAdviceStart);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 重新添加AI回复边框顶部
        chatArea.append("┏━━━━━━━━━━━━━━━━━━ AI Response ━━━━━━━━━━━━━━━━━━┓\n");
        chatArea.append("┃                                            ┃\n");
        
        // 提取建议部分
        String[] paragraphs = advice.split("\n\n");
        StringBuilder formattedAdvice = new StringBuilder();
        
        // 添加常规回复内容（除了财务建议部分）
        boolean foundAdvice = false;
        for (String paragraph : paragraphs) {
            if (paragraph.contains("financial advice") || paragraph.contains("财务建议") ||
                (paragraph.contains("1.") && paragraph.contains("2."))) {
                foundAdvice = true;
                break;
            }
            
            // 为常规内容添加边框格式
            String[] lines = paragraph.split("\n");
            for (String line : lines) {
                // 确保行不超过边框宽度，如果超过则换行
                if (line.length() > 40) {
                    // 简单的文本换行处理
                    int i = 0;
                    while (i < line.length()) {
                        int end = Math.min(i + 40, line.length());
                        if (end < line.length() && Character.isLetterOrDigit(line.charAt(end))) {
                            // 查找上一个空格
                            int lastSpace = line.lastIndexOf(' ', end);
                            if (lastSpace > i) {
                                end = lastSpace;
                            }
                        }
                        formattedAdvice.append("┃  ").append(line.substring(i, end)).append(new String(new char[40 - (end - i)]).replace('\0', ' ')).append("  ┃\n");
                        i = end;
                        if (i < line.length() && line.charAt(i) == ' ') {
                            i++; // 跳过空格
                        }
                    }
                } else {
                    // 短行，直接添加并填充空格
                    formattedAdvice.append("┃  ").append(line).append(new String(new char[40 - line.length()]).replace('\0', ' ')).append("  ┃\n");
                }
            }
            // 段落之间添加空行
            formattedAdvice.append("┃                                            ┃\n");
        }
        
        // 显示常规回复内容
        chatArea.append(formattedAdvice.toString());
        
        // 添加美化的财务建议框
        chatArea.append("┃                                            ┃\n");
        chatArea.append("┃  ┏━━━━━━━━━━━━ Financial Advice ━━━━━━━━━━━━┓      ┃\n");
        
        // 查找并格式化财务建议部分
        boolean hasAdvice = false;
        StringBuilder adviceContent = new StringBuilder();
        
        for (String paragraph : paragraphs) {
            if (paragraph.contains("financial advice") || paragraph.contains("财务建议") ||
                (paragraph.contains("1.") && paragraph.contains("2."))) {
                hasAdvice = true;
                
                // 处理建议内容
                String[] lines = paragraph.split("\n");
                for (String line : lines) {
                    // 跳过标题行
                    if (line.contains("financial advice") || line.contains("财务建议")) {
                        continue;
                    }
                    
                    // 确保行不超过建议框宽度，如果超过则换行
                    if (line.length() > 32) { // 缩小宽度以适应嵌套边框
                        // 简单的文本换行处理
                        int i = 0;
                        while (i < line.length()) {
                            int end = Math.min(i + 32, line.length());
                            if (end < line.length() && Character.isLetterOrDigit(line.charAt(end))) {
                                // 查找上一个空格
                                int lastSpace = line.lastIndexOf(' ', end);
                                if (lastSpace > i) {
                                    end = lastSpace;
                                }
                            }
                            adviceContent.append("┃  ┃  ").append(line.substring(i, end))
                                   .append(new String(new char[32 - (end - i)]).replace('\0', ' '))
                                   .append("  ┃  ┃\n");
                            i = end;
                            if (i < line.length() && line.charAt(i) == ' ') {
                                i++; // 跳过空格
                            }
                        }
                    } else {
                        // 短行，直接添加并填充空格
                        adviceContent.append("┃  ┃  ").append(line)
                               .append(new String(new char[32 - line.length()]).replace('\0', ' '))
                               .append("  ┃  ┃\n");
                    }
                }
                
                // 建议部分之间添加空行
                adviceContent.append("┃  ┃                                ┃  ┃\n");
            }
        }
        
        // 如果没有找到具体建议，添加默认建议
        if (!hasAdvice) {
            adviceContent.append("┃  ┃  No specific financial advice found    ┃  ┃\n");
        } else {
            // 移除最后一个建议部分的空行
            int lastEmptyLine = adviceContent.lastIndexOf("┃  ┃                                ┃  ┃\n");
            if (lastEmptyLine > 0) {
                adviceContent.delete(lastEmptyLine, lastEmptyLine + 45);
            }
        }
        
        // 添加建议内容
        chatArea.append(adviceContent.toString());
        
        // 添加建议框底部
        chatArea.append("┃  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛      ┃\n");
        
        // 可能的结束内容
        boolean hasEndingContent = false;
        StringBuilder endingContent = new StringBuilder();
        
        for (int i = paragraphs.length - 1; i >= 0; i--) {
            String paragraph = paragraphs[i];
            if (paragraph.contains("financial advice") || paragraph.contains("财务建议") ||
                (paragraph.contains("1.") && paragraph.contains("2."))) {
                break;
            }
            
            if (i == paragraphs.length - 1) {
                hasEndingContent = true;
                
                // 添加结束内容
                endingContent.append("┃                                            ┃\n");
                
                // 为结束内容添加边框格式
                String[] lines = paragraph.split("\n");
                for (String line : lines) {
                    // 确保行不超过边框宽度，如果超过则换行
                    if (line.length() > 40) {
                        // 简单的文本换行处理
                        int j = 0;
                        while (j < line.length()) {
                            int end = Math.min(j + 40, line.length());
                            if (end < line.length() && Character.isLetterOrDigit(line.charAt(end))) {
                                // 查找上一个空格
                                int lastSpace = line.lastIndexOf(' ', end);
                                if (lastSpace > j) {
                                    end = lastSpace;
                                }
                            }
                            endingContent.append("┃  ").append(line.substring(j, end))
                                   .append(new String(new char[40 - (end - j)]).replace('\0', ' '))
                                   .append("  ┃\n");
                            j = end;
                            if (j < line.length() && line.charAt(j) == ' ') {
                                j++; // 跳过空格
                            }
                        }
                    } else {
                        // 短行，直接添加并填充空格
                        endingContent.append("┃  ").append(line)
                               .append(new String(new char[40 - line.length()]).replace('\0', ' '))
                               .append("  ┃\n");
                    }
                }
            }
        }
        
        // 添加结束内容
        if (hasEndingContent) {
            chatArea.append(endingContent.toString());
        } else {
            // 如果没有结束内容，添加一个空行
            chatArea.append("┃                                            ┃\n");
        }
        
        // 添加AI回复底部边框
        chatArea.append("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛\n\n");
    }

    /**
     * 格式化追加内容，适应AI回复边框
     */
    private void appendFormattedContent(String content) {
        // 分行处理
        String[] lines = content.split("\n");
        StringBuilder formatted = new StringBuilder();
        
        for (String line : lines) {
            // 处理空行
            if (line.trim().isEmpty()) {
                formatted.append("┃                                            ┃\n");
                continue;
            }
            
            // 处理长行，进行换行
            if (line.length() > 40) {
                int i = 0;
                while (i < line.length()) {
                    int end = Math.min(i + 40, line.length());
                    if (end < line.length() && Character.isLetterOrDigit(line.charAt(end))) {
                        int lastSpace = line.lastIndexOf(' ', end);
                        if (lastSpace > i) {
                            end = lastSpace;
                        }
                    }
                    formatted.append("┃  ").append(line.substring(i, end))
                             .append(new String(new char[40 - (end - i)]).replace('\0', ' '))
                             .append("  ┃\n");
                    i = end;
                    if (i < line.length() && line.charAt(i) == ' ') {
                        i++; // 跳过空格
                    }
                }
            } else {
                // 短行直接添加
                formatted.append("┃  ").append(line)
                         .append(new String(new char[40 - line.length()]).replace('\0', ' '))
                         .append("  ┃\n");
            }
        }
        
        chatArea.append(formatted.toString());
    }

    private void getAIResponseStreaming(String message) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setDoOutput(true);

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "deepseek-chat");
        
        // 创建消息数组
        JSONArray messagesArray = new JSONArray();
        
        // 添加系统提示词
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT + (transactionsData != null && !transactionsData.isEmpty() ? 
                "\n\nI have analyzed the user's transaction data, and I understand their financial situation. Please provide more targeted advice based on this information." : ""));
        messagesArray.put(systemMessage);
        
        // 添加用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messagesArray.put(userMessage);
        
        requestBody.put("messages", messagesArray);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);
        // 启用流式输出
        requestBody.put("stream", true);

        // 发送请求
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 读取流式响应
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6);
                    if (data.equals("[DONE]")) {
                        break;
                    }
                    
                    try {
                        JSONObject jsonResponse = new JSONObject(data);
                        JSONArray choices = jsonResponse.getJSONArray("choices");
                        if (choices.length() > 0) {
                            JSONObject choice = choices.getJSONObject(0);
                            JSONObject delta = choice.getJSONObject("delta");
                            if (delta.has("content")) {
                                String content = delta.getString("content");
                                // 累积响应
                                currentResponse.append(content);
                                // 实时显示增量内容
                                appendStreamContent(content);
                            }
                        }
                    } catch (Exception e) {
                        // 如果解析特定行失败，继续处理下一行
                        System.err.println("Cannot parse the stream data line: " + data);
                    }
                }
            }
        } catch (Exception e) {
            // 如果API调用失败，显示错误信息
            appendStreamContent("\nSorry, I cannot connect to the AI service right now. Please try again later. Error information: " + e.getMessage());
            throw e;
        }
    }

    // 保留原方法以支持非流式API调用（用于初始交易数据分析）
    private String getAIResponse(String message) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setDoOutput(true);

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "deepseek-chat");
        
        // 创建消息数组
        JSONArray messagesArray = new JSONArray();
        
        // 添加系统提示词
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);
        messagesArray.put(systemMessage);
        
        // 添加用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messagesArray.put(userMessage);
        
        requestBody.put("messages", messagesArray);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);

        // 发送请求
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 读取响应
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        } catch (Exception e) {
            // 如果API调用失败，返回一个友好的响应
            return "Sorry, I cannot connect to the AI service right now. Please try again later. Error information: " + e.getMessage();
        }

        try {
            // 解析响应
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (Exception e) {
            return "Sorry, I cannot understand the AI service response. Error information: " + e.getMessage();
        }
    }
} 