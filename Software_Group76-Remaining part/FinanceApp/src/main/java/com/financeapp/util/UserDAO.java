package com.financeapp.util;

import com.financeapp.model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User Data Access Object
 * Handles persistence of user data
 */
public class UserDAO {
    
    private static final String USERS_CSV_PATH = "./data/users.csv";
    private static final String CSV_HEADER = "id,username,password_hash,full_name,email,created_at,last_login_at";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private Map<String, User> usersCache;
    
    /**
     * Constructor
     * Initializes user cache
     */
    public UserDAO() {
        this.usersCache = new HashMap<>();
        init();
    }
    
    /**
     * Initialize the DAO
     * Creates users file if it doesn't exist
     */
    private void init() {
        File file = new File(USERS_CSV_PATH);
        
        // Create data directory if it doesn't exist
        File dataDir = file.getParentFile();
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        // Create users file if it doesn't exist
        if (!file.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(CSV_HEADER);
                writer.newLine();
            } catch (IOException e) {
                System.err.println("Error initializing users file: " + e.getMessage());
            }
        }
        
        // Load users into cache
        loadUsers();
    }
    
    /**
     * Load users from CSV into cache
     */
    private void loadUsers() {
        usersCache.clear();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_CSV_PATH))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                User user = fromCSV(line);
                if (user != null) {
                    usersCache.put(user.getUsername().toLowerCase(), user);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }
    
    /**
     * Create user from CSV line
     * @param csvLine CSV line
     * @return User object
     */
    private User fromCSV(String csvLine) {
        try {
            String[] parts = csvLine.split(",");
            if (parts.length < 5) {
                return null;
            }
            
            User user = new User();
            user.setId(parts[0]);
            user.setUsername(parts[1]);
            user.setPasswordHash(parts[2]);
            user.setFullName(parts[3]);
            user.setEmail(parts[4]);
            
            // Parse dates if available
            if (parts.length > 5 && !parts[5].isEmpty()) {
                user.setCreatedAt(LocalDateTime.parse(parts[5], DATE_FORMATTER));
            }
            
            if (parts.length > 6 && !parts[6].isEmpty()) {
                user.setLastLoginAt(LocalDateTime.parse(parts[6], DATE_FORMATTER));
            }
            
            return user;
        } catch (Exception e) {
            System.err.println("Error parsing user CSV line: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert user to CSV line
     * @param user User object
     * @return CSV line
     */
    private String toCSV(User user) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(user.getId()).append(",");
        sb.append(user.getUsername()).append(",");
        sb.append(user.getPasswordHash()).append(",");
        sb.append(user.getFullName()).append(",");
        sb.append(user.getEmail()).append(",");
        
        // Format dates
        if (user.getCreatedAt() != null) {
            sb.append(user.getCreatedAt().format(DATE_FORMATTER));
        }
        sb.append(",");
        
        if (user.getLastLoginAt() != null) {
            sb.append(user.getLastLoginAt().format(DATE_FORMATTER));
        }
        
        return sb.toString();
    }
    
    /**
     * Save all users to CSV
     */
    private void saveUsers() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_CSV_PATH))) {
            // Write header
            writer.write(CSV_HEADER);
            writer.newLine();
            
            // Write users
            for (User user : usersCache.values()) {
                writer.write(toCSV(user));
                writer.newLine();
            }
        }
    }
    
    /**
     * Get all users
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(usersCache.values());
    }
    
    /**
     * Get user by username
     * @param username Username
     * @return User object or null if not found
     */
    public User getUserByUsername(String username) {
        return usersCache.get(username.toLowerCase());
    }
    
    /**
     * Create a new user
     * @param user User to create
     * @throws IOException if error occurs during saving
     */
    public void createUser(User user) throws IOException {
        if (usersCache.containsKey(user.getUsername().toLowerCase())) {
            throw new RuntimeException("Username already exists");
        }
        
        usersCache.put(user.getUsername().toLowerCase(), user);
        saveUsers();
    }
    
    /**
     * Update an existing user
     * @param user User to update
     * @throws IOException if error occurs during saving
     */
    public void updateUser(User user) throws IOException {
        if (!usersCache.containsKey(user.getUsername().toLowerCase())) {
            throw new RuntimeException("User not found");
        }
        
        usersCache.put(user.getUsername().toLowerCase(), user);
        saveUsers();
    }
    
    /**
     * Delete a user
     * @param username Username of user to delete
     * @throws IOException if error occurs during saving
     */
    public void deleteUser(String username) throws IOException {
        if (usersCache.remove(username.toLowerCase()) != null) {
            saveUsers();
        }
    }
} 