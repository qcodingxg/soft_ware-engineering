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
    private JPanel chatMessagesPanel; // 替换JTextArea的消息面板
    private JScrollPane scrollPane;   // 滚动面板
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
            "- 所有回答均使用英文\n\n" +
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

    // 当前AI回复的消息面板
    private JTextArea currentAIMessageArea;
    // 存储所有消息面板的列表
    private List<Component> messageComponents = new ArrayList<>();

    public AIChatPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        // 设置首选大小，使聊天面板更宽
        setPreferredSize(new Dimension(800, 600));

        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
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

        // 创建输入区域
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BACKGROUND_COLOR);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 创建包含输入框和发送按钮的面板
        JPanel inputFieldPanel = new JPanel(new BorderLayout(5, 0));
        inputFieldPanel.setBackground(Color.WHITE);
        inputFieldPanel.setBorder(new CompoundBorder(
                new RoundedBorder(20, SECONDARY_COLOR),  // 使用更大的圆角值
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        
        inputField = new JTextField();
        inputField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15)); // 更大的字体
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0),
                BorderFactory.createEmptyBorder(10, 15, 10, 5))); // 更多的内边距
        inputField.setBackground(Color.WHITE);
        inputField.setForeground(TEXT_COLOR);
        
        // 创建圆形发送按钮
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
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
        addAIMessage("Hello! I'm your personal financial advisor. Whether it's budget planning, savings goals, investment advice, or debt management, I can provide professional guidance. What financial questions can I help you with?");
        
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
        // 将更新间隔从80毫秒增加到200毫秒，同时调整进度增加的速度
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
            
            // 短暂延迟后切换回输入面板
            Timer completionTimer = new Timer(500, e -> {
                progressPanel.setVisible(false);
                remove(progressPanel);
                
                // 重新添加输入面板
                JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
                inputPanel.setBackground(BACKGROUND_COLOR);
                inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
                
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
                    addAIMessage("Based on your transaction data, I have prepared the following financial analysis and advice:\n\n" + 
                                response + "\n\nWhat specific financial questions do you need help with?");
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
                    addAIMessage("Sorry, there was an issue analyzing your transaction data. Please try again later.");
                });
            }
        }).start();
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            // 显示用户消息
            addUserMessage(message);
            inputField.setText("");
            
            // 禁用输入框和发送按钮，直到回复完成
            setInputEnabled(false);
            
            // 创建新的AI消息气泡(流式输出)
            currentAIMessageArea = createStreamingAIMessageBubble();
            
            // 重置当前回复
            currentResponse = new StringBuilder();

            // 发送到API并获取响应
            new Thread(() -> {
                try {
                    getAIResponseStreaming(message);
                } catch (Exception e) {
                    appendStreamContent("Error: " + e.getMessage());
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

    /**
     * 计算文本在给定宽度下需要的行数
     * @param text 文本内容
     * @param textArea 文本区域
     * @param width 可用宽度
     * @return 估计的行数
     */
    private int estimateLineCount(String text, JTextArea textArea, int width) {
        // 创建一个临时的字体度量对象用于计算
        FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
        
        // 考虑到单词换行，这里做一个粗略估计
        int charWidth = fm.charWidth('a'); // 使用字母'a'的宽度作为平均字符宽度
        int charsPerLine = Math.max(1, width / charWidth);
        
        // 计算文本的行数（考虑已有的换行符）
        String[] lines = text.split("\n");
        int totalLines = 0;
        
        for (String line : lines) {
            // 每行文本可能需要自动换行，所以计算它需要的额外行数
            int lineLength = line.length();
            int linesNeeded = (int) Math.ceil((double) lineLength / charsPerLine);
            totalLines += Math.max(1, linesNeeded);
        }
        
        return totalLines;
    }

    private void appendStreamContent(String content) {
        SwingUtilities.invokeLater(() -> {
            // 添加内容到文本区域
            currentAIMessageArea.append(content);
            
            // 获取当前内容的总长度
            String fullText = currentAIMessageArea.getText();
            int totalLength = fullText.length();
            
            // 根据内容长度动态调整气泡宽度
            int newWidth;
            if (totalLength < 100) {
                // 短消息
                newWidth = Math.min(350, 15 * totalLength);
                newWidth = Math.max(newWidth, 200); // 最小宽度
            } else if (totalLength < 500) {
                // 中等长度消息
                newWidth = 420;
            } else {
                // 长消息
                newWidth = 450;
            }
            
            // 估计需要的行数
            int textPadding = 30; // 文本区域内边距(左右两侧各15)
            int estimatedLines = estimateLineCount(fullText, currentAIMessageArea, newWidth - textPadding);
            int lineHeight = currentAIMessageArea.getFontMetrics(currentAIMessageArea.getFont()).getHeight();
            int estimatedHeight = estimatedLines * lineHeight + 10; // 额外增加一点高度作为缓冲
            
            // 设置新的首选大小
            currentAIMessageArea.setPreferredSize(new Dimension(newWidth, estimatedHeight));
            
            // 强制重新布局并验证大小
            Container parent = currentAIMessageArea.getParent();
            while (parent != null) {
                parent.invalidate();
                parent = parent.getParent();
            }
            chatMessagesPanel.revalidate();
            chatMessagesPanel.repaint();
            
            // 滚动到底部
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
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
                        System.err.println("无法解析流数据行: " + data);
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

    /**
     * 创建用户消息气泡（右侧对齐）
     */
    private JPanel createUserMessageBubble(String message) {
        // 外部面板，用于布局
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(Color.WHITE);
        outerPanel.setBorder(new EmptyBorder(5, 80, 5, 10)); // 增加左侧留白，使消息更靠右

        // 消息面板
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(USER_BUBBLE_COLOR);
        messagePanel.setBorder(new CompoundBorder(
            new RoundedBorder(10, USER_BUBBLE_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));

        // 消息文本
        JTextArea textArea = new JTextArea(message);
        textArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15)); // 增大字体
        textArea.setForeground(new Color(30, 30, 30)); // 更深的文本颜色，增加对比度
        textArea.setBackground(USER_BUBBLE_COLOR);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBorder(null);
        
        // 根据消息字数动态调整文本区域大小
        int messageLength = message.length();
        int preferredWidth;
        
        // 根据字数设置不同的宽度
        if (messageLength < 50) {
            // 短消息
            preferredWidth = Math.min(300, 15 * messageLength);
            preferredWidth = Math.max(preferredWidth, 150); // 最小宽度
        } else if (messageLength < 200) {
            // 中等长度消息
            preferredWidth = 350;
        } else {
            // 长消息
            preferredWidth = 400;
        }
        
        // 估计需要的行数和高度
        int textPadding = 30; // 文本区域内边距(左右两侧各15)
        int estimatedLines = estimateLineCount(message, textArea, preferredWidth - textPadding);
        int lineHeight = textArea.getFontMetrics(textArea.getFont()).getHeight();
        int estimatedHeight = estimatedLines * lineHeight + 10; // 额外增加一点高度作为缓冲
        
        // 设置首选尺寸，使文本区域宽度和消息长度匹配
        textArea.setPreferredSize(new Dimension(preferredWidth, estimatedHeight));

        messagePanel.add(textArea, BorderLayout.CENTER);
        outerPanel.add(messagePanel, BorderLayout.EAST); // 靠右对齐

        // 添加"You"标签
        JLabel userLabel = new JLabel("You");
        userLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        userLabel.setForeground(new Color(80, 80, 80));
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        labelPanel.setBackground(Color.WHITE);
        labelPanel.add(userLabel);
        
        // 垂直布局面板
        JPanel verticalPanel = new JPanel();
        verticalPanel.setLayout(new BoxLayout(verticalPanel, BoxLayout.Y_AXIS));
        verticalPanel.setBackground(Color.WHITE);
        verticalPanel.add(labelPanel);
        verticalPanel.add(outerPanel);
        
        return verticalPanel;
    }

    /**
     * 创建AI消息气泡（左侧对齐）
     */
    private JPanel createAIMessageBubble(String message) {
        // 外部面板，用于布局
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(Color.WHITE);
        outerPanel.setBorder(new EmptyBorder(5, 10, 5, 80)); // 增加右侧留白，使消息更靠左

        // 消息面板
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(BOT_BUBBLE_COLOR);
        messagePanel.setBorder(new CompoundBorder(
            new RoundedBorder(10, BOT_BUBBLE_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));

        // 消息文本
        JTextArea textArea = new JTextArea(message);
        textArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15)); // 增大字体
        textArea.setForeground(new Color(30, 30, 30)); // 更深的文本颜色，增加对比度
        textArea.setBackground(BOT_BUBBLE_COLOR);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBorder(null);
        
        // 根据消息字数动态调整文本区域大小
        int messageLength = message.length();
        int preferredWidth;
        
        // AI回复通常更长，所以给予更大的尺寸
        if (messageLength < 100) {
            // 短消息
            preferredWidth = Math.min(350, 15 * messageLength);
            preferredWidth = Math.max(preferredWidth, 200); // 最小宽度
        } else if (messageLength < 500) {
            // 中等长度消息
            preferredWidth = 420;
        } else {
            // 长消息
            preferredWidth = 450;
        }
        
        // 估计需要的行数和高度
        int textPadding = 30; // 文本区域内边距(左右两侧各15)
        int estimatedLines = estimateLineCount(message, textArea, preferredWidth - textPadding);
        int lineHeight = textArea.getFontMetrics(textArea.getFont()).getHeight();
        int estimatedHeight = estimatedLines * lineHeight + 10; // 额外增加一点高度作为缓冲
        
        // 设置首选尺寸，使文本区域宽度和消息长度匹配
        textArea.setPreferredSize(new Dimension(preferredWidth, estimatedHeight));

        messagePanel.add(textArea, BorderLayout.CENTER);
        outerPanel.add(messagePanel, BorderLayout.WEST); // 靠左对齐
        
        // 添加"AI Financial Advisor"标签
        JLabel aiLabel = new JLabel("AI Financial Advisor");
        aiLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        aiLabel.setForeground(new Color(80, 80, 80));
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelPanel.setBackground(Color.WHITE);
        labelPanel.add(aiLabel);
        
        // 垂直布局面板
        JPanel verticalPanel = new JPanel();
        verticalPanel.setLayout(new BoxLayout(verticalPanel, BoxLayout.Y_AXIS));
        verticalPanel.setBackground(Color.WHITE);
        verticalPanel.add(labelPanel);
        verticalPanel.add(outerPanel);
        
        return verticalPanel;
    }

    /**
     * 创建流式AI消息气泡（返回文本区域以便后续更新）
     */
    private JTextArea createStreamingAIMessageBubble() {
        // 外部面板，用于布局
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(Color.WHITE);
        outerPanel.setBorder(new EmptyBorder(5, 10, 5, 80)); // 增加右侧留白，使消息更靠左

        // 消息面板
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(BOT_BUBBLE_COLOR);
        messagePanel.setBorder(new CompoundBorder(
            new RoundedBorder(10, BOT_BUBBLE_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));

        // 消息文本
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15)); // 增大字体
        textArea.setForeground(new Color(30, 30, 30)); // 更深的文本颜色，增加对比度
        textArea.setBackground(BOT_BUBBLE_COLOR);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBorder(null);
        
        // 初始设置为较宽，因为AI回复通常较长
        // 后续会随着内容增加而自动调整大小
        textArea.setPreferredSize(new Dimension(450, 20));

        messagePanel.add(textArea, BorderLayout.CENTER);
        outerPanel.add(messagePanel, BorderLayout.WEST); // 靠左对齐
        
        // 添加"AI Financial Advisor"标签
        JLabel aiLabel = new JLabel("AI Financial Advisor");
        aiLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        aiLabel.setForeground(new Color(80, 80, 80));
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelPanel.setBackground(Color.WHITE);
        labelPanel.add(aiLabel);
        
        // 垂直布局面板
        JPanel verticalPanel = new JPanel();
        verticalPanel.setLayout(new BoxLayout(verticalPanel, BoxLayout.Y_AXIS));
        verticalPanel.setBackground(Color.WHITE);
        verticalPanel.add(labelPanel);
        verticalPanel.add(outerPanel);
        
        // 添加到消息面板
        chatMessagesPanel.add(verticalPanel);
        messageComponents.add(verticalPanel);
        chatMessagesPanel.add(Box.createVerticalStrut(15)); // 添加一些底部间距
        
        // 更新UI
        chatMessagesPanel.revalidate();
        chatMessagesPanel.repaint();
        
        // 滚动到底部
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
        
        return textArea;
    }
    
    /**
     * 添加用户消息到聊天区域
     */
    private void addUserMessage(String message) {
        JPanel messageBubble = createUserMessageBubble(message);
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
        JPanel messageBubble = createAIMessageBubble(message);
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
} 