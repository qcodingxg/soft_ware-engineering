package com.financeapp.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * UI Constants and Helper Methods
 * Centralized definition of all UI-related constants to ensure consistency
 */
public class UIConstants {
    // Colors
    public static final Color PRIMARY_COLOR = new Color(24, 115, 204);
    public static final Color PRIMARY_DARK_COLOR = new Color(19, 90, 157);
    public static final Color PRIMARY_LIGHT_COLOR = new Color(75, 148, 220);
    
    public static final Color ACCENT_COLOR = new Color(255, 152, 0);
    public static final Color ACCENT_DARK_COLOR = new Color(230, 126, 0);
    
    public static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    public static final Color CARD_BACKGROUND_COLOR = new Color(255, 255, 255);
    public static final Color TEXT_COLOR = new Color(33, 33, 33);
    public static final Color TEXT_SECONDARY_COLOR = new Color(117, 117, 117);
    
    public static final Color ERROR_COLOR = new Color(211, 47, 47);
    public static final Color SUCCESS_COLOR = new Color(46, 174, 96);
    public static final Color WARNING_COLOR = new Color(245, 166, 35);
    public static final Color INFO_COLOR = new Color(3, 169, 244);
    
    // Fonts
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font SUBHEADER_FONT = new Font("SansSerif", Font.BOLD, 16);
    public static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 14);
    
    // Borders and Padding
    public static final int PADDING = 16;
    public static final int PADDING_SMALL = 8;
    public static final int PADDING_LARGE = 24;
    public static final int BORDER_RADIUS = 8;
    
    // Create standard card border
    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 220, 224), 1),
                new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
    }
    
    // Create primary button
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }
    
    // Create secondary button
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(PRIMARY_COLOR);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1));
        return button;
    }
    
    // Create standard label
    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }
    
    // Create title label
    public static JLabel createTitleLabel(String text) {
        return createLabel(text, TITLE_FONT, TEXT_COLOR);
    }
    
    // Create subtitle label
    public static JLabel createSubtitleLabel(String text) {
        return createLabel(text, SUBHEADER_FONT, TEXT_COLOR);
    }
    
    // Create card panel
    public static JPanel createCardPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(PADDING, PADDING));
        panel.setBackground(CARD_BACKGROUND_COLOR);
        panel.setBorder(createCardBorder());
        
        if (title != null && !title.isEmpty()) {
            JLabel titleLabel = createSubtitleLabel(title);
            titleLabel.setBorder(new EmptyBorder(0, 0, PADDING_SMALL, 0));
            panel.add(titleLabel, BorderLayout.NORTH);
        }
        
        return panel;
    }
    
    // Add hover effect to components
    public static void addHoverEffect(AbstractButton button) {
        Color originalBg = button.getBackground();
        Color originalFg = button.getForeground();
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    if (originalBg.equals(PRIMARY_COLOR)) {
                        button.setBackground(PRIMARY_DARK_COLOR);
                    } else if (originalBg.equals(Color.WHITE)) {
                        button.setBackground(new Color(240, 240, 240));
                    }
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(originalBg);
                    button.setForeground(originalFg);
                }
            }
        });
    }
} 