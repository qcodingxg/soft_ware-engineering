package com.financeapp.view;

import com.financeapp.controller.AuthController;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * Login Panel
 * Displays login form and handles login validation
 */
public class LoginPanel extends JPanel {
    
    private final AuthController authController;
    private final LoginCallback callback;
    
    // Form components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    
    /**
     * Login callback interface
     */
    public interface LoginCallback {
        void onLoginSuccess();
        void onRegisterRequest();
    }
    
    /**
     * Constructor
     * @param authController Authentication controller
     * @param callback Login callback
     */
    public LoginPanel(AuthController authController, LoginCallback callback) {
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
        
        // Create login form
        JPanel loginForm = createLoginForm();
        
        // Add to panel
        add(loginForm, new GridBagConstraints());
    }
    
    /**
     * Create login form
     */
    private JPanel createLoginForm() {
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
        JLabel titleLabel = new JLabel("Finance App Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, gbc);
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameLabel.setForeground(TEXT_COLOR);
        panel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        usernameField = createStyledTextField(15);
        panel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setForeground(TEXT_COLOR);
        panel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setForeground(TEXT_COLOR);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panel.add(passwordField, gbc);
        
        // Buttons panel
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        // Login button
        loginButton = createStyledButton("Login", PRIMARY_COLOR);
        loginButton.addActionListener(e -> login());
        buttonPanel.add(loginButton);
        
        // Register button
        registerButton = createStyledButton("Register", SECONDARY_COLOR);
        registerButton.addActionListener(e -> callback.onRegisterRequest());
        buttonPanel.add(registerButton);
        
        panel.add(buttonPanel, gbc);
        
        // Add enter key listener
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        });
        
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
        button.setPreferredSize(new Dimension(100, 40));
        
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
     * Handle login button click
     */
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter your username", 
                    "Login Error", 
                    JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter your password", 
                    "Login Error", 
                    JOptionPane.ERROR_MESSAGE);
            passwordField.requestFocus();
            return;
        }
        
        try {
            boolean success = authController.login(username, password);
            
            if (success) {
                callback.onLoginSuccess();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Invalid username or password", 
                        "Login Failed", 
                        JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                passwordField.requestFocus();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                    "Error during login: " + e.getMessage(), 
                    "Login Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
} 