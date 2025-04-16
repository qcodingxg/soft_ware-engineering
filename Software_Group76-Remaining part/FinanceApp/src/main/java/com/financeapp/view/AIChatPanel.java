package com.financeapp.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    public AIChatPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 创建聊天区域
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // 创建输入区域
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("发送");
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

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
        
        // 添加初始化消息
        chatArea.append("财智助手: 您好！我是您的个人财务顾问。无论是预算规划、储蓄目标、投资建议还是债务管理，我都能为您提供专业指导。有什么财务问题我可以帮您解答吗？\n\n");
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            // 显示用户消息
            appendMessage("用户: " + message);
            inputField.setText("");

            // 发送到API并获取响应
            new Thread(() -> {
                try {
                    String response = getAIResponse(message);
                    appendMessage("财智助手: " + response);
                } catch (Exception e) {
                    appendMessage("错误: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

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