package com.financeapp.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User model class
 * Represents a registered user in the application
 */
public class User {
    private String id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    
    /**
     * Default constructor
     */
    public User() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Parameterized constructor
     * @param username Username
     * @param passwordHash Hashed password
     * @param fullName User's full name
     * @param email User's email
     */
    public User(String username, String passwordHash, String fullName, String email) {
        this();
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
    }
    
    // Getters and setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    /**
     * Update last login time
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", lastLoginAt=" + lastLoginAt +
                '}';
    }
} 