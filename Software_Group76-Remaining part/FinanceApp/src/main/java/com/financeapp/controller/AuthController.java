package com.financeapp.controller;

import com.financeapp.model.User;
import com.financeapp.util.PasswordUtils;
import com.financeapp.util.UserDAO;

import java.io.IOException;

/**
 * Authentication Controller
 * Handles user authentication, registration, and session management
 */
public class AuthController {
    
    private final UserDAO userDAO;
    private User currentUser;
    
    /**
     * Constructor
     */
    public AuthController() {
        this.userDAO = new UserDAO();
        this.currentUser = null;
    }
    
    /**
     * Check if a user is currently logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Get the currently logged in user
     * @return Current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Register a new user
     * @param username Username
     * @param password Password
     * @param fullName Full name
     * @param email Email address
     * @return Newly created user
     * @throws IOException if error occurs during user creation
     */
    public User register(String username, String password, String fullName, String email) throws IOException {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        
        // Check if username already exists
        if (userDAO.getUserByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Hash the password
        String passwordHash = PasswordUtils.hashPassword(password);
        
        // Create the user
        User user = new User(username, passwordHash, fullName, email);
        userDAO.createUser(user);
        
        return user;
    }
    
    /**
     * Login with username and password
     * @param username Username
     * @param password Password
     * @return true if login successful, false otherwise
     * @throws IOException if error occurs during user update
     */
    public boolean login(String username, String password) throws IOException {
        if (username == null || password == null) {
            return false;
        }
        
        // Get the user
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            return false;
        }
        
        // Verify the password
        if (!PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            return false;
        }
        
        // Update last login time
        user.updateLastLogin();
        userDAO.updateUser(user);
        
        // Set current user
        currentUser = user;
        
        return true;
    }
    
    /**
     * Logout the current user
     */
    public void logout() {
        currentUser = null;
    }
    
    /**
     * Change password for the current user
     * @param currentPassword Current password
     * @param newPassword New password
     * @return true if password change successful, false otherwise
     * @throws IOException if error occurs during user update
     */
    public boolean changePassword(String currentPassword, String newPassword) throws IOException {
        if (!isLoggedIn()) {
            return false;
        }
        
        // Verify current password
        if (!PasswordUtils.verifyPassword(currentPassword, currentUser.getPasswordHash())) {
            return false;
        }
        
        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters");
        }
        
        // Hash the new password
        String newPasswordHash = PasswordUtils.hashPassword(newPassword);
        currentUser.setPasswordHash(newPasswordHash);
        
        // Update the user
        userDAO.updateUser(currentUser);
        
        return true;
    }
    
    /**
     * Update user profile
     * @param fullName New full name
     * @param email New email
     * @throws IOException if error occurs during user update
     */
    public void updateProfile(String fullName, String email) throws IOException {
        if (!isLoggedIn()) {
            throw new IllegalStateException("No user is logged in");
        }
        
        currentUser.setFullName(fullName);
        currentUser.setEmail(email);
        
        userDAO.updateUser(currentUser);
    }
} 