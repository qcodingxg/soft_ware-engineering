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
    private static final String SYSTEM_PROMPT = "你是一位专业的个人财务顾问，名为'财智助手'。你的专长包括：" +
            "1. 个人预算规划与支出追踪\n" +
            "2. 债务管理与清偿策略\n" +
            "3. 储蓄目标设定与达成方法\n" +
            "4. 基础投资建议（股票、基金、定期存款等）\n" +
            "5. 税务规划与优化\n" +
            "6. 退休规划\n" +
            "7. 保险配置建议\n\n" +
            "在回答用户问题时，请遵循以下原则：\n" +
            "- 保持专业且易于理解的语言\n" +
            "- 提供具体、可操作的建议\n" +
            "- 解释财务概念时使用简单的类比\n" +
            "- 尊重用户的财务状况，不做过度假设\n" +
            "- 鼓励健康的财务习惯和长期规划\n" +
            "- 提醒用户重要的财务决策应咨询专业人士\n" +
            "- 所有回答均使用中文\n\n" +
            "当用户提出非财务相关问题时，礼貌地将话题引导回财务领域。";
    
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
        
        sendButton = createStyledButton("发送");
        
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
        inputField.putClientProperty("JTextField.placeholderText", "请输入您的财务问题...");
        
        // 初始化当前回复
        currentResponse = new StringBuilder();
        
        // 加载交易数据
        loadTransactionsData();
        
        // 添加初始化消息
        chatArea.append("财智助手: 您好！我是您的个人财务顾问。无论是预算规划、储蓄目标、投资建议还是债务管理，我都能为您提供专业指导。有什么财务问题我可以帮您解答吗？\n\n");
        
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
        JLabel titleLabel = new JLabel("AI财务顾问");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.WEST);
        
        // 创建状态标签
        JLabel statusLabel = new JLabel("在线");
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
                String message = "请基于以下交易数据分析用户的财务状况，并提供3-5条具体的财务建议。这些数据是用户的最新交易记录。请确保你的回答包含以下部分：1. 简短的财务状况总结；2. 3-5条具体的、针对性的财务建议。格式应简洁清晰。\n\n" + 
                                "交易数据 (CSV格式):\n" + transactionsData;
                
                // 禁用输入框直到初始分析完成
                setInputEnabled(false);
                
                // 启动进度条动画
                SwingUtilities.invokeLater(() -> startProgressBar());
                
                // 获取AI响应（使用完整响应而非流式输出，避免显示处理过程）
                String response = getAIResponse(message);
                
                // 将分析结果存储到系统内，并显示给用户
                System.out.println("AI已分析交易数据，准备好针对用户情况的建议");
                
                // 停止进度条并显示结果
                SwingUtilities.invokeLater(() -> {
                    // 停止进度条动画
                    stopProgressBar();
                    
                    // 显示分析结果和财务建议
                    chatArea.append("财智助手: 基于您的交易数据，我为您准备了以下财务分析和建议：\n\n");
                    chatArea.append(response);
                    chatArea.append("\n\n您有什么具体的财务问题需要咨询吗？\n\n");
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
                    chatArea.append("财智助手: 抱歉，分析您的交易数据时遇到了问题。请稍后再试。\n\n");
                });
            }
        }).start();
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            // 显示用户消息
            appendMessage("用户: " + message);
            inputField.setText("");
            
            // 禁用输入框和发送按钮，直到回复完成
            setInputEnabled(false);
            
            // 添加AI助手标签但不换行，以便后续流式显示内容
            appendMessageWithoutNewline("财智助手: ");
            
            // 重置当前回复
            currentResponse = new StringBuilder();

            // 发送到API并获取响应
            new Thread(() -> {
                try {
                    getAIResponseStreaming(message);
                    // 回复完成后添加额外的换行
                    appendNewline();
                } catch (Exception e) {
                    appendMessageFromThread("错误: " + e.getMessage());
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
            if (message.startsWith("用户:")) {
                // 为用户消息应用不同样式
                chatArea.append(message + "\n\n");
            } else {
                chatArea.append(message + "\n\n");
            }
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private void appendMessageWithoutNewline(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message);
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private void appendNewline() {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("\n\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private void appendMessageFromThread(String message) {
        SwingUtilities.invokeLater(() -> {
            // 为错误消息应用不同样式
            if (message.startsWith("错误:")) {
                chatArea.append(message + "\n\n");
            } else {
                chatArea.append(message + "\n\n");
            }
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private void appendStreamContent(String content) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(content);
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
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
                "\n\n我已经分析了用户的交易数据，了解了用户的财务状况。请基于这些信息提供更有针对性的建议。" : ""));
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
                        System.err.println("无法解析流数据行: " + data);
                    }
                }
            }
        } catch (Exception e) {
            // 如果API调用失败，显示错误信息
            appendStreamContent("\n抱歉，我现在无法连接到AI服务。请稍后再试。错误信息: " + e.getMessage());
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
            return "抱歉，我现在无法连接到AI服务。请稍后再试。错误信息: " + e.getMessage();
        }

        try {
            // 解析响应
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (Exception e) {
            return "抱歉，我无法理解AI服务的响应。错误信息: " + e.getMessage();
        }
    }
} 