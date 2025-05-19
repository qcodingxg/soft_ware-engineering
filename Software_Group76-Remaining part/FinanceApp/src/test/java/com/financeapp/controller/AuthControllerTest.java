package com.financeapp.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.financeapp.model.User;

/**
 * Tests for AuthController class
 * Tests user registration, login, and other authentication operations
 */
public class AuthControllerTest {

    private AuthController authController;
    private String testUsername;
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_FULLNAME = "Test User";
    private static final String TEST_EMAIL = "test@example.com";
    
    @TempDir
    Path tempDir;
    
    /**
     * Setup before each test
     * Creates a temporary data directory and initializes the AuthController
     */
    @BeforeEach
    public void setUp() throws IOException {
        // Generate a unique username for each test to avoid conflicts
        testUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Create a temporary data directory
        Path dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);
        
        // Set the system property to use the temporary directory
        System.setProperty("user.dir", tempDir.toString());
        
        // Initialize AuthController
        authController = new AuthController();
    }
    
    /**
     * Cleanup after each test
     */
    @AfterEach
    public void tearDown() {
        // Reset system property
        System.clearProperty("user.dir");
    }
    
    /**
     * Test user registration with valid data
     */
    @Test
    public void testRegisterValidUser() throws IOException {
        // Register a new user
        User user = authController.register(testUsername, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        
        // Verify user was created with correct data
        assertNotNull(user);
        assertEquals(testUsername, user.getUsername());
        assertEquals(TEST_FULLNAME, user.getFullName());
        assertEquals(TEST_EMAIL, user.getEmail());
        assertNotNull(user.getPasswordHash());
        assertNotNull(user.getId());
        assertNotNull(user.getCreatedAt());
    }
    
    /**
     * Test user registration with empty username
     */
    @Test
    public void testRegisterEmptyUsername() {
        // Try to register with empty username
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authController.register("", TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        });
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Username cannot be empty"));
    }
    
    /**
     * Test user registration with short password
     */
    @Test
    public void testRegisterShortPassword() {
        // Try to register with short password
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authController.register(testUsername, "12345", TEST_FULLNAME, TEST_EMAIL);
        });
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Password must be at least 6 characters"));
    }
    
    /**
     * Test user registration with duplicate username
     */
    @Test
    public void testRegisterDuplicateUsername() throws IOException {
        // Register first user
        authController.register(testUsername, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        
        // Try to register with same username
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authController.register(testUsername, "different123", "Another User", "another@example.com");
        });
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Username already exists"));
    }
    
    /**
     * Test successful login
     */
    @Test
    public void testLoginSuccess() throws IOException {
        // Register a user
        authController.register(testUsername, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        
        // Login with correct credentials
        boolean loginResult = authController.login(testUsername, TEST_PASSWORD);
        
        // Verify login was successful
        assertTrue(loginResult);
        assertTrue(authController.isLoggedIn());
        assertNotNull(authController.getCurrentUser());
        assertEquals(testUsername, authController.getCurrentUser().getUsername());
        assertNotNull(authController.getCurrentUser().getLastLoginAt());
    }
    
    /**
     * Test login with incorrect password
     */
    @Test
    public void testLoginWrongPassword() throws IOException {
        // Register a user
        authController.register(testUsername, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        
        // Try to login with wrong password
        boolean loginResult = authController.login(testUsername, "wrongpassword");
        
        // Verify login failed
        assertFalse(loginResult);
        assertFalse(authController.isLoggedIn());
        assertNull(authController.getCurrentUser());
    }
    
    /**
     * Test login with non-existent username
     */
    @Test
    public void testLoginNonExistentUser() throws IOException {
        // Try to login with non-existent username
        boolean loginResult = authController.login("nonexistentuser", TEST_PASSWORD);
        
        // Verify login failed
        assertFalse(loginResult);
        assertFalse(authController.isLoggedIn());
        assertNull(authController.getCurrentUser());
    }
    
    /**
     * Test logout functionality
     */
    @Test
    public void testLogout() throws IOException {
        // Register and login a user
        authController.register(testUsername, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        authController.login(testUsername, TEST_PASSWORD);
        
        // Verify user is logged in
        assertTrue(authController.isLoggedIn());
        
        // Logout
        authController.logout();
        
        // Verify user is logged out
        assertFalse(authController.isLoggedIn());
        assertNull(authController.getCurrentUser());
    }
    
    /**
     * Test change password functionality
     */
    @Test
    public void testChangePassword() throws IOException {
        // Register and login a user
        authController.register(testUsername, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        authController.login(testUsername, TEST_PASSWORD);
        
        // Change password
        String newPassword = "newpassword123";
        boolean changeResult = authController.changePassword(TEST_PASSWORD, newPassword);
        
        // Verify password change was successful
        assertTrue(changeResult);
        
        // Logout
        authController.logout();
        
        // Try to login with old password
        boolean oldLoginResult = authController.login(testUsername, TEST_PASSWORD);
        assertFalse(oldLoginResult);
        
        // Try to login with new password
        boolean newLoginResult = authController.login(testUsername, newPassword);
        assertTrue(newLoginResult);
    }
    
    /**
     * Test change password with incorrect current password
     */
    @Test
    public void testChangePasswordWrongCurrentPassword() throws IOException {
        // Register and login a user
        authController.register(testUsername, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        authController.login(testUsername, TEST_PASSWORD);
        
        // Try to change password with wrong current password
        boolean changeResult = authController.changePassword("wrongpassword", "newpassword123");
        
        // Verify password change failed
        assertFalse(changeResult);
    }
    
    /**
     * Test update profile functionality
     */
    @Test
    public void testUpdateProfile() throws IOException {
        // Register and login a user
        authController.register(testUsername, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        authController.login(testUsername, TEST_PASSWORD);
        
        // Update profile
        String newFullName = "Updated Name";
        String newEmail = "updated@example.com";
        authController.updateProfile(newFullName, newEmail);
        
        // Verify profile was updated
        assertEquals(newFullName, authController.getCurrentUser().getFullName());
        assertEquals(newEmail, authController.getCurrentUser().getEmail());
    }
} 