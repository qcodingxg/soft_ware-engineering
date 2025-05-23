package com.financeapp.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 处理与AI聊天API的所有交互
 */
public class AIChatService {
    private static final String API_KEY = "sk-1b18b92f15b84a2a98b510300e8fbc28"; // 请替换为您的API密钥
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

    // 交易数据文件路径
    private static final String TRANSACTIONS_CSV_PATH = "data/transactions.csv";

    // 存储交易数据内容
    private String transactionsData;

    // 接口定义: 用于处理流式响应
    public interface StreamResponseHandler {
        void onResponseChunk(String content);
        void onError(Exception e);
        void onComplete();
    }

    public AIChatService() {
        loadTransactionsData();
    }

    /**
     * 加载交易数据从CSV文件
     */
    private void loadTransactionsData() {
        loadTransactionsData(TRANSACTIONS_CSV_PATH);
    }

    /**
     * 加载交易数据从指定的CSV文件路径
     * @param csvPath CSV文件路径
     */
    public void loadTransactionsData(String csvPath) {
        try {
            // 从指定路径读取文件
            File file = new File(csvPath);
            if (!file.exists()) {
                // 尝试从绝对路径读取
                String absolutePath = new File("").getAbsolutePath();
                file = new File(absolutePath + File.separator + csvPath);

                if (!file.exists()) {
                    System.err.println("交易数据文件未找到: " + csvPath);
                    transactionsData = null;
                    return;
                }
            }

            // 读取文件内容
            transactionsData = Files.readAllLines(Paths.get(file.getAbsolutePath()))
                .stream()
                .collect(Collectors.joining("\n"));

            System.out.println("成功加载交易数据从: " + file.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("加载交易数据失败: " + e.getMessage());
            e.printStackTrace();
            transactionsData = null;
        }
    }

    /**
     * 获取交易数据的内容
     */
    public String getTransactionsData() {
        return transactionsData;
    }

    /**
     * 发送交易数据到AI，获取财务分析
     */
    public String analyzeTransactionData() throws Exception {
        if (transactionsData == null || transactionsData.isEmpty()) {
            return "No transaction data available for analysis.";
        }

        // 创建提示词，要求AI基于交易数据生成财务建议
        String message = "Please analyze the user's financial situation based on the following transaction data and provide 3-5 specific financial advice. These data are the user's latest transaction records. Please ensure your response includes the following parts: 1. A brief summary of the user's financial situation; 2. 3-5 specific and targeted financial advice. The format should be concise and clear.\n\n" +
                        "Transaction data (CSV format):\n" + transactionsData;

        // 获取AI响应（使用完整响应而非流式输出）
        return getAIResponse(message);
    }

    /**
     * 获取非流式AI响应（用于初始交易数据分析）
     */
    public String getAIResponse(String message) throws Exception {
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
     * 获取流式AI响应
     */
    public void getAIResponseStreaming(String message, StreamResponseHandler handler) {
        new Thread(() -> {
            try {
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
                                        // 回调处理增量内容
                                        handler.onResponseChunk(content);
                                    }
                                }
                            } catch (Exception e) {
                                // 如果解析特定行失败，继续处理下一行
                                System.err.println("无法解析流数据行: " + data);
                            }
                        }
                    }

                    // 处理完成
                    handler.onComplete();
                }
            } catch (Exception e) {
                // 处理错误
                handler.onError(e);
            }
        }).start();
    }
}