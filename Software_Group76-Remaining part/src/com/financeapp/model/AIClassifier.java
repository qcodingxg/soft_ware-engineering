package com.financeapp.model;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * AI Classifier
 * Implements simple classification logic and holiday detection
 */
public class AIClassifier {
    private final Map<String, List<Pattern>> categoryPatterns;
    private final Map<String, String> userCorrections;
    private static final String CORRECTIONS_LOG_PATH = "./data/corrections.log";
    
    /**
     * Initialize classifier
     */
    public AIClassifier() {
        categoryPatterns = new HashMap<>();
        userCorrections = new HashMap<>();
        
        // Initialize default classification rules
        initDefaultPatterns();
        
        // Load user correction records
        loadCorrections();
    }
    
    /**
     * Initialize default classification rules
     */
    private void initDefaultPatterns() {
        // Food category
        List<Pattern> foodPatterns = new ArrayList<>();
        foodPatterns.add(Pattern.compile(".*restaurant.*", Pattern.CASE_INSENSITIVE));
        foodPatterns.add(Pattern.compile(".*coffee.*", Pattern.CASE_INSENSITIVE));
        foodPatterns.add(Pattern.compile(".*diner.*", Pattern.CASE_INSENSITIVE));
        foodPatterns.add(Pattern.compile(".*lunch.*", Pattern.CASE_INSENSITIVE));
        foodPatterns.add(Pattern.compile(".*dinner.*", Pattern.CASE_INSENSITIVE));
        foodPatterns.add(Pattern.compile(".*breakfast.*", Pattern.CASE_INSENSITIVE));
        foodPatterns.add(Pattern.compile(".*takeout.*", Pattern.CASE_INSENSITIVE));
        foodPatterns.add(Pattern.compile(".*food.*", Pattern.CASE_INSENSITIVE));
        categoryPatterns.put("Food", foodPatterns);
        
        // Transportation category
        List<Pattern> transportPatterns = new ArrayList<>();
        transportPatterns.add(Pattern.compile(".*bus.*", Pattern.CASE_INSENSITIVE));
        transportPatterns.add(Pattern.compile(".*subway.*", Pattern.CASE_INSENSITIVE));
        transportPatterns.add(Pattern.compile(".*taxi.*", Pattern.CASE_INSENSITIVE));
        transportPatterns.add(Pattern.compile(".*uber.*", Pattern.CASE_INSENSITIVE));
        transportPatterns.add(Pattern.compile(".*lyft.*", Pattern.CASE_INSENSITIVE));
        transportPatterns.add(Pattern.compile(".*gas.*", Pattern.CASE_INSENSITIVE));
        transportPatterns.add(Pattern.compile(".*transport.*", Pattern.CASE_INSENSITIVE));
        categoryPatterns.put("Transportation", transportPatterns);
        
        // Shopping category
        List<Pattern> shoppingPatterns = new ArrayList<>();
        shoppingPatterns.add(Pattern.compile(".*supermarket.*", Pattern.CASE_INSENSITIVE));
        shoppingPatterns.add(Pattern.compile(".*mall.*", Pattern.CASE_INSENSITIVE));
        shoppingPatterns.add(Pattern.compile(".*shopping.*", Pattern.CASE_INSENSITIVE));
        shoppingPatterns.add(Pattern.compile(".*amazon.*", Pattern.CASE_INSENSITIVE));
        shoppingPatterns.add(Pattern.compile(".*walmart.*", Pattern.CASE_INSENSITIVE));
        shoppingPatterns.add(Pattern.compile(".*target.*", Pattern.CASE_INSENSITIVE));
        categoryPatterns.put("Shopping", shoppingPatterns);
        
        // Entertainment category
        List<Pattern> entertainmentPatterns = new ArrayList<>();
        entertainmentPatterns.add(Pattern.compile(".*movie.*", Pattern.CASE_INSENSITIVE));
        entertainmentPatterns.add(Pattern.compile(".*game.*", Pattern.CASE_INSENSITIVE));
        entertainmentPatterns.add(Pattern.compile(".*ktv.*", Pattern.CASE_INSENSITIVE));
        entertainmentPatterns.add(Pattern.compile(".*show.*", Pattern.CASE_INSENSITIVE));
        entertainmentPatterns.add(Pattern.compile(".*theater.*", Pattern.CASE_INSENSITIVE));
        entertainmentPatterns.add(Pattern.compile(".*entertainment.*", Pattern.CASE_INSENSITIVE));
        categoryPatterns.put("Entertainment", entertainmentPatterns);
        
        // Utilities category
        List<Pattern> utilitiesPatterns = new ArrayList<>();
        utilitiesPatterns.add(Pattern.compile(".*water.*", Pattern.CASE_INSENSITIVE));
        utilitiesPatterns.add(Pattern.compile(".*electric.*", Pattern.CASE_INSENSITIVE));
        utilitiesPatterns.add(Pattern.compile(".*gas bill.*", Pattern.CASE_INSENSITIVE));
        utilitiesPatterns.add(Pattern.compile(".*internet.*", Pattern.CASE_INSENSITIVE));
        utilitiesPatterns.add(Pattern.compile(".*phone.*", Pattern.CASE_INSENSITIVE));
        utilitiesPatterns.add(Pattern.compile(".*utility.*", Pattern.CASE_INSENSITIVE));
        categoryPatterns.put("Utilities", utilitiesPatterns);
    }
    
    /**
     * Load user correction records
     */
    private void loadCorrections() {
        try {
            if (Files.exists(Paths.get(CORRECTIONS_LOG_PATH))) {
                List<String> lines = Files.readAllLines(Paths.get(CORRECTIONS_LOG_PATH));
                for (String line : lines) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 2) {
                        String description = parts[0].trim();
                        String category = parts[1].trim();
                        userCorrections.put(description, category);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load correction records: " + e.getMessage());
        }
    }
    
    /**
     * Classify a transaction
     * @param transaction Transaction to classify
     * @return Classified transaction
     */
    public Transaction classify(Transaction transaction) {
        // If already classified and not "Uncategorized", don't classify
        if (transaction.getCategory() != null && !transaction.getCategory().equals("Uncategorized")) {
            return transaction;
        }
        
        String description = transaction.getDescription();
        
        // First check user corrections
        if (userCorrections.containsKey(description)) {
            transaction.setCategory(userCorrections.get(description));
            return transaction;
        }
        
        // Apply classification rules
        for (Map.Entry<String, List<Pattern>> entry : categoryPatterns.entrySet()) {
            String category = entry.getKey();
            List<Pattern> patterns = entry.getValue();
            
            for (Pattern pattern : patterns) {
                if (pattern.matcher(description).matches()) {
                    transaction.setCategory(category);
                    return transaction;
                }
            }
        }
        
        // Check if during Spring Festival period
        if (isHolidayPeriod(transaction.getDate())) {
            transaction.setCategory("Holiday");
            return transaction;
        }
        
        // If no matching rule, keep as "Uncategorized"
        transaction.setCategory("Uncategorized");
        return transaction;
    }
    
    /**
     * Record user correction
     * @param transaction Original transaction
     * @param newCategory New category
     */
    public void recordCorrection(Transaction transaction, String newCategory) {
        String description = transaction.getDescription();
        userCorrections.put(description, newCategory);
        
        // Save to log file
        try (FileWriter writer = new FileWriter(CORRECTIONS_LOG_PATH, true)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = LocalDateTime.now().format(formatter); // Using LocalDateTime instead of LocalDate
            writer.write(String.format("%s|%s|%s|%s\n", 
                    description, 
                    newCategory, 
                    transaction.getCategory(), // Original category
                    timestamp));
        } catch (IOException e) {
            System.err.println("Failed to save correction record: " + e.getMessage());
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