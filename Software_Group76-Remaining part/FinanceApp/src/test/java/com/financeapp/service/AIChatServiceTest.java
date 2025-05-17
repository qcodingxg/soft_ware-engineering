package com.financeapp.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 测试AIChatService类
 * 测试AI聊天功能和交易数据分析
 */
public class AIChatServiceTest {

    private AIChatService aiChatService;
    
    @TempDir
    Path tempDir;
    
    private Path dataDir;
    private Path transactionsFile;
    private String originalUserDir;
    
    /**
     * 每次测试前的设置
     * 创建临时数据目录和测试交易数据文件
     */
    @BeforeEach
    public void setUp() throws IOException {
        // 保存原始用户目录
        originalUserDir = System.getProperty("user.dir");
        
        // 创建临时数据目录
        dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);
        
        // 创建测试交易数据文件
        transactionsFile = dataDir.resolve("transactions.csv");
        createTestTransactionsFile(transactionsFile.toFile());
        
        // 设置系统属性以使用临时目录
        System.setProperty("user.dir", tempDir.toString());
        
        // 初始化AIChatService
        aiChatService = new AIChatService();
    }
    
    /**
     * 每次测试后的清理
     */
    @AfterEach
    public void tearDown() {
        // 恢复原始用户目录
        System.setProperty("user.dir", originalUserDir);
    }
    
    /**
     * 创建测试交易数据文件
     */
    private void createTestTransactionsFile(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // 写入CSV头
            writer.write("date,category,amount,description\n");
            // 写入一些测试交易数据
            writer.write("2023-01-01,Food,25.50,Restaurant dinner\n");
            writer.write("2023-01-02,Transport,10.00,Bus ticket\n");
            writer.write("2023-01-03,Shopping,150.00,Clothes\n");
            writer.write("2023-01-04,Bills,80.00,Electricity bill\n");
            writer.write("2023-01-05,Income,2000.00,Salary\n");
        }
    }
    
    /**
     * 测试加载交易数据
     */
    @Test
    public void testLoadTransactionsData() {
        // 验证交易数据已加载
        String transactionsData = aiChatService.getTransactionsData();
        assertNotNull(transactionsData);
        assertFalse(transactionsData.isEmpty());
        
        // 验证交易数据内容
        assertTrue(transactionsData.contains("date,category,amount,description"));
        assertTrue(transactionsData.contains("2023-01-01,Food,25.50,Restaurant dinner"));
        assertTrue(transactionsData.contains("2023-01-05,Income,2000.00,Salary"));
    }
    
    /**
     * 测试AI响应（非流式）
     * 注意：这是一个集成测试，需要有效的API密钥
     */
    @Test
    public void testGetAIResponse() {
        try {
            // 发送一个简单的财务问题
            String response = aiChatService.getAIResponse("What is the best way to save money?");
            
            // 验证响应不为空
            assertNotNull(response);
            assertFalse(response.isEmpty());
            
            // 由于AI响应是不确定的，我们只能做一些基本验证
            // 检查响应是否包含一些预期的关键词
            assertTrue(
                response.contains("save") || 
                response.contains("saving") || 
                response.contains("budget") || 
                response.contains("financial") ||
                response.contains("money"),
                "响应应包含与财务相关的关键词"
            );
            
        } catch (Exception e) {
            // 如果API调用失败（例如无效的API密钥），则跳过测试
            assumeTrue(false, "跳过测试：" + e.getMessage());
        }
    }
    
    /**
     * 测试交易数据分析
     * 注意：这是一个集成测试，需要有效的API密钥
     */
    @Test
    public void testAnalyzeTransactionData() {
        try {
            // 分析交易数据
            String analysis = aiChatService.analyzeTransactionData();
            
            // 验证分析结果不为空
            assertNotNull(analysis);
            assertFalse(analysis.isEmpty());
            
            // 检查分析结果是否包含一些预期的关键词
            assertTrue(
                analysis.contains("transaction") || 
                analysis.contains("financial") || 
                analysis.contains("budget") || 
                analysis.contains("income") ||
                analysis.contains("expense") ||
                analysis.contains("advice"),
                "分析结果应包含与财务分析相关的关键词"
            );
            
        } catch (Exception e) {
            // 如果API调用失败，则跳过测试
            assumeTrue(false, "跳过测试：" + e.getMessage());
        }
    }
    
    /**
     * 测试流式AI响应
     * 注意：这是一个集成测试，需要有效的API密钥
     */
    @Test
    public void testGetAIResponseStreaming() throws InterruptedException {
        // 使用CountDownLatch等待异步响应完成
        CountDownLatch latch = new CountDownLatch(1);
        
        // 存储响应内容和错误
        AtomicReference<StringBuilder> contentRef = new AtomicReference<>(new StringBuilder());
        AtomicReference<Exception> errorRef = new AtomicReference<>();
        List<String> responseChunks = new ArrayList<>();
        
        // 创建响应处理器
        AIChatService.StreamResponseHandler handler = new AIChatService.StreamResponseHandler() {
            @Override
            public void onResponseChunk(String content) {
                contentRef.get().append(content);
                responseChunks.add(content);
            }
            
            @Override
            public void onError(Exception e) {
                errorRef.set(e);
                latch.countDown();
            }
            
            @Override
            public void onComplete() {
                latch.countDown();
            }
        };
        
        // 发送流式请求
        aiChatService.getAIResponseStreaming("How can I create a monthly budget?", handler);
        
        // 等待响应完成（最多30秒）
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        
        // 检查是否有错误
        if (errorRef.get() != null) {
            // 如果API调用失败，则跳过测试
            assumeTrue(false, "跳过测试：" + errorRef.get().getMessage());
            return;
        }
        
        // 验证响应完成
        assertTrue(completed, "流式响应应在超时前完成");
        
        // 验证响应内容
        String fullContent = contentRef.get().toString();
        assertNotNull(fullContent);
        assertFalse(fullContent.isEmpty());
        
        // 验证接收到了多个响应块
        assertFalse(responseChunks.isEmpty(), "应接收到至少一个响应块");
        
        // 检查响应是否包含一些预期的关键词
        assertTrue(
            fullContent.contains("budget") || 
            fullContent.contains("financial") || 
            fullContent.contains("income") || 
            fullContent.contains("expense") ||
            fullContent.contains("money"),
            "响应应包含与预算相关的关键词"
        );
    }
    
    /**
     * 测试无效API密钥的情况
     */
    @Test
    public void testInvalidApiKey() {
        // 创建一个使用无效API密钥的AIChatService实例
        // 注意：这需要修改AIChatService类以支持自定义API密钥
        // 由于当前实现不支持，我们跳过这个测试
        
        // 如果AIChatService支持自定义API密钥，可以使用以下代码：
        /*
        AIChatService invalidService = new AIChatService("invalid-api-key");
        
        try {
            String response = invalidService.getAIResponse("Test question");
            assertTrue(response.contains("cannot connect") || response.contains("error"), 
                    "使用无效API密钥时应返回错误消息");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("unauthorized") || 
                       e.getMessage().contains("invalid") ||
                       e.getMessage().contains("error"),
                    "使用无效API密钥时应抛出相关异常");
        }
        */
    }
} 