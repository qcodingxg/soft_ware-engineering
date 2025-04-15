package com.financeapp.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password utility class
 * Provides methods for hashing and verifying passwords
 */
public class PasswordUtils {
    
    private static final int SALT_LENGTH = 16;
    
    /**
     * Hash a password
     * @param password Raw password
     * @return Hashed password (salt:hash)
     */
    public static String hashPassword(String password) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash the password with the salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Encode salt and hash as Base64
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);
            
            // Return salt:hash
            return saltBase64 + ":" + hashBase64;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verify a password against a stored hash
     * @param password Raw password
     * @param storedHash Stored hash (salt:hash)
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Split the stored hash into salt and hash
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            // Decode salt and hash from Base64
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);
            
            // Hash the input password with the same salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] inputHash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Compare the hashes
            if (hash.length != inputHash.length) {
                return false;
            }
            
            // Time-constant comparison to prevent timing attacks
            int diff = 0;
            for (int i = 0; i < hash.length; i++) {
                diff |= hash[i] ^ inputHash[i];
            }
            
            return diff == 0;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error verifying password", e);
        }
    }
    
    /**
     * Generate a random password
     * @param length Password length
     * @return Random password
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            length = 8; // Minimum password length
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
} 