package com.financeapp.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Background Panel
 * A JPanel with background image support
 */
public class BackgroundPanel extends JPanel {
    
    private BufferedImage backgroundImage;
    private int imageMode = SCALED;
    
    // Constants for image display mode
    public static final int TILED = 0;
    public static final int SCALED = 1;
    public static final int ACTUAL = 2;
    
    /**
     * Constructor with image path
     * @param imagePath Path to the image file
     * @param mode Image display mode
     */
    public BackgroundPanel(String imagePath, int mode) {
        this.imageMode = mode;
        try {
            // First try to load as a resource
            InputStream is = getClass().getClassLoader().getResourceAsStream(imagePath);
            if (is != null) {
                backgroundImage = ImageIO.read(is);
            } else {
                // If not found as resource, try as file path
                backgroundImage = ImageIO.read(new File(imagePath));
            }
        } catch (IOException e) {
            System.err.println("Error loading background image: " + e.getMessage());
            // Try to load with just the filename part
            try {
                String fileName = new File(imagePath).getName();
                InputStream is = getClass().getClassLoader().getResourceAsStream("image/" + fileName);
                if (is != null) {
                    backgroundImage = ImageIO.read(is);
                }
            } catch (Exception ex) {
                System.err.println("Failed to load image from all paths: " + ex.getMessage());
            }
        }
        setLayout(new BorderLayout());
    }
    
    /**
     * Constructor with resource path
     * @param resourcePath Resource path to the image file
     * @param isResource Flag indicating if this is a resource path
     * @param mode Image display mode
     */
    public BackgroundPanel(String resourcePath, boolean isResource, int mode) {
        this.imageMode = mode;
        try {
            if (isResource) {
                InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
                if (is != null) {
                    backgroundImage = ImageIO.read(is);
                } else {
                    System.err.println("Resource not found: " + resourcePath);
                }
            } else {
                backgroundImage = ImageIO.read(new File(resourcePath));
            }
        } catch (IOException e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }
        setLayout(new BorderLayout());
    }
    
    /**
     * Set the background image
     * @param imagePath Path to the image file
     */
    public void setBackgroundImage(String imagePath) {
        try {
            // First try to load as a resource
            InputStream is = getClass().getClassLoader().getResourceAsStream(imagePath);
            if (is != null) {
                backgroundImage = ImageIO.read(is);
            } else {
                // If not found as resource, try as file path
                backgroundImage = ImageIO.read(new File(imagePath));
            }
            repaint();
        } catch (IOException e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }
    }
    
    /**
     * Set the display mode
     * @param mode Display mode (TILED, SCALED, or ACTUAL)
     */
    public void setImageMode(int mode) {
        this.imageMode = mode;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (backgroundImage == null) {
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        
        switch (imageMode) {
            case TILED:
                // Tile the image across the panel
                int width = getWidth();
                int height = getHeight();
                int imageWidth = backgroundImage.getWidth();
                int imageHeight = backgroundImage.getHeight();
                
                for (int x = 0; x < width; x += imageWidth) {
                    for (int y = 0; y < height; y += imageHeight) {
                        g2d.drawImage(backgroundImage, x, y, this);
                    }
                }
                break;
                
            case SCALED:
                // Scale the image to fit the panel
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                break;
                
            case ACTUAL:
                // Draw the image at its actual size
                g2d.drawImage(backgroundImage, 0, 0, this);
                break;
        }
    }
} 