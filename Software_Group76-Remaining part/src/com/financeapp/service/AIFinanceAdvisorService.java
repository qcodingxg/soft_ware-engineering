package com.financeapp.service;

import com.financeapp.model.Transaction;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for analyzing financial data using AI and generating recommendations
 */
public class AIFinanceAdvisorService {
    
    private static final String CSV_HEADER = "日期,分类,金额,备注";
    private static final HttpClient httpClient = HttpClient.newBuilder().build();
    
    // OpenAI API 配置
    private String apiKey;
    private String apiUrl;
    private String model;
    
    // 默认值
    private static final String DEFAULT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";
    
    public AIFinanceAdvisorService(String apiKey) {
        this.apiKey = apiKey;
        this.apiUrl = DEFAULT_API_URL;
        this.model = DEFAULT_MODEL;
    }
    
    public AIFinanceAdvisorService(String apiKey, String apiUrl, String model) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl != null && !apiUrl.isEmpty() ? apiUrl : DEFAULT_API_URL;
        this.model = model != null && !model.isEmpty() ? model : DEFAULT_MODEL;
    }
    
    /**
     * 设置API URL
     * @param apiUrl 新的API URL
     */
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl != null && !apiUrl.isEmpty() ? apiUrl : DEFAULT_API_URL;
    }
    
    /**
     * 设置模型名称
     * @param model 新的模型名称
     */
    public void setModel(String model) {
        this.model = model != null && !model.isEmpty() ? model : DEFAULT_MODEL;
    }
    
    /**
     * 获取当前API URL
     * @return 当前API URL
     */
    public String getApiUrl() {
        return apiUrl;
    }
    
    /**
     * 获取当前模型名称
     * @return 当前模型名称
     */
    public String getModel() {
        return model;
    }
    
    /**
     * 更新API密钥
     * @param apiKey 新的API密钥
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * 从CSV文件中加载交易数据
     * @param csvFilePath CSV文件路径
     * @return 交易列表
     */
    public List<Transaction> loadTransactionsFromCSV(String csvFilePath) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        Path path = Paths.get(csvFilePath);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine && line.contains(CSV_HEADER)) {
                    isFirstLine = false;
                    continue;
                }
                
                try {
                    Transaction transaction = Transaction.fromCSV(line);
                    transactions.add(transaction);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error parsing CSV line: " + e.getMessage());
                }
            }
        }
        
        return transactions;
    }
    
    /**
     * 获取当前月份的交易数据分析
     * @param transactions 交易列表
     * @return 分析结果
     */
    public Map<String, Object> analyzeCurrentMonth(List<Transaction> transactions) {
        Map<String, Object> analysis = new HashMap<>();
        
        // 获取当前月份
        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();
        
        // 过滤当前月份的交易
        List<Transaction> currentMonthTransactions = transactions.stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .collect(Collectors.toList());
        
        // 计算总收入和支出
        double totalIncome = currentMonthTransactions.stream()
                .filter(t -> t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        double totalExpense = currentMonthTransactions.stream()
                .filter(t -> t.getAmount() < 0)
                .mapToDouble(t -> Math.abs(t.getAmount()))
                .sum();
        
        double balance = totalIncome - totalExpense;
        
        // 按类别汇总支出
        Map<String, Double> categoryExpenses = new HashMap<>();
        currentMonthTransactions.stream()
                .filter(t -> t.getAmount() < 0)
                .forEach(t -> {
                    String category = t.getCategory();
                    double amount = Math.abs(t.getAmount());
                    categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
                });
        
        // 找出最大支出类别
        String topExpenseCategory = categoryExpenses.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
        
        double topExpenseAmount = categoryExpenses.getOrDefault(topExpenseCategory, 0.0);
        
        // 添加分析结果
        analysis.put("totalIncome", totalIncome);
        analysis.put("totalExpense", totalExpense);
        analysis.put("balance", balance);
        analysis.put("categoryExpenses", categoryExpenses);
        analysis.put("topExpenseCategory", topExpenseCategory);
        analysis.put("topExpenseAmount", topExpenseAmount);
        analysis.put("transactionCount", currentMonthTransactions.size());
        
        return analysis;
    }
    
    /**
     * 使用 OpenAI GPT 获取财务建议
     * @param transactions 交易数据
     * @return 财务建议
     */
    public String getAIRecommendations(List<Transaction> transactions) throws IOException, InterruptedException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "Error: API key not configured. Please set a valid OpenAI API key.";
        }
        
        Map<String, Object> analysis = analyzeCurrentMonth(transactions);
        
        // 准备发送给 OpenAI 的数据
        String prompt = createPromptFromAnalysis(analysis, transactions);
        
        // 使用字符串构建JSON请求
        String requestBody = buildOpenAIRequestJson(prompt);
        
        // 发送请求到 OpenAI API
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // 使用正则表达式从响应中提取内容
                String content = extractContentFromResponse(response.body());
                if (content != null) {
                    return content;
                }
            }
            
            return "Error: " + response.statusCode() + " - " + response.body();
        } catch (Exception e) {
            return "Error while calling OpenAI API: " + e.getMessage();
        }
    }
    
    /**
     * 构建OpenAI请求的JSON字符串
     * @param prompt 提示文本
     * @return JSON请求字符串
     */
    private String buildOpenAIRequestJson(String prompt) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"model\": \"").append(model).append("\",");
        json.append("\"messages\": [");
        
        // 添加系统消息
        json.append("{");
        json.append("\"role\": \"system\",");
        json.append("\"content\": \"You are a financial advisor specialized in personal finance management. ");
        json.append("Provide concise, practical advice for improving financial health based on transaction data. ");
        json.append("Include specific, actionable recommendations and highlight potential savings opportunities. ");
        json.append("Format your response with clear sections for summary, insights, and next steps.\"");
        json.append("},");
        
        // 添加用户消息
        json.append("{");
        json.append("\"role\": \"user\",");
        json.append("\"content\": \"").append(escapeJsonString(prompt)).append("\"");
        json.append("}");
        
        json.append("],");
        json.append("\"temperature\": 0.7,");
        json.append("\"max_tokens\": 800");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * 转义JSON字符串
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    private String escapeJsonString(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    /**
     * 从OpenAI响应中提取内容
     * @param responseBody 响应体
     * @return 提取的内容
     */
    private String extractContentFromResponse(String responseBody) {
        Pattern pattern = Pattern.compile("\"content\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(responseBody);
        
        if (matcher.find()) {
            String content = matcher.group(1);
            return content.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
        }
        
        return null;
    }
    
    /**
     * 创建发送给 OpenAI 的提示
     * @param analysis 分析结果
     * @param transactions 交易数据
     * @return 提示文本
     */
    private String createPromptFromAnalysis(Map<String, Object> analysis, List<Transaction> transactions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Based on the following financial data, provide personalized advice and recommendations for next month:\n\n");
        
        // 添加当前月份分析
        prompt.append("Current Month Summary:\n");
        prompt.append("- Total Income: $").append(String.format("%.2f", analysis.get("totalIncome"))).append("\n");
        prompt.append("- Total Expenses: $").append(String.format("%.2f", analysis.get("totalExpense"))).append("\n");
        prompt.append("- Balance: $").append(String.format("%.2f", analysis.get("balance"))).append("\n");
        prompt.append("- Transaction Count: ").append(analysis.get("transactionCount")).append("\n");
        prompt.append("- Top Expense Category: ").append(analysis.get("topExpenseCategory"))
                .append(" ($").append(String.format("%.2f", analysis.get("topExpenseAmount"))).append(")\n\n");
        
        // 添加类别支出详情
        prompt.append("Expense Breakdown by Category:\n");
        @SuppressWarnings("unchecked")
        Map<String, Double> categoryExpenses = (Map<String, Double>) analysis.get("categoryExpenses");
        categoryExpenses.forEach((category, amount) -> {
            prompt.append("- ").append(category).append(": $").append(String.format("%.2f", amount)).append("\n");
        });
        
        // 添加请求
        prompt.append("\nBased on this information, please provide:\n");
        prompt.append("1. A brief analysis of the current spending patterns\n");
        prompt.append("2. Three specific recommendations for improving financial health next month\n");
        prompt.append("3. Potential savings opportunities\n");
        prompt.append("4. Any areas of concern that should be addressed immediately\n");
        
        return prompt.toString();
    }
    
    /**
     * 获取当前月份与前几个月的支出趋势比较
     * @param transactions 所有交易
     * @param months 要比较的月数
     * @return 趋势分析结果
     */
    public Map<String, Object> getMonthlyTrends(List<Transaction> transactions, int months) {
        Map<String, Object> trends = new HashMap<>();
        
        YearMonth currentMonth = YearMonth.now();
        for (int i = 0; i < months; i++) {
            YearMonth targetMonth = currentMonth.minus(i, ChronoUnit.MONTHS);
            LocalDate start = targetMonth.atDay(1);
            LocalDate end = targetMonth.atEndOfMonth();
            
            double monthlyExpense = transactions.stream()
                    .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                    .filter(t -> t.getAmount() < 0)
                    .mapToDouble(t -> Math.abs(t.getAmount()))
                    .sum();
            
            trends.put(targetMonth.toString(), monthlyExpense);
        }
        
        return trends;
    }
} 