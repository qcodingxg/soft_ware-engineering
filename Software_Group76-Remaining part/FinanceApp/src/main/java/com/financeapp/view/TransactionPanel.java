package com.financeapp.view;

import com.financeapp.controller.TransactionController;
import com.financeapp.model.Transaction;
import com.financeapp.model.AIClassifier;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction Entry Panel
 * Contains transaction form and transaction list
 */
public class TransactionPanel extends JPanel {
    private final TransactionController controller;
    private final AIClassifier classifier;

    // Form components
    private JTextField dateField, amountField, categoryField, descriptionField, searchField;
    private JButton addButton, importButton,suggestButton,deleteButton;

    // Table components
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color ACCENT_COLOR = new Color(26, 188, 156);
    private static final Color TABLE_HEADER_COLOR = new Color(52, 73, 94);
    private static final Color TABLE_ALTERNATE_COLOR = new Color(245, 248, 250);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 20);
    
    /**
     * Constructor
     * @param controller Transaction controller
     */
    public TransactionPanel(TransactionController controller) {
        this.controller = controller;
        this.classifier = new AIClassifier();
        initUI();
    }
    
    /**
     * Initialize UI
     */
    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create form panel
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.NORTH);
        
        // Create table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
    }
    
    /**
     * Create form panel
     */
    private JPanel createFormPanel() {
        // Create panel with shadow border
        JPanel shadowPanel = new JPanel(new BorderLayout());
        shadowPanel.setBackground(BACKGROUND_COLOR);
        shadowPanel.setBorder(new ShadowBorder());
        
        // Main panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedLineBorder(PRIMARY_COLOR, 1, 10),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(PRIMARY_COLOR);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        titlePanel.setPreferredSize(new Dimension(0, 50));
        
        JLabel titleLabel = new JLabel("Add New Transaction");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setIcon(createTransactionIcon());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Create the form
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Form container
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        
        // Date
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel dateLabel = new JLabel("Date (yyyy-MM-dd):");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dateLabel.setForeground(TEXT_COLOR);
        formContainer.add(dateLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        dateField = createStyledTextField(10);
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dateField.setToolTipText("Enter date in yyyy-MM-dd format");
        formContainer.add(dateField, gbc);
        
        // Amount
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        amountLabel.setForeground(TEXT_COLOR);
        formContainer.add(amountLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        amountField = createStyledTextField(10);
        amountField.setToolTipText("Enter amount (positive for income, negative for expense)");
        amountField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!((c >= '0' && c <= '9') || c == '.' || c == '-' || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE)) {
                    e.consume();
                }
            }
        });
        formContainer.add(amountField, gbc);
        
        // Category
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        categoryLabel.setForeground(TEXT_COLOR);
        formContainer.add(categoryLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        categoryField = createStyledTextField(10);
        categoryField.setToolTipText("Enter transaction category (e.g., Food, Utilities, etc.)");
        formContainer.add(categoryField, gbc);
        
        // Description
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        descriptionLabel.setForeground(TEXT_COLOR);
        formContainer.add(descriptionLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        descriptionField = createStyledTextField(20);
        descriptionField.setToolTipText("Enter optional description");
        formContainer.add(descriptionField, gbc);
        
        // Button panel
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBackground(Color.WHITE);
        
        addButton = createGlassButton("Add Transaction", SUCCESS_COLOR);
        addButton.setIcon(UIManager.getIcon("FileView.fileIcon"));
        addButton.addActionListener(e -> addTransaction());
        buttonPanel.add(addButton);

        suggestButton = createGlassButton("AI Suggest", ACCENT_COLOR);
        suggestButton.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        suggestButton.addActionListener(e -> suggestCategory());
        buttonPanel.add(suggestButton);

        importButton = createGlassButton("Import CSV", SECONDARY_COLOR);
        importButton.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        importButton.addActionListener(e -> importCSV());
        buttonPanel.add(importButton);
        
        formContainer.add(buttonPanel, gbc);
        
        // Add components to main panel
        panel.setLayout(new BorderLayout());
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(formContainer, BorderLayout.CENTER);
        
        // Add main panel to shadow panel
        shadowPanel.add(panel, BorderLayout.CENTER);
        
        return shadowPanel;
    }
    
    /**
     * Create table panel
     */
    private JPanel createTablePanel() {
        // Create panel with shadow border
        JPanel shadowPanel = new JPanel(new BorderLayout());
        shadowPanel.setBackground(BACKGROUND_COLOR);
        shadowPanel.setBorder(new ShadowBorder());
        
        // Main panel
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedLineBorder(SECONDARY_COLOR, 1, 10),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(SECONDARY_COLOR);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        titlePanel.setPreferredSize(new Dimension(0, 50));
        
        JLabel titleLabel = new JLabel("Transaction List");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setIcon(UIManager.getIcon("Table.ascendingSortIcon"));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Create search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(5, 5, 10, 5)
        ));
        
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchLabel.setForeground(TEXT_COLOR);
        searchPanel.add(searchLabel);
        
        searchField = createStyledTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search by any field...");
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterTable(searchField.getText());
            }
        });
        searchPanel.add(searchField);
        
        JButton clearButton = createStyledButton("Clear", SECONDARY_COLOR);
        clearButton.addActionListener(e -> {
            searchField.setText("");
            filterTable("");
        });
        searchPanel.add(clearButton);
        
        // Create table model
        String[] columns = {"Date", "Amount", "Category", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 1) return Double.class;
                return String.class;
            }
        };
        
        // Create table
        transactionTable = new JTable(tableModel);
        transactionTable.setBackground(Color.WHITE);
        transactionTable.setForeground(TEXT_COLOR);
        transactionTable.setSelectionBackground(SECONDARY_COLOR);
        transactionTable.setSelectionForeground(Color.WHITE);
        transactionTable.setGridColor(new Color(240, 240, 240));
        transactionTable.setShowGrid(true);
        transactionTable.setRowHeight(30);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        transactionTable.setIntercellSpacing(new Dimension(5, 5));
        
        // Style table header
        JTableHeader header = transactionTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 35));
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
        
        // Set column widths
        TableColumnModel columnModel = transactionTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100); // Date
        columnModel.getColumn(1).setPreferredWidth(100); // Amount
        columnModel.getColumn(2).setPreferredWidth(120); // Category
        columnModel.getColumn(3).setPreferredWidth(200); // Description
        
        // Add sorter
        sorter = new TableRowSorter<>(tableModel);
        transactionTable.setRowSorter(sorter);
        
        // Set alternating row colors
        transactionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ALTERNATE_COLOR);
                }
                
                // Special formatting for amount column
                if (column == 1 && value != null) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    
                    double amount = value instanceof Double ? (Double) value : Double.parseDouble(value.toString());
                    if (amount < 0) {
                        setForeground(ERROR_COLOR);
                    } else if (amount > 0) {
                        setForeground(SUCCESS_COLOR);
                    } else {
                        setForeground(TEXT_COLOR);
                    }
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setForeground(isSelected ? Color.WHITE : TEXT_COLOR);
                }
                
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                
                return c;
            }
        });
        
        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Add delete button with animation
        JButton deleteButton = createGlassButton("Delete Selected", ERROR_COLOR);
        deleteButton.setIcon(UIManager.getIcon("FileChooser.detailsViewIcon"));
        deleteButton.addActionListener(e -> deleteSelectedTransaction());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.add(deleteButton);
        
        // Assemble all components
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.setLayout(new BorderLayout());
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        // Add main panel to shadow panel
        shadowPanel.add(panel, BorderLayout.CENTER);
        
        return shadowPanel;
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
     * Create styled button
     */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return button;
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
                
                // Paint text and icon
                super.paint(g2, c);
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
     * Create transaction icon for header
     */
    private Icon createTransactionIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw a dollar sign
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2d.drawString("$", x + 8, y + 18);
                
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
     * Filter table based on search text
     */
    private void filterTable(String searchText) {
        if (searchText.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }

    private LocalDate validateDate() throws DateTimeParseException, IllegalArgumentException {
        String dateText = dateField.getText().trim();
        if (dateText.isEmpty()) {
            throw new IllegalArgumentException("Please enter a date");
        }
        return LocalDate.parse(dateText, DateTimeFormatter.ISO_DATE);
    }

    private double validateAmount() throws NumberFormatException, IllegalArgumentException {
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            throw new IllegalArgumentException("Please enter an amount");
        }
        return Double.parseDouble(amountText);
    }

    private void resetForm() {
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        amountField.setText("");
        descriptionField.setText("");
        categoryField.setText("");
        dateField.requestFocus();
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    /**
     * Add transaction
     */
    private void addTransaction() {
        try {
            LocalDate date = validateDate();
            double amount = validateAmount();
            String description = descriptionField.getText().trim();

            if (description.isEmpty()) {
                showMessage("Description cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String category = categoryField.getText().trim();
            if (category.isEmpty()) {
                // 改为模态对话框确保用户必须处理
                int option = JOptionPane.showConfirmDialog(
                        this,
                        "Category is empty. Do you want AI suggestion?",
                        "Category Required",
                        JOptionPane.YES_NO_OPTION
                );

                if (option == JOptionPane.YES_OPTION) {
                    suggestCategory();
                }
                return; // 仍然需要返回，等待用户操作
            }

            // 确保所有必要字段已填写
            Transaction transaction = new Transaction(date, category, amount, description);
            controller.addTransaction(transaction);

            resetForm();
            updateTransactionList();
            showMessage("Transaction added", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            handleAddError(e);
        }
    }

    private void handleAddError(Exception e) {
        String message = "";
        if (e instanceof DateTimeParseException) {
            message = "Invalid date format (yyyy-MM-dd)";
            dateField.requestFocus();
        } else if (e instanceof NumberFormatException) {
            message = "Invalid amount";
            amountField.requestFocus();
        } else {
            message = "Error: " + e.getMessage();
        }
        showMessage(message, "Error", JOptionPane.ERROR_MESSAGE);
    }


    private void suggestCategory() {
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            showMessage("Please enter description first", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        setLoadingState(true);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                Transaction tempTrans = new Transaction(
                        LocalDate.now(),
                        "Uncategorized",
                        0.0,
                        description
                );
                return classifier.classify(tempTrans).getCategory();
            }

            @Override
            protected void done() {
                try {
                    String category = get();
                    showCategoryConfirmation(category, description);
                } catch (Exception e) {
                    handleClassificationError(e);
                } finally {
                    setLoadingState(false);
                }
            }
        }.execute();
    }

    private void setLoadingState(boolean isLoading) {
        suggestButton.setEnabled(!isLoading);
        suggestButton.setText(isLoading ? "Analyzing..." : "AI Suggest");
        categoryField.setForeground(isLoading ? Color.GRAY : TEXT_COLOR);
        categoryField.setText(isLoading ? "Contacting AI..." : "");
    }

    private void showCategoryConfirmation(String suggestedCategory, String description) {
        String[] options = {"Accept and Add", "Modify", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "AI suggests: " + suggestedCategory,
                "Confirm Category",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) { // Accept and Add
            categoryField.setText(suggestedCategory);
            addTransaction(); // 直接触发添加
        }
        else if (choice == 1) { // Modify
            String userInput = JOptionPane.showInputDialog(
                    this,
                    "Edit category:",
                    suggestedCategory
            );
            if (userInput != null && !userInput.trim().isEmpty()) {
                categoryField.setText(userInput);
                // 记录用户修正
                Transaction tempTrans = new Transaction(
                        LocalDate.now(),
                        suggestedCategory,
                        0.0,
                        description
                );
                classifier.recordCorrection(tempTrans, userInput);
                // 新增：自动触发添加交易
                addTransaction();
            }
        }
    }

    private void handleClassificationError(Exception e) {
        String errorMsg = e.getCause() instanceof IOException ?
                "Network error. Please check your connection." :
                "Classification failed. Please try manually.";

        showMessage(errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
        categoryField.setText("");
    }

    /**
     * Import CSV file
     */
    private void importCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select CSV file");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV file", "csv"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            try {
                List<Transaction> importedTransactions = readCSVFile(selectedFile);
                
                if (importedTransactions.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                            "No valid transactions found in file", 
                            "Import Warning", 
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                controller.addTransactions(importedTransactions);
                updateTransactionList();
                
                JOptionPane.showMessageDialog(this, 
                        "Successfully imported " + importedTransactions.size() + " transaction records", 
                        "Import Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                        "Import CSV file failed: " + e.getMessage(), 
                        "Import Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Read CSV file
     * @param file CSV file
     * @return Transaction list
     * @throws IOException If IO error occurs during reading
     */
    private List<Transaction> readCSVFile(File file) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int lineNumber = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip title line
            String line = reader.readLine();
            lineNumber++;
            
            // Read data line
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                
                try {
                    Transaction transaction = Transaction.fromCSV(line);
                    transactions.add(transaction);
                } catch (IllegalArgumentException e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                    // Continue processing next line
                }
            }
        }
        
        // Show errors if any
        if (!errors.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder("Some lines could not be imported:\n");
            for (int i = 0; i < Math.min(errors.size(), 5); i++) {
                errorMsg.append(errors.get(i)).append("\n");
            }
            if (errors.size() > 5) {
                errorMsg.append("... and ").append(errors.size() - 5).append(" more errors.");
            }
            
            JOptionPane.showMessageDialog(this,
                    errorMsg.toString(),
                    "Import Warnings",
                    JOptionPane.WARNING_MESSAGE);
        }
        
        return transactions;
    }
    
    /**
     * Delete selected transaction
     */
    private void deleteSelectedTransaction() {
        int selectedViewRow = transactionTable.getSelectedRow();
        if (selectedViewRow >= 0) {
            try {
                // Convert view row index to model row index
                int selectedModelRow = transactionTable.convertRowIndexToModel(selectedViewRow);
                Transaction transaction = controller.getTransactions().get(selectedModelRow);
                
                int confirm = JOptionPane.showConfirmDialog(this, 
                        "Are you sure you want to delete the selected transaction record?", 
                        "Confirm Delete", 
                        JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    controller.deleteTransaction(transaction);
                    updateTransactionList();
                }
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                        "Transaction delete failed: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            } catch (IndexOutOfBoundsException e) {
                JOptionPane.showMessageDialog(this,
                        "Error finding selected transaction. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Please select the transaction record to delete", 
                    "Information", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Update transaction list
     */
    public void updateTransactionList() {
        // Clear search field
        searchField.setText("");
        
        // Clear table
        tableModel.setRowCount(0);
        
        // Add all transactions
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Transaction transaction : controller.getTransactions()) {
            // Format amount with appropriate sign
            String formattedAmount = String.format("%.2f", transaction.getAmount());
            
            Object[] row = {
                    transaction.getDate().format(formatter),
                    transaction.getAmount(), // Store as Double for proper sorting
                    transaction.getCategory(),
                    transaction.getDescription()
            };
            tableModel.addRow(row);
        }
        
        // Reset filters
        sorter.setRowFilter(null);
    }
    
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
} 