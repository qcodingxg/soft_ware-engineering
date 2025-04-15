package com.financeapp.view;

import com.financeapp.controller.AuthController;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * Register Panel
 * Displays registration form and handles user registration
 */
public class RegisterPanel extends JPanel {
    
    private final AuthController authController;
    private final RegisterCallback callback;
    
    // Form components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField fullNameField;
    private JTextField emailField;
    private JButton registerButton;
    private JButton backButton;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    
    /**
     * Register callback interface
     */
    public interface RegisterCallback {
        void onRegisterSuccess();
        void onBackToLogin();
    }
    
    /**
     * Constructor
     * @param authController Authentication controller
     * @param callback Register callback
     */
    public RegisterPanel(AuthController authController, RegisterCallback callback) {
        this.authController = authController;
        this.callback = callback;
        initUI();
    }
    
    /**
     * Initialize UI
     */
    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(BACKGROUND_COLOR);
        
        // Create registration form
        JPanel registerForm = createRegisterForm();
        
        // Add to panel
        add(registerForm, new GridBagConstraints());
    }
    
    /**
     * Create registration form
     */
    private JPanel createRegisterForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, gbc);
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel usernameLabel = new JLabel("Username*:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameLabel.setForeground(TEXT_COLOR);
        panel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        usernameField = createStyledTextField(20);
        panel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Password*:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setForeground(TEXT_COLOR);
        panel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        passwordField = createStyledPasswordField(20);
        panel.add(passwordField, gbc);
        
        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel confirmPasswordLabel = new JLabel("Confirm Password*:");
        confirmPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        confirmPasswordLabel.setForeground(TEXT_COLOR);
        panel.add(confirmPasswordLabel, gbc);
        
        gbc.gridx = 1;
        confirmPasswordField = createStyledPasswordField(20);
        panel.add(confirmPasswordField, gbc);
        
        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel fullNameLabel = new JLabel("Full Name:");
        fullNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fullNameLabel.setForeground(TEXT_COLOR);
        panel.add(fullNameLabel, gbc);
        
        gbc.gridx = 1;
        fullNameField = createStyledTextField(20);
        panel.add(fullNameField, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailLabel.setForeground(TEXT_COLOR);
        panel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        emailField = createStyledTextField(20);
        panel.add(emailField, gbc);
        
        // Required fields note
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        JLabel requiredLabel = new JLabel("* Required fields");
        requiredLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        requiredLabel.setForeground(TEXT_COLOR);
        panel.add(requiredLabel, gbc);
        
        // Buttons panel
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        // Register button
        registerButton = createStyledButton("Register", SUCCESS_COLOR);
        registerButton.addActionListener(e -> register());
        buttonPanel.add(registerButton);
        
        // Back button
        backButton = createStyledButton("Back to Login", SECONDARY_COLOR);
        backButton.addActionListener(e -> callback.onBackToLogin());
        buttonPanel.add(backButton);
        
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    /**
     * Create styled text field
     * @param columns Number of columns
     * @return Styled text field
     */
    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }
    
    /**
     * Create styled password field
     * @param columns Number of columns
     * @return Styled password field
     */
    private JPasswordField createStyledPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }
    
    /**
     * Create styled button
     * @param text Button text
     * @param color Button color
     * @return Styled button
     */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(150, 40));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    /**
     * Handle register button click
     */
    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        
        // Validate username
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a username", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return;
        }
        
        // Validate password
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a password", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
            passwordField.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, 
                    "Password must be at least 6 characters", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
            passwordField.requestFocus();
            return;
        }
        
        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, 
                    "Passwords do not match", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
            confirmPasswordField.requestFocus();
            return;
        }
        
        // Validate email
        if (!email.isEmpty() && !isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a valid email address", 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
            emailField.requestFocus();
            return;
        }
        
        try {
            // Register the user
            authController.register(username, password, fullName, email);
            
            JOptionPane.showMessageDialog(this, 
                    "Registration successful! You can now log in.", 
                    "Registration Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            callback.onRegisterSuccess();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, 
                    e.getMessage(), 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                    "Error during registration: " + e.getMessage(), 
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Validate email format
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        // Simple email validation, can be improved
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
} 