package com.financeapp.model;

import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.JSONObject;

/**
 * AI Classifier
 * Implements simple classification logic and holiday detection
 */
public class AIClassifier {
    private final Map<String, String> userCorrections;
    private static final String CORRECTIONS_LOG_PATH = "./data/corrections.log";
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String DEEPSEEK_API_KEY = "sk-1b18b92f15b84a2a98b510300e8fbc28";

    /**
     * Initialize classifier
     */
    public AIClassifier() {
        userCorrections = new HashMap<>();
        loadCorrections();
    }

    /**
     * Classify a transaction
     * @param transaction Transaction to classify
     * @return Classified transaction
     */
    public Transaction classify(Transaction transaction) throws IOException {
        // Skip if already classified
        if (transaction.getCategory() != null && !transaction.getCategory().equals("Uncategorized")) {
            return transaction;
        }

        if (isHolidayPeriod(transaction.getDate())) {
            transaction.setCategory("Holiday");
            return transaction;
        }
        String description = transaction.getDescription();

        // Check user corrections first
        if (userCorrections.containsKey(description)) {
            transaction.setCategory(userCorrections.get(description));
            return transaction;
        }

        // Call DeepSeek API for dynamic classification
        String category = classifyWithDeepSeek(description);
        if (category != null && !category.equals("Uncategorized")) {
            transaction.setCategory(category);
        } else {
            transaction.setCategory("Uncategorized"); // Fallback
        }

        return transaction;
    }

    /**
     * Classify using AI API
     */
    private String classifyWithDeepSeek(String description) throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "deepseek-chat"); // Use the appropriate model
        requestBody.put("messages", new JSONObject[] {
                new JSONObject()
                        .put("role", "user")
                        .put("content", "Classify the following transaction description into one of these categories: " +
                        "Food, Transportation, Shopping, Entertainment, Utilities, Holiday, or Uncategorized. " +
                        "Description: " + description + "\n\nReturn ONLY the category name.")
        });

        HttpURLConnection connection = (HttpURLConnection) new URL(DEEPSEEK_API_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + DEEPSEEK_API_KEY);
        connection.setDoOutput(true);

        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.toString().getBytes());
        }

        // Parse response
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            String content = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            return content.trim(); // e.g., "Food" or "Transportation"
        } catch (Exception e) {
            System.err.println("DeepSeek API call failed: " + e.getMessage());
            return "Uncategorized"; // Fallback on error
        }
    }


    /**
     * Record user correction (unchanged)
     */
    public void recordCorrection(Transaction transaction, String newCategory) {
        String description = transaction.getDescription();
        userCorrections.put(description, newCategory);

        try (FileWriter writer = new FileWriter(CORRECTIONS_LOG_PATH, true)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = LocalDateTime.now().format(formatter);
            writer.write(String.format("%s|%s|%s|%s\n",
                    description,
                    newCategory,
                    transaction.getCategory(),
                    timestamp));
        } catch (IOException e) {
            System.err.println("Failed to save correction: " + e.getMessage());
        }
    }

    /**
     * Load user corrections (unchanged)
     */
    private void loadCorrections() {
        try {
            if (Files.exists(Paths.get(CORRECTIONS_LOG_PATH))) {
                List<String> lines = Files.readAllLines(Paths.get(CORRECTIONS_LOG_PATH));
                for (String line : lines) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 2) {
                        userCorrections.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load corrections: " + e.getMessage());
        }
    }

    /**
     * Check if date is during a holiday period
     * Simplified implementation, using sample logic
     * TODO: Use professional calendar library
     */
    private boolean isHolidayPeriod(LocalDate date) {
        // This is just an example, in practice should use a proper calendar library
        // For demonstration, assume that 2024 Spring Festival is Feb 10 (lunar new year), range from Feb 4 to Feb 24
        if (date.getYear() == 2024) {
            LocalDate startDate = LocalDate.of(2024, 2, 4);
            LocalDate endDate = LocalDate.of(2024, 2, 24);

            return !date.isBefore(startDate) && !date.isAfter(endDate);
        }

        // TODO: Add more holiday date ranges, or use calendar library to calculate automatically

        return false;
    }
}