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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for AIChatService class
 * Tests AI chat functionality and transaction data analysis
 */
public class AIChatServiceTest {

    private AIChatService aiChatService;
    
    @TempDir
    Path tempDir;
    
    private Path dataDir;
    private Path transactionsFile;
    private String originalUserDir;
    
    /**
     * Setup before each test
     * Creates temporary data directory and test transaction data file
     */
    @BeforeEach
    public void setUp() throws IOException {
        // Save original user directory
        originalUserDir = System.getProperty("user.dir");
        
        // Create temporary data directory
        dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);
        
        // Create test transaction data file
        transactionsFile = dataDir.resolve("transactions.csv");
        createTestTransactionsFile(transactionsFile.toFile());
        
        // Set system property to use temporary directory
        System.setProperty("user.dir", tempDir.toString());
        
        // Initialize AIChatService
        aiChatService = new AIChatService();
    }
    
    /**
     * Cleanup after each test
     */
    @AfterEach
    public void tearDown() {
        // Restore original user directory
        System.setProperty("user.dir", originalUserDir);
    }
    
    /**
     * Create test transaction data file
     */
    private void createTestTransactionsFile(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write CSV header
            writer.write("日期,分类,金额,备注\n");
            // Write some test transaction data
            writer.write("2023-01-01,Food,25.50,Restaurant dinner\n");
            writer.write("2023-01-02,Transport,10.00,Bus ticket\n");
            writer.write("2023-01-03,Shopping,150.00,Clothes\n");
            writer.write("2023-01-04,Bills,80.00,Electricity bill\n");
            writer.write("2023-01-05,Income,2000.00,Salary\n");
        }
    }
    
    /**
     * Test loading transaction data
     */
    @Test
    public void testLoadTransactionsData() {
        // Verify transaction data is loaded
        String transactionsData = aiChatService.getTransactionsData();
        assertNotNull(transactionsData);
        assertFalse(transactionsData.isEmpty());
        
        // Verify transaction data content
        assertTrue(transactionsData.contains("日期,分类,金额,备注"));
        assertTrue(transactionsData.contains("2025-03-18,Food,300.00,Electric bill"));
        assertTrue(transactionsData.contains("2025-03-18,Uncategorized,8908.00,Initial deposit"));
    }
    
    /**
     * Test AI response (non-streaming)
     * Note: This is an integration test that requires a valid API key
     * Disabled by default to avoid test failures in CI environments
     */
    @Test
    @Disabled("Requires valid API key and network connection")
    public void testGetAIResponse() {
        try {
            // Send a simple financial question
            String response = aiChatService.getAIResponse("What is the best way to save money?");
            
            // Verify response is not empty
            assertNotNull(response);
            assertFalse(response.isEmpty());
            
            // Since AI responses are non-deterministic, we can only do basic validations
            // Check if response contains some expected keywords
            assertTrue(
                response.contains("save") || 
                response.contains("saving") || 
                response.contains("budget") || 
                response.contains("financial") ||
                response.contains("money"),
                "Response should contain finance-related keywords"
            );
            
        } catch (Exception e) {
            // Skip test if API call fails (e.g., invalid API key)
            assumeTrue(false, "Skipping test: " + e.getMessage());
        }
    }
    
    /**
     * Test transaction data analysis
     * Note: This is an integration test that requires a valid API key
     * Disabled by default to avoid test failures in CI environments
     */
    @Test
    @Disabled("Requires valid API key and network connection")
    public void testAnalyzeTransactionData() {
        try {
            // Analyze transaction data
            String analysis = aiChatService.analyzeTransactionData();
            
            // Verify analysis result is not empty
            assertNotNull(analysis);
            assertFalse(analysis.isEmpty());
            
            // Check if analysis result contains some expected keywords
            assertTrue(
                analysis.contains("transaction") || 
                analysis.contains("financial") || 
                analysis.contains("budget") || 
                analysis.contains("income") ||
                analysis.contains("expense") ||
                analysis.contains("advice"),
                "Analysis result should contain financial analysis keywords"
            );
            
        } catch (Exception e) {
            // Skip test if API call fails
            assumeTrue(false, "Skipping test: " + e.getMessage());
        }
    }
    
    /**
     * Test streaming AI response
     * Note: This is an integration test that requires a valid API key
     * Disabled by default to avoid test failures in CI environments
     */
    @Test
    @Disabled("Requires valid API key and network connection")
    public void testGetAIResponseStreaming() throws InterruptedException {
        // Use CountDownLatch to wait for async response completion
        CountDownLatch latch = new CountDownLatch(1);
        
        // Store response content and errors
        AtomicReference<StringBuilder> contentRef = new AtomicReference<>(new StringBuilder());
        AtomicReference<Exception> errorRef = new AtomicReference<>();
        List<String> responseChunks = new ArrayList<>();
        
        // Create response handler
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
        
        // Send streaming request
        aiChatService.getAIResponseStreaming("How can I create a monthly budget?", handler);
        
        // Wait for response to complete (max 30 seconds)
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        
        // Check for errors
        if (errorRef.get() != null) {
            // Skip test if API call fails
            assumeTrue(false, "Skipping test: " + errorRef.get().getMessage());
            return;
        }
        
        // Verify response completed
        assertTrue(completed, "Streaming response should complete before timeout");
        
        // Verify response content
        String fullContent = contentRef.get().toString();
        assertNotNull(fullContent);
        assertFalse(fullContent.isEmpty());
        
        // Verify multiple response chunks were received
        assertFalse(responseChunks.isEmpty(), "Should receive at least one response chunk");
        
        // Check if response contains some expected keywords
        assertTrue(
            fullContent.contains("budget") || 
            fullContent.contains("financial") || 
            fullContent.contains("income") || 
            fullContent.contains("expense") ||
            fullContent.contains("money"),
            "Response should contain budget-related keywords"
        );
    }
    
    /**
     * Test invalid API key scenario
     * Disabled by default as it requires modifying the AIChatService class
     */
    @Test
    @Disabled("Requires modification to AIChatService to support custom API keys")
    public void testInvalidApiKey() {
        // Create an AIChatService instance with invalid API key
        // Note: This requires modifying the AIChatService class to support custom API keys
        // Since the current implementation doesn't support this, we skip this test
        
        // If AIChatService supports custom API keys, the following code could be used:
        /*
        AIChatService invalidService = new AIChatService("invalid-api-key");
        
        try {
            String response = invalidService.getAIResponse("Test question");
            assertTrue(response.contains("cannot connect") || response.contains("error"), 
                    "Should return error message when using invalid API key");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("unauthorized") || 
                       e.getMessage().contains("invalid") ||
                       e.getMessage().contains("error"),
                    "Should throw relevant exception when using invalid API key");
        }
        */
    }
} 