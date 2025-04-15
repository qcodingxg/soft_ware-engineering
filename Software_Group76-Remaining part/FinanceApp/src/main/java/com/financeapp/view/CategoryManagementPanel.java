package com.financeapp.view;

import com.financeapp.controller.TransactionController;
import com.financeapp.model.Transaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

/**
 * Category Management Panel
 * Combines category summary and editing functionality
 */
public class CategoryManagementPanel extends JPanel {
    
    private final TransactionController controller;
    
    // Main components
    private JSplitPane mainSplitPane;
    private JTabbedPane tabbedPane;
    
    // Category summary components
    private JTable categoryTable;
    private DefaultTableModel categoryTableModel;
    private JTable transactionTable;
    private DefaultTableModel transactionTableModel;
    
    // Filter components
    private JComboBox<Integer> yearComboBox;
    private JComboBox<Integer> monthComboBox;
    private JComboBox<String> categoryComboBox;
    private JButton filterButton;
    
    // Category edit components
    private JTextField newCategoryField;
    private JButton changeCategoryButton;
    private JComboBox<String> bulkCategoryComboBox;
    private JButton bulkChangeCategoryButton;
    
    // Transaction edit components
    private JTextField dateField;
    private JTextField amountField;
    private JTextField categoryField;
    private JTextField descriptionField;
    private JButton updateButton;
    private JButton clearButton;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Colors (matching login panel)
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color TABLE_HEADER_COLOR = new Color(52, 73, 94);
    private static final Color TABLE_ALTERNATE_COLOR = new Color(245, 245, 245);
    
    /**
     * Custom rounded border with shadow
     */
    private class ShadowBorder extends AbstractBorder {
        private static final int SHADOW_SIZE = 4;
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw shadow
            for (int i = 0; i < SHADOW_SIZE; i++) {
                g2.setColor(new Color(0, 0, 0, 20 - i * 4));
                g2.drawRoundRect(x + i, y + i, width - i * 2 - 1, height - i * 2 - 1, 10, 10);
            }
            
            g2.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = SHADOW_SIZE;
            return insets;
        }
    }
    
    /**
     * Custom rounded border
     */
    private class RoundedLineBorder extends LineBorder {
        private int radius;
        
        public RoundedLineBorder(Color color, int thickness, int radius) {
            super(color, thickness, false);
            this.radius = radius;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(lineColor);
            g2d.setStroke(new BasicStroke(thickness));
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }
    }
    
    /**
     * Create glass-style button with rounded corners and hover effects
     */
    private JButton createGlassButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(color);
        button.setBackground(new Color(255, 255, 255, 220));
        
        // Set custom UI
        button.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();
                
                // Paint background
                if (model.isPressed()) {
                    g2.setColor(color.darker());
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
                    g2.setColor(Color.WHITE);
                } else if (model.isRollover()) {
                    g2.setColor(color);
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(new Color(255, 255, 255, 220));
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
                    g2.setColor(color);
                    g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 10, 10);
                }
                
                // Paint text and icon - always show text
                FontMetrics fm = g2.getFontMetrics();
                Rectangle textRect = new Rectangle(0, 0, c.getWidth(), c.getHeight());
                
                // Center text
                int x = (c.getWidth() - fm.stringWidth(b.getText())) / 2;
                int y = (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                
                if (model.isPressed() || model.isRollover()) {
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(color);
                }
                
                g2.drawString(b.getText(), x, y);
                
                if (b.getIcon() != null) {
                    b.getIcon().paintIcon(c, g2, 5, (c.getHeight() - b.getIcon().getIconHeight()) / 2);
                }
                
                g2.dispose();
            }
        });
        
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return button;
    }
    
    /**
     * Create styled text field with rounded corners
     */
    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT_COLOR);
        
        // Custom rounded border
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedLineBorder(new Color(200, 200, 200), 1, 5),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        
        // Focus effect
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedLineBorder(PRIMARY_COLOR, 1, 5),
                    BorderFactory.createEmptyBorder(7, 10, 7, 10)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedLineBorder(new Color(200, 200, 200), 1, 5),
                    BorderFactory.createEmptyBorder(7, 10, 7, 10)
                ));
            }
        });
        
        return field;
    }
    
    /**
     * Constructor
     * @param controller Transaction controller
     */
    public CategoryManagementPanel(TransactionController controller) {
        this.controller = controller;
        initUI();
    }
    
    /**
     * Initialize UI
     */
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(BACKGROUND_COLOR);
        
        // Create filter panel
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);
        
        // Create main split pane
        mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setDividerLocation(200);
        mainSplitPane.setResizeWeight(0.4);
        mainSplitPane.setBackground(BACKGROUND_COLOR);
        
        // Create category summary table panel
        JPanel categoryTablePanel = createCategoryTablePanel();
        mainSplitPane.setTopComponent(categoryTablePanel);
        
        // Create tabbed pane for bottom section
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabbedPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.setForeground(TEXT_COLOR);
        
        // Create transaction details panel
        JPanel transactionDetailsPanel = createTransactionDetailsPanel();
        tabbedPane.addTab("Transaction List", null, transactionDetailsPanel, "View transactions in selected category");
        
        // Create category edit panel
        JPanel categoryEditPanel = createCategoryEditPanel();
        tabbedPane.addTab("Edit Categories", null, categoryEditPanel, "Modify transaction categories");
        
        // Create transaction edit panel
        JPanel transactionEditPanel = createTransactionEditPanel();
        tabbedPane.addTab("Edit Transaction", null, transactionEditPanel, "Edit all transaction details");
        
        mainSplitPane.setBottomComponent(tabbedPane);
        
        add(mainSplitPane, BorderLayout.CENTER);
    }
    
    /**
     * Create filter panel
     */
    private JPanel createFilterPanel() {
        // Create panel with shadow border
        JPanel shadowPanel = new JPanel(new BorderLayout());
        shadowPanel.setBackground(BACKGROUND_COLOR);
        shadowPanel.setBorder(new ShadowBorder());
        
        // Main panel
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedLineBorder(PRIMARY_COLOR, 1, 10),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(PRIMARY_COLOR);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        titlePanel.setPreferredSize(new Dimension(0, 45));
        
        JLabel titleLabel = new JLabel("Filter Options");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setIcon(createFilterIcon());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Filter controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        controlsPanel.setBackground(Color.WHITE);
        
        // Year selection
        JLabel yearLabel = new JLabel("Year:");
        yearLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        yearLabel.setForeground(TEXT_COLOR);
        controlsPanel.add(yearLabel);
        
        yearComboBox = new JComboBox<>();
        yearComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        yearComboBox.setBackground(Color.WHITE);
        yearComboBox.setForeground(TEXT_COLOR);
        yearComboBox.setPreferredSize(new Dimension(80, 30));
        populateYearComboBox();
        controlsPanel.add(yearComboBox);
        
        // Month selection
        JLabel monthLabel = new JLabel("Month:");
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        monthLabel.setForeground(TEXT_COLOR);
        controlsPanel.add(monthLabel);
        
        monthComboBox = new JComboBox<>();
        monthComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        monthComboBox.setBackground(Color.WHITE);
        monthComboBox.setForeground(TEXT_COLOR);
        monthComboBox.setPreferredSize(new Dimension(80, 30));
        for (int i = 1; i <= 12; i++) {
            monthComboBox.addItem(i);
        }
        monthComboBox.setSelectedItem(LocalDate.now().getMonthValue());
        controlsPanel.add(monthComboBox);
        
        // Category selection
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        categoryLabel.setForeground(TEXT_COLOR);
        controlsPanel.add(categoryLabel);
        
        categoryComboBox = new JComboBox<>();
        categoryComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryComboBox.setBackground(Color.WHITE);
        categoryComboBox.setForeground(TEXT_COLOR);
        categoryComboBox.setPreferredSize(new Dimension(150, 30));
        categoryComboBox.addItem("All Categories");
        updateCategoryComboBox();
        controlsPanel.add(categoryComboBox);
        
        // Filter button with glass style
        filterButton = createGlassButton("Apply Filter", PRIMARY_COLOR);
        filterButton.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        filterButton.addActionListener(e -> updateData());
        controlsPanel.add(filterButton);
        
        // Add components to panel
        panel.setLayout(new BorderLayout());
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(controlsPanel, BorderLayout.CENTER);
        
        // Add to shadow panel
        shadowPanel.add(panel, BorderLayout.CENTER);
        
        return shadowPanel;
    }
    
    /**
     * Create filter icon
     */
    private Icon createFilterIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw a funnel/filter shape
                g2d.setColor(Color.WHITE);
                int[] xPoints = {x + 5, x + 19, x + 15, x + 15, x + 9, x + 9, x + 5};
                int[] yPoints = {y + 5, y + 5, y + 12, y + 18, y + 18, y + 12, y + 5};
                g2d.fillPolygon(xPoints, yPoints, 7);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 24;
            }
            
            @Override
            public int getIconHeight() {
                return 24;
            }
        };
    }
    
    /**
     * Create category summary table panel
     */
    private JPanel createCategoryTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                "Category Summary", 
                TitledBorder.LEFT, 
                TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 12),
                PRIMARY_COLOR));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Create table model
        String[] columns = {"Category", "Count", "Total Amount", "Percentage", "Average"};
        categoryTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table not editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1 || columnIndex == 2 || columnIndex == 4) {
                    return Double.class;
                }
                return String.class;
            }
        };
        
        // Create table
        categoryTable = new JTable(categoryTableModel);
        categoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryTable.setBackground(Color.WHITE);
        categoryTable.setForeground(TEXT_COLOR);
        categoryTable.setSelectionBackground(SECONDARY_COLOR);
        categoryTable.setSelectionForeground(Color.WHITE);
        categoryTable.setGridColor(new Color(220, 220, 220));
        categoryTable.setRowHeight(25);
        
        // Style table header - 修改列名颜色
        JTableHeader header = categoryTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, SECONDARY_COLOR));
        
        // Set custom renderer for header to ensure visibility
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setBackground(PRIMARY_COLOR);
                label.setForeground(Color.WHITE);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, SECONDARY_COLOR));
                return label;
            }
        });
        
        // Set alternating row colors
        categoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ALTERNATE_COLOR);
                }
                return c;
            }
        });
        
        // Set table renderers
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        categoryTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        categoryTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        categoryTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        categoryTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        
        // Add selection listener
        categoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateTransactionTable();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create transaction details panel
     */
    private JPanel createTransactionDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Create table model
        String[] columns = {"Date", "Amount", "Category", "Description"};
        transactionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table not editable
            }
        };
        
        // Create table
        transactionTable = new JTable(transactionTableModel);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        transactionTable.setBackground(Color.WHITE);
        transactionTable.setForeground(TEXT_COLOR);
        transactionTable.setSelectionBackground(SECONDARY_COLOR);
        transactionTable.setSelectionForeground(Color.WHITE);
        transactionTable.setGridColor(new Color(220, 220, 220));
        transactionTable.setRowHeight(25);
        transactionTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Style table header - 修改列名颜色
        JTableHeader transHeader = transactionTable.getTableHeader();
        transHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        transHeader.setBackground(PRIMARY_COLOR);
        transHeader.setForeground(Color.WHITE);
        transHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, SECONDARY_COLOR));
        
        // Set custom renderer for header to ensure visibility
        transHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setBackground(PRIMARY_COLOR);
                label.setForeground(Color.WHITE);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, SECONDARY_COLOR));
                return label;
            }
        });
        
        // Set alternating row colors
        transactionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ALTERNATE_COLOR);
                }
                return c;
            }
        });
        
        // Add selection listener to populate edit fields when a transaction is selected
        transactionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && transactionTable.getSelectedRow() >= 0) {
                loadSelectedTransaction();
                // Switch to edit tab when a transaction is selected
                if (tabbedPane.getSelectedIndex() != 2) {
                    tabbedPane.setSelectedIndex(2);
                }
            }
        });
        
        // Set table renderers
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        transactionTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create category edit panel
     */
    private JPanel createCategoryEditPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Create individual edit panel
        JPanel singleEditPanel = new JPanel();
        singleEditPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1),
                "Edit Selected Transactions", 
                TitledBorder.LEFT, 
                TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 12),
                PRIMARY_COLOR));
        singleEditPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        singleEditPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel newCategoryLabel = new JLabel("New Category:");
        newCategoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        newCategoryLabel.setForeground(TEXT_COLOR);
        singleEditPanel.add(newCategoryLabel);
        
        newCategoryField = createStyledTextField(15);
        singleEditPanel.add(newCategoryField);
        
        changeCategoryButton = createGlassButton("Update Selected", PRIMARY_COLOR);
        changeCategoryButton.addActionListener(e -> changeSelectedTransactionCategory());
        singleEditPanel.add(changeCategoryButton);
        
        // Create batch edit panel
        JPanel bulkEditPanel = new JPanel();
        bulkEditPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1),
                "Batch Category Update", 
                TitledBorder.LEFT, 
                TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 12),
                PRIMARY_COLOR));
        bulkEditPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bulkEditPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel changeToLabel = new JLabel("Change to:");
        changeToLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        changeToLabel.setForeground(TEXT_COLOR);
        bulkEditPanel.add(changeToLabel);
        
        bulkCategoryComboBox = new JComboBox<>();
        bulkCategoryComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bulkCategoryComboBox.setBackground(Color.WHITE);
        bulkCategoryComboBox.setForeground(TEXT_COLOR);
        updateBulkCategoryComboBox();
        bulkCategoryComboBox.setPreferredSize(new Dimension(150, bulkCategoryComboBox.getPreferredSize().height));
        bulkEditPanel.add(bulkCategoryComboBox);
        
        bulkChangeCategoryButton = createGlassButton("Update Category", PRIMARY_COLOR);
        bulkChangeCategoryButton.addActionListener(e -> bulkChangeCategory());
        bulkEditPanel.add(bulkChangeCategoryButton);
        
        // Combine panels
        JPanel combinedPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        combinedPanel.setBackground(BACKGROUND_COLOR);
        combinedPanel.add(singleEditPanel);
        combinedPanel.add(bulkEditPanel);
        
        JPanel instructions = new JPanel(new BorderLayout());
        instructions.setBackground(BACKGROUND_COLOR);
        JTextArea instructionText = new JTextArea(
                "Instructions:\n" +
                "1. To edit categories of specific transactions, select them in the Transaction List tab, " +
                "enter a new category name, and click 'Update Selected'.\n" +
                "2. To batch update all transactions in a category, select the category in the summary table above, " +
                "choose a target category, and click 'Update Category'.");
        instructionText.setEditable(false);
        instructionText.setLineWrap(true);
        instructionText.setWrapStyleWord(true);
        instructionText.setBackground(BACKGROUND_COLOR);
        instructionText.setForeground(TEXT_COLOR);
        instructionText.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        instructionText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        instructions.add(instructionText, BorderLayout.CENTER);
        
        panel.add(combinedPanel, BorderLayout.NORTH);
        panel.add(instructions, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create transaction edit panel
     */
    private JPanel createTransactionEditPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1)));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Date field
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel dateLabel = new JLabel("Date (yyyy-MM-dd):");
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(TEXT_COLOR);
        formPanel.add(dateLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        dateField = createStyledTextField(20);
        formPanel.add(dateField, gbc);
        
        // Amount field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        amountLabel.setForeground(TEXT_COLOR);
        formPanel.add(amountLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        amountField = createStyledTextField(20);
        formPanel.add(amountField, gbc);
        
        // Category field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryLabel.setForeground(TEXT_COLOR);
        formPanel.add(categoryLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        categoryField = createStyledTextField(20);
        formPanel.add(categoryField, gbc);
        
        // Description field
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descriptionLabel.setForeground(TEXT_COLOR);
        formPanel.add(descriptionLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        descriptionField = createStyledTextField(20);
        formPanel.add(descriptionField, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        updateButton = createGlassButton("Update Transaction", PRIMARY_COLOR);
        updateButton.addActionListener(e -> updateTransaction());
        buttonPanel.add(updateButton);
        
        clearButton = createGlassButton("Clear Fields", SECONDARY_COLOR);
        clearButton.addActionListener(e -> clearFields());
        buttonPanel.add(clearButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Populate year combo box
     */
    private void populateYearComboBox() {
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 5; i <= currentYear + 1; i++) {
            yearComboBox.addItem(i);
        }
        yearComboBox.setSelectedItem(currentYear);
    }
    
    /**
     * Update category combo box
     */
    private void updateCategoryComboBox() {
        Set<String> categories = new HashSet<>();
        categoryComboBox.removeAllItems();
        categoryComboBox.addItem("All Categories");
        
        for (Transaction transaction : controller.getTransactions()) {
            categories.add(transaction.getCategory());
        }
        
        List<String> sortedCategories = new ArrayList<>(categories);
        Collections.sort(sortedCategories);
        
        for (String category : sortedCategories) {
            categoryComboBox.addItem(category);
        }
    }
    
    /**
     * Update bulk category combo box
     */
    private void updateBulkCategoryComboBox() {
        Set<String> categories = new HashSet<>();
        bulkCategoryComboBox.removeAllItems();
        
        for (Transaction transaction : controller.getTransactions()) {
            categories.add(transaction.getCategory());
        }
        
        List<String> sortedCategories = new ArrayList<>(categories);
        Collections.sort(sortedCategories);
        
        for (String category : sortedCategories) {
            bulkCategoryComboBox.addItem(category);
        }
        
        // Add new category option
        bulkCategoryComboBox.addItem("New Category...");
    }
    
    /**
     * Update data
     */
    public void updateData() {
        int selectedYear = (int) yearComboBox.getSelectedItem();
        int selectedMonth = (int) monthComboBox.getSelectedItem();
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        
        // Clear category table
        categoryTableModel.setRowCount(0);
        
        // Get transactions for selected period
        List<Transaction> filteredTransactions = filterTransactions(selectedYear, selectedMonth);
        
        // Group by category
        Map<String, List<Transaction>> categoryTransactions = new HashMap<>();
        double totalAmount = 0;
        
        for (Transaction transaction : filteredTransactions) {
            String category = transaction.getCategory();
            if (!categoryTransactions.containsKey(category)) {
                categoryTransactions.put(category, new ArrayList<>());
            }
            categoryTransactions.get(category).add(transaction);
            totalAmount += Math.abs(transaction.getAmount());
        }
        
        // Fill category table
        for (Map.Entry<String, List<Transaction>> entry : categoryTransactions.entrySet()) {
            String category = entry.getKey();
            List<Transaction> transactions = entry.getValue();
            
            // Calculate total for this category
            double categoryTotal = 0;
            for (Transaction t : transactions) {
                categoryTotal += Math.abs(t.getAmount());
            }
            
            // Calculate percentage and average
            double percentage = totalAmount > 0 ? (categoryTotal / totalAmount * 100) : 0;
            double average = categoryTotal / transactions.size();
            
            // Add to table
            Object[] row = {
                    category,
                    transactions.size(),
                    String.format("%.2f", categoryTotal),
                    String.format("%.2f%%", percentage),
                    String.format("%.2f", average)
            };
            categoryTableModel.addRow(row);
        }
        
        // Update transaction table
        updateTransactionTable();
    }
    
    /**
     * Update transaction table
     */
    private void updateTransactionTable() {
        // Clear transaction table
        transactionTableModel.setRowCount(0);
        
        int selectedRow = categoryTable.getSelectedRow();
        String selectedCategory = "All Categories";
        
        if (selectedRow >= 0) {
            selectedCategory = (String) categoryTableModel.getValueAt(selectedRow, 0);
        }
        
        int selectedYear = (int) yearComboBox.getSelectedItem();
        int selectedMonth = (int) monthComboBox.getSelectedItem();
        
        // Get transactions for selected period
        List<Transaction> filteredTransactions = filterTransactions(selectedYear, selectedMonth);
        
        // Fill transaction table
        for (Transaction transaction : filteredTransactions) {
            // If viewing a specific category
            if (!"All Categories".equals(selectedCategory) && !transaction.getCategory().equals(selectedCategory)) {
                continue;
            }
            
            Object[] row = {
                    transaction.getDate().format(DATE_FORMATTER),
                    String.format("%.2f", transaction.getAmount()),
                    transaction.getCategory(),
                    transaction.getDescription()
            };
            transactionTableModel.addRow(row);
        }
    }
    
    /**
     * Filter transactions for specific year and month
     */
    private List<Transaction> filterTransactions(int year, int month) {
        List<Transaction> result = new ArrayList<>();
        
        // Get selected category
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        
        // Calculate start and end date of month
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
        
        for (Transaction transaction : controller.getTransactions()) {
            LocalDate transactionDate = transaction.getDate();
            
            // Check date range
            if (transactionDate.isEqual(startDate) || 
                    (transactionDate.isAfter(startDate) && transactionDate.isBefore(endDate)) || 
                    transactionDate.isEqual(endDate)) {
                
                // Check category
                if ("All Categories".equals(selectedCategory) || selectedCategory.equals(transaction.getCategory())) {
                    result.add(transaction);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Change category of selected transactions
     */
    private void changeSelectedTransactionCategory() {
        int[] selectedRows = transactionTable.getSelectedRows();
        
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, 
                    "Please select transactions to update", 
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String newCategory = newCategoryField.getText().trim();
        if (newCategory.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a new category name", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            for (int selectedRow : selectedRows) {
                // Get selected transaction info
                String dateStr = (String) transactionTableModel.getValueAt(selectedRow, 0);
                String amountStr = (String) transactionTableModel.getValueAt(selectedRow, 1);
                String category = (String) transactionTableModel.getValueAt(selectedRow, 2);
                String description = (String) transactionTableModel.getValueAt(selectedRow, 3);
                
                double amount = Double.parseDouble(amountStr.replace(",", ""));
                
                // Find corresponding transaction object
                Transaction transaction = findTransactionByDetails(dateStr, amount, category, description);
                
                if (transaction != null) {
                    // Update category
                    controller.updateCategory(transaction, newCategory);
                }
            }
            
            // Update UI
            updateCategorySummary();
            
            JOptionPane.showMessageDialog(this, 
                    "Successfully updated " + selectedRows.length + " transaction(s)", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            // Clear input field
            newCategoryField.setText("");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error updating category: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Bulk change category
     */
    private void bulkChangeCategory() {
        int categoryTableSelectedRow = categoryTable.getSelectedRow();
        
        if (categoryTableSelectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a category in the summary table", 
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String oldCategory = (String) categoryTableModel.getValueAt(categoryTableSelectedRow, 0);
        String newCategory = (String) bulkCategoryComboBox.getSelectedItem();
        
        // If "New Category..." option selected
        if ("New Category...".equals(newCategory)) {
            newCategory = JOptionPane.showInputDialog(this, 
                    "Enter new category name:", 
                    "New Category", 
                    JOptionPane.QUESTION_MESSAGE);
            
            if (newCategory == null || newCategory.trim().isEmpty()) {
                return; // User canceled or empty input
            }
            newCategory = newCategory.trim();
        }
        
        if (oldCategory.equals(newCategory)) {
            JOptionPane.showMessageDialog(this, 
                    "New category is the same as original category", 
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            int count = 0;
            
            // Filter currently displayed transactions
            int selectedYear = (int) yearComboBox.getSelectedItem();
            int selectedMonth = (int) monthComboBox.getSelectedItem();
            List<Transaction> filteredTransactions = filterTransactions(selectedYear, selectedMonth);
            
            // Update all transactions in selected category
            for (Transaction transaction : filteredTransactions) {
                if (transaction.getCategory().equals(oldCategory)) {
                    controller.updateCategory(transaction, newCategory);
                    count++;
                }
            }
            
            // Update UI
            updateCategorySummary();
            
            JOptionPane.showMessageDialog(this, 
                    "Successfully updated " + count + " transaction(s) from \"" + oldCategory + "\" to \"" + newCategory + "\"", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                    "Error updating categories: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Load selected transaction into edit fields
     */
    private void loadSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow >= 0) {
            dateField.setText((String) transactionTableModel.getValueAt(selectedRow, 0));
            amountField.setText(((String) transactionTableModel.getValueAt(selectedRow, 1)).replace(",", ""));
            categoryField.setText((String) transactionTableModel.getValueAt(selectedRow, 2));
            descriptionField.setText((String) transactionTableModel.getValueAt(selectedRow, 3));
        }
    }
    
    /**
     * Clear all edit fields
     */
    private void clearFields() {
        dateField.setText("");
        amountField.setText("");
        categoryField.setText("");
        descriptionField.setText("");
        transactionTable.clearSelection();
    }
    
    /**
     * Update transaction
     */
    private void updateTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a transaction first", 
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Validate input
        if (!validateInput()) {
            return;
        }
        
        try {
            // Get the original transaction
            String originalDateStr = (String) transactionTableModel.getValueAt(selectedRow, 0);
            double originalAmount = Double.parseDouble(((String) transactionTableModel.getValueAt(selectedRow, 1)).replace(",", ""));
            String originalCategory = (String) transactionTableModel.getValueAt(selectedRow, 2);
            String originalDescription = (String) transactionTableModel.getValueAt(selectedRow, 3);
            
            // Find the transaction in the controller
            Transaction transaction = findTransactionByDetails(originalDateStr, originalAmount, 
                    originalCategory, originalDescription);
            
            if (transaction != null) {
                // Update the transaction
                transaction.setDate(LocalDate.parse(dateField.getText(), DATE_FORMATTER));
                transaction.setAmount(Double.parseDouble(amountField.getText()));
                transaction.setCategory(categoryField.getText());
                transaction.setDescription(descriptionField.getText());
                
                // Save changes
                controller.saveTransactions();
                
                // Update the UI
                updateCategorySummary();
                clearFields();
                
                JOptionPane.showMessageDialog(this, 
                        "Transaction updated successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                    "Error updating transaction: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Validate input fields
     */
    private boolean validateInput() {
        // Validate date
        try {
            LocalDate.parse(dateField.getText(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, 
                    "Invalid date format. Please use yyyy-MM-dd", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            dateField.requestFocus();
            return false;
        }
        
        // Validate amount
        try {
            Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                    "Invalid amount format. Please enter a valid number", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            amountField.requestFocus();
            return false;
        }
        
        // Validate category
        if (categoryField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Category cannot be empty", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            categoryField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Find transaction by details
     */
    private Transaction findTransactionByDetails(String dateStr, double amount, String category, String description) {
        LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
        
        for (Transaction transaction : controller.getTransactions()) {
            if (transaction.getDate().equals(date) && 
                    Math.abs(transaction.getAmount() - amount) < 0.001 && 
                    transaction.getCategory().equals(category) && 
                    transaction.getDescription().equals(description)) {
                return transaction;
            }
        }
        
        return null;
    }
    
    /**
     * Update category list
     */
    public void updateCategoryList() {
        updateCategorySummary();
    }
    
    /**
     * Update category summary
     */
    public void updateCategorySummary() {
        updateCategoryComboBox();
        updateBulkCategoryComboBox();
        updateData();
    }
} 