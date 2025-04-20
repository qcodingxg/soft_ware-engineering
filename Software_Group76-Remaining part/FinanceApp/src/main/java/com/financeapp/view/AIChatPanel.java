package com.financeapp.view;

import com.financeapp.service.AIChatService;
import com.financeapp.view.components.ChatBubbleFactory;
import com.financeapp.view.components.ChatBubbleFactory.RoundedBorder;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * AI聊天面板，支持与AI财务顾问进行对话
 */
public class AIChatPanel extends JPanel {
    private JPanel chatMessagesPanel; // 消息面板
    private JScrollPane scrollPane;   // 滚动面板
    private JTextField inputField;    // 输入框
    private JButton sendButton;       // 发送按钮
    
    // 界面颜色定义
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185); // 深蓝色
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219); // 蓝色
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241); // 浅灰色
    private static final Color TEXT_COLOR = new Color(44, 62, 80); // 深灰色
    private static final Color SUGGESTION_COLOR = new Color(245, 247, 249); // 建议按钮背景色
    private static final Color SUGGESTION_HOVER_COLOR = new Color(225, 235, 245); // 建议按钮悬停背景色
    
    // 进度条组件
    private JProgressBar progressBar;
    private Timer progressTimer;
    private JPanel progressPanel;
    private JLabel progressLabel;

    // 当前AI回复的消息面板
    private JTextArea currentAIMessageArea;
    // 存储所有消息面板的列表
    private List<Component> messageComponents = new ArrayList<>();

    // 用于存储当前的回复内容
    private StringBuilder currentResponse;
    
    // AI聊天服务
    private AIChatService chatService;
    
    // 推荐问题列表
    private static final String[] ALL_SUGGESTED_QUESTIONS = {
        "How to create a monthly budget?",
        "What's the best way to save for retirement?",
        "Should I invest in stocks or mutual funds?",
        "How much emergency fund should I have?",
        "What are the benefits of a high-yield savings account?",
        "How to reduce my credit card debt?",
        "What are the benefits of a high-yield savings account?",
        "How to reduce my credit card debt?",
        "What are the benefits of a high-yield savings account?",
        "How to reduce my credit card debt?",
        "What are the benefits of a high-yield savings account?",
        "How to reduce my credit card debt?",
        "What are the benefits of a high-yield savings account?",
        "How to reduce my credit card debt?",
        "How to start investing in finance?",
        "How to optimize my income tax?",
        "How to save for my child's education?",
        "How to do a good financial planning for young people?",
        "How to set a five-year financial goal?",
        "How to evaluate my insurance needs?",
        "How to manage multiple income sources?",
        "How to build a good credit record?",
        "What are the benefits of a high-yield savings account?",
        "How to avoid impulse consumption?",
        "How to do a good daily record?",
        "How to evaluate investment risks?",
        "How to plan my retirement savings?"
    };
    
    // 当前显示的推荐问题
    private String[] currentSuggestedQuestions;
    
    // 每次显示的建议问题数量
    private static final int SUGGESTION_DISPLAY_COUNT = 5;
    
    // 建议面板
    private JPanel suggestionsPanel;

    /**
     * 创建AI聊天面板
     */
    public AIChatPanel() {
        // 初始化聊天服务
        chatService = new AIChatService();
        
        // 设置面板基本属性
        setLayout(new BorderLayout(0, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        // 设置首选大小，使聊天面板更宽
        setPreferredSize(new Dimension(800, 600));

        // 创建标题面板
        JPanel titlePanel = ChatBubbleFactory.createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);

        // 创建聊天区域
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(BACKGROUND_COLOR);
        
        // 创建垂直消息面板
        chatMessagesPanel = new JPanel();
        chatMessagesPanel.setLayout(new BoxLayout(chatMessagesPanel, BoxLayout.Y_AXIS));
        chatMessagesPanel.setBackground(Color.WHITE);
        
        // 添加一些顶部间距
        chatMessagesPanel.add(Box.createVerticalStrut(10));
        
        // 创建滚动面板
        scrollPane = new JScrollPane(chatMessagesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        add(chatPanel, BorderLayout.CENTER);

        // 初始化当前显示的建议问题
        refreshSuggestions();
        
        // 创建底部面板，包括推荐问题和输入区
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setBackground(BACKGROUND_COLOR);
        bottomPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        // 创建推荐问题面板
        JPanel suggestionsPanel = createSuggestionsPanel();
        bottomPanel.add(suggestionsPanel, BorderLayout.NORTH);

        // 创建输入区域
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BACKGROUND_COLOR);
        inputPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
        
        // 创建包含输入框和发送按钮的面板
        JPanel inputFieldPanel = new JPanel(new BorderLayout(5, 0));
        inputFieldPanel.setBackground(Color.WHITE);
        inputFieldPanel.setBorder(new CompoundBorder(
                new RoundedBorder(20, SECONDARY_COLOR),  // 使用更大的圆角值
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        
        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 15)); // 更大的字体
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0),
                BorderFactory.createEmptyBorder(10, 15, 10, 5))); // 更多的内边距
        inputField.setBackground(Color.WHITE);
        inputField.setForeground(TEXT_COLOR);
        
        // 创建圆形发送按钮
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        sendButton.setBackground(PRIMARY_COLOR);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.setPreferredSize(new Dimension(80, 36));
        sendButton.setBorder(new RoundedBorder(20, PRIMARY_COLOR));
        
        // 将输入框和按钮添加到面板
        inputFieldPanel.add(inputField, BorderLayout.CENTER);
        inputFieldPanel.add(sendButton, BorderLayout.EAST);
        
        // 添加到主输入面板
        inputPanel.add(inputFieldPanel, BorderLayout.CENTER);
        
        // 将输入面板添加到底部面板
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        
        // 将底部面板添加到主面板
        add(bottomPanel, BorderLayout.SOUTH);
        
        // 创建进度条面板(开始时不可见)
        progressPanel = ChatBubbleFactory.createProgressPanel();
        // 从进度条面板中获取组件的正确方式
        JPanel progressContentPanel = (JPanel)progressPanel.getComponent(0);
        progressBar = (JProgressBar) progressContentPanel.getComponent(1);
        progressLabel = (JLabel) progressContentPanel.getComponent(0);
        progressPanel.setVisible(false);

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
        
        // 添加初始化消息
        addAIMessage("Hello! I'm your personal financial advisor. Whether it's budget planning, savings goals, investment advice, or debt management, I can provide professional guidance. What financial questions can I help you with?");
        
        // 发送初始交易数据到AI
        if (chatService.getTransactionsData() != null && !chatService.getTransactionsData().isEmpty()) {
            analyzeTransactionData();
        }
    }
    
    /**
     * 创建推荐问题面板
     */
    private JPanel createSuggestionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
        // 创建顶部面板，包含标签和刷新按钮
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel suggestLabel = new JLabel("Try asking:");
        suggestLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        suggestLabel.setForeground(TEXT_COLOR);
        topPanel.add(suggestLabel, BorderLayout.WEST);
        
        // 创建刷新按钮
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        refreshButton.setBackground(PRIMARY_COLOR);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.setMargin(new Insets(3, 8, 3, 8));
        refreshButton.setBorder(new RoundedBorder(15, PRIMARY_COLOR));
        
        // 添加刷新按钮鼠标悬停效果
        refreshButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                refreshButton.setBackground(SECONDARY_COLOR);
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                refreshButton.setBackground(PRIMARY_COLOR);
            }
        });
        
        // 添加刷新按钮点击事件
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshSuggestions();
            }
        });
        
        topPanel.add(refreshButton, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        
        // 创建建议按钮面板
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttonsPanel.setBackground(BACKGROUND_COLOR);
        
        // 初始化当前显示的建议问题
        if (currentSuggestedQuestions == null) {
            refreshSuggestions();
        }
        
        // 添加建议问题按钮
        for (String question : currentSuggestedQuestions) {
            JButton suggestionButton = createSuggestionButton(question);
            buttonsPanel.add(suggestionButton);
        }
        
        panel.add(buttonsPanel, BorderLayout.CENTER);
        suggestionsPanel = panel; // 保存引用以便后续刷新
        
        return panel;
    }
    
    /**
     * 刷新建议问题列表
     */
    private void refreshSuggestions() {
        // 随机选择指定数量的问题
        currentSuggestedQuestions = getRandomQuestions(ALL_SUGGESTED_QUESTIONS, SUGGESTION_DISPLAY_COUNT);
        
        // 如果建议面板已存在，则更新它
        if (suggestionsPanel != null) {
            // 获取按钮面板
            JPanel buttonsPanel = (JPanel) suggestionsPanel.getComponent(1);
            buttonsPanel.removeAll();
            
            // 添加新的建议按钮
            for (String question : currentSuggestedQuestions) {
                JButton suggestionButton = createSuggestionButton(question);
                buttonsPanel.add(suggestionButton);
            }
            
            // 刷新UI
            buttonsPanel.revalidate();
            buttonsPanel.repaint();
        }
    }
    
    /**
     * 从问题列表中随机选择指定数量的问题
     */
    private String[] getRandomQuestions(String[] allQuestions, int count) {
        // 创建问题索引列表
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < allQuestions.length; i++) {
            indices.add(i);
        }
        
        // 随机打乱顺序
        java.util.Collections.shuffle(indices);
        
        // 选择前count个问题
        int actualCount = Math.min(count, allQuestions.length);
        String[] result = new String[actualCount];
        
        for (int i = 0; i < actualCount; i++) {
            result[i] = allQuestions[indices.get(i)];
        }
        
        return result;
    }
    
    /**
     * 创建单个推荐问题按钮
     */
    private JButton createSuggestionButton(String question) {
        JButton button = new JButton(question);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setBackground(SUGGESTION_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(5, 10, 5, 10));
        
        // 使用圆角边框
        button.setBorder(new CompoundBorder(
            new RoundedBorder(15, SECONDARY_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(SUGGESTION_HOVER_COLOR);
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SUGGESTION_COLOR);
            }
        });
        
        // 添加点击事件
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 将问题填充到输入框并发送
                inputField.setText(question);
                sendMessage();
            }
        });
        
        return button;
    }
    
    /**
     * 发送分析交易数据请求，显示进度条并等待结果
     */
    private void analyzeTransactionData() {
        new Thread(() -> {
            try {
                // 禁用输入框直到初始分析完成
                setInputEnabled(false);
                
                // 替换底部面板为进度条面板
                remove(getComponent(2)); // 移除底部面板
                add(progressPanel, BorderLayout.SOUTH);
                progressPanel.setVisible(true);
                revalidate();
                repaint();
                
                // 启动进度条动画
                startProgressBar();
                
                // 获取AI响应
                String response = chatService.analyzeTransactionData();
                
                // 将分析结果存储到系统内，并显示给用户
                System.out.println("AI has analyzed the transaction data and prepared advice for the user's situation");
                
                // 停止进度条并显示结果
                SwingUtilities.invokeLater(() -> {
                    // 停止进度条动画
                    stopProgressBar();
                    
                    // 转换Markdown为普通文本
                    String plainTextResponse = convertMarkdownToPlainText(response);
                    
                    // 显示分析结果和财务建议
                    addAIMessage("Based on your transaction data, I have prepared the following financial analysis and advice:\n\n" + 
                                plainTextResponse + "\n\nWhat specific financial questions do you need help with?");
                });
                
                // 启用输入框
                setInputEnabled(true);
                
            } catch (Exception e) {
                System.err.println("Failed to send transaction data: " + e.getMessage());
                e.printStackTrace();
                
                // 停止进度条并恢复输入
                SwingUtilities.invokeLater(() -> {
                    stopProgressBar();
                    setInputEnabled(true);
                    addAIMessage("Sorry, there was an issue analyzing your transaction data. Please try again later.");
                });
            }
        }).start();
    }

    /**
     * 发送用户消息到AI并获取响应
     */
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            // 显示用户消息
            addUserMessage(message);
            inputField.setText("");
            
            // 禁用输入框和发送按钮，直到回复完成
            setInputEnabled(false);
            
            // 创建新的AI消息气泡(流式输出)
            currentAIMessageArea = ChatBubbleFactory.createStreamingAIMessageBubble(chatMessagesPanel, messageComponents);
            
            // 重置当前回复
            currentResponse = new StringBuilder();

            // 发送到API并获取响应
            chatService.getAIResponseStreaming(message, new AIChatService.StreamResponseHandler() {
                @Override
                public void onResponseChunk(String content) {
                    // 累积响应
                    currentResponse.append(content);
                    
                    // 转换Markdown为普通文本
                    String plainTextContent = convertMarkdownToPlainText(content);
                    
                    // 实时显示增量内容
                    SwingUtilities.invokeLater(() -> {
                        ChatBubbleFactory.updateStreamingMessageBubble(currentAIMessageArea, plainTextContent, chatMessagesPanel);
                        // 滚动到底部
                        JScrollBar vertical = scrollPane.getVerticalScrollBar();
                        vertical.setValue(vertical.getMaximum());
                    });
                }
                
                @Override
                public void onError(Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        ChatBubbleFactory.updateStreamingMessageBubble(currentAIMessageArea, 
                                "\nSorry, I cannot connect to the AI service right now. Please try again later. Error information: " + e.getMessage(), 
                                chatMessagesPanel);
                        setInputEnabled(true);
                    });
                    e.printStackTrace();
                }
                
                @Override
                public void onComplete() {
                    SwingUtilities.invokeLater(() -> setInputEnabled(true));
                }
            });
        }
    }
    
    /**
     * 启动进度条动画
     */
    private void startProgressBar() {
        progressBar.setValue(0);
        
        // 创建定时器，更新进度条
        progressTimer = new Timer(100, new ActionListener() {
            private double progressValue = 0; // 使用double类型存储实际进度值
            private boolean forward = true;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (forward) {
                    // 减小进度增加的速度
                    progressValue += 0.55;
                    if (progressValue >= 85) {
                        // 进度达到85%后放慢速度
                        forward = false;
                    }
                } else {
                    // 模拟处理波动，进一步减缓进度增加
                    progressValue += (Math.random() > 0.8) ? 0.3 : 0;
                }
                
                // 将double值转换为int后设置到进度条
                int intProgress = (int)Math.min(progressValue, 95); 
                progressBar.setValue(intProgress); // 最大进度为95%，完成时设为100%
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
            
            // 短暂延迟后切换回底部面板
            Timer completionTimer = new Timer(500, e -> {
                progressPanel.setVisible(false);
                remove(progressPanel);
                
                // 重新创建并添加底部面板
                JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
                bottomPanel.setBackground(BACKGROUND_COLOR);
                bottomPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
                
                // 刷新一次建议问题
                refreshSuggestions();
                
                // 创建推荐问题面板
                JPanel suggestionsPanel = createSuggestionsPanel();
                bottomPanel.add(suggestionsPanel, BorderLayout.NORTH);
                
                // 创建输入区域
                JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
                inputPanel.setBackground(BACKGROUND_COLOR);
                inputPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
                
                // 创建包含输入框和发送按钮的面板
                JPanel inputFieldPanel = new JPanel(new BorderLayout(5, 0));
                inputFieldPanel.setBackground(Color.WHITE);
                inputFieldPanel.setBorder(new CompoundBorder(
                        new RoundedBorder(20, SECONDARY_COLOR),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)));
                
                inputField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(0, 0, 0, 0),
                        BorderFactory.createEmptyBorder(10, 15, 10, 5)));
                
                // 将输入框和按钮添加到面板
                inputFieldPanel.add(inputField, BorderLayout.CENTER);
                inputFieldPanel.add(sendButton, BorderLayout.EAST);
                
                // 添加到主输入面板
                inputPanel.add(inputFieldPanel, BorderLayout.CENTER);
                
                // 将输入面板添加到底部面板
                bottomPanel.add(inputPanel, BorderLayout.CENTER);
                
                // 将底部面板添加到主面板
                add(bottomPanel, BorderLayout.SOUTH);
                
                revalidate();
                repaint();
            });
            completionTimer.setRepeats(false);
            completionTimer.start();
        }
    }
    
    /**
     * 设置输入控件启用状态
     */
    private void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
    }
    
    /**
     * 添加用户消息到聊天区域
     */
    private void addUserMessage(String message) {
        JPanel messageBubble = ChatBubbleFactory.createUserMessageBubble(message);
        chatMessagesPanel.add(messageBubble);
        messageComponents.add(messageBubble);
        chatMessagesPanel.add(Box.createVerticalStrut(15)); // 添加一些底部间距
        
        // 更新UI
        chatMessagesPanel.revalidate();
        chatMessagesPanel.repaint();
        
        // 滚动到底部
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    /**
     * 添加AI消息到聊天区域
     */
    private void addAIMessage(String message) {
        JPanel messageBubble = ChatBubbleFactory.createAIMessageBubble(message);
        chatMessagesPanel.add(messageBubble);
        messageComponents.add(messageBubble);
        chatMessagesPanel.add(Box.createVerticalStrut(15)); // 添加一些底部间距
        
        // 更新UI
        chatMessagesPanel.revalidate();
        chatMessagesPanel.repaint();
        
        // 滚动到底部
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    /**
     * 将Markdown格式转换为普通文本
     * 
     * @param markdown Markdown格式文本
     * @return 转换后的普通文本
     */
    private String convertMarkdownToPlainText(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        
        String result = markdown;
        
        // 处理标题 (# Header)
        result = result.replaceAll("(?m)^#\\s+(.*?)$", "$1:");
        result = result.replaceAll("(?m)^##\\s+(.*?)$", "$1:");
        result = result.replaceAll("(?m)^###\\s+(.*?)$", "$1:");
        result = result.replaceAll("(?m)^####\\s+(.*?)$", "$1:");
        
        // 处理粗体 (**text** 或 __text__)
        result = result.replaceAll("\\*\\*(.*?)\\*\\*", "$1");
        result = result.replaceAll("__(.*?)__", "$1");
        
        // 处理斜体 (*text* 或 _text_)
        result = result.replaceAll("\\*(.*?)\\*", "$1");
        result = result.replaceAll("_(.*?)_", "$1");
        
        // 处理列表项，保留列表符号但格式化空间
        result = result.replaceAll("(?m)^\\s*-\\s+", "• ");
        result = result.replaceAll("(?m)^\\s*\\*\\s+", "• ");
        result = result.replaceAll("(?m)^\\s*\\d+\\.\\s+", "• ");
        
        // 处理代码块 ```code```
        Pattern codeBlockPattern = Pattern.compile("```[\\s\\S]*?```");
        Matcher codeBlockMatcher = codeBlockPattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (codeBlockMatcher.find()) {
            String codeBlock = codeBlockMatcher.group();
            String cleanCode = codeBlock.substring(3, codeBlock.length() - 3).trim();
            codeBlockMatcher.appendReplacement(sb, Matcher.quoteReplacement(cleanCode));
        }
        codeBlockMatcher.appendTail(sb);
        result = sb.toString();
        
        // 处理行内代码 `code`
        result = result.replaceAll("`([^`]*?)`", "$1");
        
        // 处理链接 [text](url)
        result = result.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "$1");
        
        // 处理图片 ![alt text](url)
        result = result.replaceAll("!\\[(.*?)\\]\\((.*?)\\)", "[Image: $1]");
        
        // 处理水平分隔线
        result = result.replaceAll("(?m)^\\s*[-*_]{3,}\\s*$", "\n---\n");
        
        // 处理引用
        result = result.replaceAll("(?m)^>\\s+(.*?)$", "\"$1\"");
        
        // 处理表格(简化处理，仅移除表格标记)
        result = result.replaceAll("\\|\\s*:?-+:?\\s*\\|", "");
        result = result.replaceAll("\\|", " | ");
        
        return result;
    }
} 