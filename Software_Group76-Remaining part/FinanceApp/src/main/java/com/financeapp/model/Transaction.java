package com.financeapp.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Entity class representing a financial transaction
 * Contains basic information like date, amount, category, description etc.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private LocalDate date;
    private String category;
    private double amount;
    private String description;
    
    // Default constructor
    public Transaction() {
        this.date = LocalDate.now();
        this.category = "Uncategorized";
        this.amount = 0.0;
        this.description = "";
    }
    
    // Parameterized constructor
    public Transaction(LocalDate date, String category, double amount, String description) {
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }
    
    // Getter and Setter methods
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Convert to CSV format
     * @return CSV formatted string
     */
    public String toCSV() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s,%s,%.2f,%s",
                date.format(formatter),
                category,
                amount,
                description);
    }
    
    /**
     * Create Transaction from CSV line
     * @param csvLine CSV formatted line
     * @return Transaction object
     * @throws IllegalArgumentException if the CSV format is invalid
     */
    public static Transaction fromCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV line cannot be null or empty");
        }
        
        String[] parts = csvLine.split(",", -1); // Keep empty fields
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid CSV format: " + csvLine + ". Expected at least 3 fields.");
        }
        
        try {
            LocalDate date = LocalDate.parse(parts[0].trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String category = parts[1].trim();
            if (category.isEmpty()) {
                category = "Uncategorized";
            }
            
            double amount;
            try {
                amount = Double.parseDouble(parts[2].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid amount: " + parts[2], e);
            }
            
            String description = parts.length > 3 ? parts[3].trim() : "";
            
            return new Transaction(date, category, amount, description);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + parts[0] + ". Expected yyyy-MM-dd", e);
        }
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "date=" + date +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Double.compare(that.amount, amount) == 0 &&
                date.equals(that.date) &&
                category.equals(that.category) &&
                description.equals(that.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(date, category, amount, description);
    }
} 