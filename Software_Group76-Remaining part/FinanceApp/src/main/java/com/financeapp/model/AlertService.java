package com.financeapp.model;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Alert Service
 * Handles alert generation, persistence, and notification
 */
public class AlertService {

    private static final String ALERT_HISTORY_FILE = "data/alert_history.csv";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Alert levels
    public enum AlertLevel {
        INFO, 
        WARNING, 
        CRITICAL
    }
    
    /**
     * Alert data model
     */
    public static class Alert implements Serializable {
        private final String id;
        private final String message;
        private final AlertLevel level;
        private final LocalDateTime timestamp;
        private final String category;
        private boolean acknowledged;
        
        public Alert(String message, AlertLevel level, String category) {
            this.id = UUID.randomUUID().toString();
            this.message = message;
            this.level = level;
            this.timestamp = LocalDateTime.now();
            this.category = category;
            this.acknowledged = false;
        }
        
        public String getId() {
            return id;
        }
        
        public String getMessage() {
            return message;
        }
        
        public AlertLevel getLevel() {
            return level;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public String getCategory() {
            return category;
        }
        
        public boolean isAcknowledged() {
            return acknowledged;
        }
        
        public void setAcknowledged(boolean acknowledged) {
            this.acknowledged = acknowledged;
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %s", 
                    timestamp.format(DATE_FORMAT), 
                    level, 
                    message);
        }
        
        /**
         * Convert to CSV format
         */
        public String toCsv() {
            return String.join(",", 
                    id,
                    message.replace(",", ";"),  // Escape commas
                    level.toString(),
                    timestamp.format(DATE_FORMAT),
                    category,
                    String.valueOf(acknowledged));
        }
        
        /**
         * Parse from CSV format
         */
        public static Alert fromCsv(String csv) {
            String[] parts = csv.split(",", 6);  // Limit to 6 parts to handle commas in message
            if (parts.length < 6) return null;
            
            String id = parts[0];
            String message = parts[1].replace(";", ","); // Unescape commas
            AlertLevel level = AlertLevel.valueOf(parts[2]);
            LocalDateTime timestamp = LocalDateTime.parse(parts[3], DATE_FORMAT);
            String category = parts[4];
            boolean acknowledged = Boolean.parseBoolean(parts[5]);
            
            Alert alert = new Alert(message, level, category);
            // Use reflection to set final fields
            try {
                java.lang.reflect.Field idField = Alert.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(alert, id);
                
                java.lang.reflect.Field timestampField = Alert.class.getDeclaredField("timestamp");
                timestampField.setAccessible(true);
                timestampField.set(alert, timestamp);
                
                alert.setAcknowledged(acknowledged);
                return alert;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    
    // In-memory cache of alerts
    private List<Alert> alertHistory = new ArrayList<>();
    
    // Notification handlers
    private List<AlertNotificationHandler> notificationHandlers = new ArrayList<>();
    
    // Alert filter settings
    private boolean notifyOnlyHighPriority = false;
    private Set<String> mutedCategories = new HashSet<>();
    
    /**
     * Constructor
     */
    public AlertService() {
        loadAlertHistory();
        
        // Register default notification handlers
        registerNotificationHandler(new ConsoleNotificationHandler());
    }
    
    /**
     * Load alert history from file
     */
    private void loadAlertHistory() {
        File file = new File(ALERT_HISTORY_FILE);
        if (!file.exists()) {
            // Create directory if it doesn't exist
            File directory = file.getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }
            
            alertHistory = new ArrayList<>();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            alertHistory = reader.lines()
                    .map(Alert::fromCsv)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            alertHistory = new ArrayList<>();
        }
    }
    
    /**
     * Save alert history to file
     */
    private void saveAlertHistory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ALERT_HISTORY_FILE))) {
            for (Alert alert : alertHistory) {
                writer.write(alert.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Create a new alert
     * @param message Alert message
     * @param level Alert level
     * @param category Category (e.g., "Expenses", "System", etc.)
     * @return The created alert
     */
    public Alert createAlert(String message, AlertLevel level, String category) {
        Alert alert = new Alert(message, level, category);
        alertHistory.add(alert);
        saveAlertHistory();
        
        // Send notifications
        sendNotification(alert);
        
        return alert;
    }
    
    /**
     * Get all alerts
     */
    public List<Alert> getAllAlerts() {
        return new ArrayList<>(alertHistory);
    }
    
    /**
     * Get unacknowledged alerts
     */
    public List<Alert> getUnacknowledgedAlerts() {
        return alertHistory.stream()
                .filter(a -> !a.isAcknowledged())
                .collect(Collectors.toList());
    }
    
    /**
     * Get alerts for a specific day
     */
    public List<Alert> getAlertsByDate(LocalDate date) {
        return alertHistory.stream()
                .filter(a -> a.getTimestamp().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }
    
    /**
     * Get alerts by category
     */
    public List<Alert> getAlertsByCategory(String category) {
        return alertHistory.stream()
                .filter(a -> a.getCategory().equals(category))
                .collect(Collectors.toList());
    }
    
    /**
     * Get alerts by level
     */
    public List<Alert> getAlertsByLevel(AlertLevel level) {
        return alertHistory.stream()
                .filter(a -> a.getLevel() == level)
                .collect(Collectors.toList());
    }
    
    /**
     * Acknowledge an alert
     * @param alertId ID of the alert to acknowledge
     * @return true if acknowledged, false if not found
     */
    public boolean acknowledgeAlert(String alertId) {
        for (Alert alert : alertHistory) {
            if (alert.getId().equals(alertId)) {
                alert.setAcknowledged(true);
                saveAlertHistory();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Clear all alerts
     */
    public void clearAllAlerts() {
        alertHistory.clear();
        saveAlertHistory();
    }
    
    /**
     * Delete alerts older than specified days
     */
    public int deleteOldAlerts(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        int initialSize = alertHistory.size();
        
        alertHistory = alertHistory.stream()
                .filter(a -> a.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());
                
        int deletedCount = initialSize - alertHistory.size();
        if (deletedCount > 0) {
            saveAlertHistory();
        }
        
        return deletedCount;
    }
    
    /**
     * Register a notification handler
     */
    public void registerNotificationHandler(AlertNotificationHandler handler) {
        notificationHandlers.add(handler);
    }
    
    /**
     * Send notification to all handlers
     */
    private void sendNotification(Alert alert) {
        // Check if we should notify based on filter settings
        if (notifyOnlyHighPriority && alert.getLevel() != AlertLevel.CRITICAL) {
            return;
        }
        
        if (mutedCategories.contains(alert.getCategory())) {
            return;
        }
        
        for (AlertNotificationHandler handler : notificationHandlers) {
            handler.sendNotification(alert);
        }
    }
    
    /**
     * Set notification filter to only high priority alerts
     */
    public void setNotifyOnlyHighPriority(boolean notifyOnlyHighPriority) {
        this.notifyOnlyHighPriority = notifyOnlyHighPriority;
    }
    
    /**
     * Mute a category
     */
    public void muteCategory(String category) {
        mutedCategories.add(category);
    }
    
    /**
     * Unmute a category
     */
    public void unmuteCategory(String category) {
        mutedCategories.remove(category);
    }
    
    /**
     * Reset all notification settings
     */
    public void resetNotificationSettings() {
        notifyOnlyHighPriority = false;
        mutedCategories.clear();
    }
    
    /**
     * Alert Notification Handler interface
     */
    public interface AlertNotificationHandler {
        void sendNotification(Alert alert);
    }
    
    /**
     * Default console notification handler
     */
    private static class ConsoleNotificationHandler implements AlertNotificationHandler {
        @Override
        public void sendNotification(Alert alert) {
            System.out.println("[ALERT] " + alert.toString());
        }
    }
    
    /**
     * GUI notification handler - to be implemented by the view
     */
    public static class GuiNotificationHandler implements AlertNotificationHandler {
        private final Runnable refreshCallback;
        
        public GuiNotificationHandler(Runnable refreshCallback) {
            this.refreshCallback = refreshCallback;
        }
        
        @Override
        public void sendNotification(Alert alert) {
            // Trigger UI refresh
            if (refreshCallback != null) {
                refreshCallback.run();
            }
        }
    }
} 