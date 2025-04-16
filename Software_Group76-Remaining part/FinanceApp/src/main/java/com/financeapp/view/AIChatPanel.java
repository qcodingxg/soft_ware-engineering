package com.financeapp.view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.text.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.json.JSONObject;
import org.json.JSONArray;

import com.financeapp.controller.TransactionController;
import com.financeapp.model.Transaction;

public class AIChatPanel extends JPanel {
    // UI组件
    private JTextPane chatArea; // 使用JTextPane替代JTextArea以支持更丰富的内容
    private JTextField inputField;
    private JButton sendButton;
    private JScrollPane scrollPane;
    private JPanel headerPanel;
    private JPanel inputPanel;
    private JPanel suggestionPanel;
    private JLabel statusLabel;
    private JButton clearButton;
    private JButton helpButton;
    private JLabel titleLabel;
    private JLabel avatarLabel;
    private JProgressBar typingIndicator;
    private JButton[] quickSuggestionButtons;
    
    // 线程池用于处理异步任务
    private final ExecutorService executorService;
    
    // 样式相关
    private Font chatFont;
    private Font boldFont;
    private Color primaryColor = new Color(0, 120, 212); // 蓝色基调
    private Color secondaryColor = new Color(241, 241, 241); // 浅灰色背景
    private Color accentColor = new Color(0, 183, 74); // 绿色点缀
    private Color userBubbleColor = new Color(232, 242, 252); // 用户消息气泡颜色
    private Color aiBubbleColor = new Color(240, 250, 240); // AI消息气泡颜色
    private final ImageIcon sendIcon;
    private final ImageIcon aiAvatar;
    
    // API相关
    private static final String API_KEY = "sk-92f9dba0310242988bafce610d4664be"; // 请替换为您的API密钥
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String SYSTEM_PROMPT = "你是一位专业的个人财务顾问，名为'财智助手'。你的专长包括：" +
            "1. 个人预算规划与支出追踪\n" +
            "2. 债务管理与清偿策略\n" +
            "3. 储蓄目标设定与达成方法\n" +
            "4. 基础投资建议（股票、基金、定期存款等）\n" +
            "5. 税务规划与优化\n" +
            "6. 退休规划\n" +
            "7. 保险配置建议\n\n" +
            "在回答用户问题时，请遵循以下原则：\n" +
            "- 保持专业且易于理解的语言\n" +
            "- 提供具体、可操作的建议\n" +
            "- 解释财务概念时使用简单的类比\n" +
            "- 尊重用户的财务状况，不做过度假设\n" +
            "- 鼓励健康的财务习惯和长期规划\n" +
            "- 提醒用户重要的财务决策应咨询专业人士\n" +
            "- 所有回答均使用中文\n\n" +
            "当用户提出非财务相关问题时，礼貌地将话题引导回财务领域。";
    
    // 存储内容相关
    private StringBuilder currentResponse;
    private StyledDocument doc;
    private Style userStyle;
    private Style assistantStyle;
    private Style systemStyle;
    private Style linkStyle;
    
    // 数据相关
    private final TransactionController transactionController;
    private String transactionSummary;
    private String[] suggestedQuestions = {
        "如何制定月度预算?",
        "如何减少不必要支出?",
        "有哪些理财产品适合我?",
        "如何开始投资理财?",
        "如何快速还清信用卡债务?"
    };
    
    // 聊天状态
    private boolean isAiTyping = false;
    
    public AIChatPanel(TransactionController transactionController) {
        this.transactionController = transactionController;
        
        // 初始化线程池
        executorService = Executors.newCachedThreadPool();
        
        // 加载图标资源
        ImageIcon tempSendIcon = null;
        try {
            tempSendIcon = new ImageIcon(getClass().getResource("/icons/send_icon.png"));
            if (tempSendIcon.getIconWidth() > 0) {
                Image img = tempSendIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                tempSendIcon = new ImageIcon(img);
            } else {
                // 如果图标加载失败，创建一个默认图标
                tempSendIcon = null;
            }
        } catch (Exception e) {
            // 如果图标加载失败，使用null
            tempSendIcon = null;
        }
        sendIcon = tempSendIcon;
        
        // 尝试加载AI头像，如果加载失败，使用默认图标
        ImageIcon tempAiIcon = null;
        try {
            tempAiIcon = new ImageIcon(getClass().getResource("/icons/ai_avatar.png"));
            if (tempAiIcon.getIconWidth() > 0) {
                Image img = tempAiIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                tempAiIcon = new ImageIcon(img);
            } else {
                // 如果图标加载失败，创建一个默认图标
                tempAiIcon = createDefaultAvatar();
            }
        } catch (Exception e) {
            // 如果无法加载图标，创建一个默认图标
            tempAiIcon = createDefaultAvatar();
        }
        aiAvatar = tempAiIcon;
        
        // 初始化字体
        try {
            chatFont = new Font("微软雅黑", Font.PLAIN, 14);
            boldFont = new Font("微软雅黑", Font.BOLD, 15);
        } catch (Exception e) {
            chatFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
            boldFont = new Font(Font.SANS_SERIF, Font.BOLD, 15);
        }
        
        // 设置面板属性
        setLayout(new BorderLayout(0, 5));
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setBackground(Color.WHITE);
        
        // 初始化UI组件
        initializeComponents();
        
        // 初始化聊天区域样式
        initializeStyles();
        
        // 初始化当前回复
        currentResponse = new StringBuilder();
        
        // 加载交易数据并生成摘要
        generateTransactionSummary();
        
        // 添加初始化消息
        appendSystemMessage("欢迎使用财智助手，正在加载您的数据...");
        generateInitialAIGreeting();
    }
    
    private void initializeComponents() {
        // ======== 创建顶部面板 ========
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(primaryColor);
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // 标题标签
        titleLabel = new JLabel("财智助手 - 您的个人财务顾问");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        // 助手头像
        avatarLabel = new JLabel(aiAvatar);
        avatarLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        // 组合到顶部面板
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        titlePanel.add(avatarLabel);
        titlePanel.add(Box.createHorizontalStrut(10));
        titlePanel.add(titleLabel);
        
        // 帮助按钮
        helpButton = new JButton("?");
        helpButton.setFont(new Font("Arial", Font.BOLD, 14));
        helpButton.setForeground(Color.WHITE);
        helpButton.setBackground(primaryColor);
        helpButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        helpButton.setPreferredSize(new Dimension(30, 30));
        helpButton.setToolTipText("查看帮助信息");
        helpButton.setFocusPainted(false);
        helpButton.addActionListener(e -> showHelpDialog());
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(helpButton, BorderLayout.EAST);
        
        // 添加顶部面板
        add(headerPanel, BorderLayout.NORTH);
        
        // ======== 创建聊天区域 ========
        chatArea = new JTextPane();
        chatArea.setContentType("text/html");
        chatArea.setEditable(false);
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        chatArea.setFont(chatFont);
        chatArea.setBackground(Color.WHITE);
        doc = chatArea.getStyledDocument();
        
        // 使聊天区域支持超链接
        chatArea.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AIChatPanel.this, 
                                "无法打开链接: " + ex.getMessage(), 
                                "链接错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // ======== 创建快速提问面板 ========
        suggestionPanel = new JPanel();
        suggestionPanel.setLayout(new BoxLayout(suggestionPanel, BoxLayout.X_AXIS));
        suggestionPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        suggestionPanel.setBackground(secondaryColor);
        
        quickSuggestionButtons = new JButton[suggestedQuestions.length];
        for (int i = 0; i < suggestedQuestions.length; i++) {
            quickSuggestionButtons[i] = createSuggestionButton(suggestedQuestions[i]);
            suggestionPanel.add(quickSuggestionButtons[i]);
            if (i < suggestedQuestions.length - 1) {
                suggestionPanel.add(Box.createHorizontalStrut(10));
            }
        }
        
        add(suggestionPanel, BorderLayout.NORTH);
        
        // ======== 创建输入区域 ========
        inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(Color.WHITE);
        
        // 状态标签 - 显示AI状态
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("微软雅黑", Font.ITALIC, 12));
        statusLabel.setForeground(Color.GRAY);
        
        // 输入字段
        inputField = new JTextField();
        inputField.setFont(chatFont);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primaryColor, 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        
        // 输入面板 - 包含输入框和发送按钮
        JPanel textInputPanel = new JPanel(new BorderLayout(5, 0));
        textInputPanel.setOpaque(false);
        
        // 发送按钮
        if (sendIcon != null) {
            sendButton = new JButton(sendIcon);
        } else {
            sendButton = new JButton("发送");
        }
        if (sendIcon == null) {
            sendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        }
        sendButton.setBackground(primaryColor);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // 清除按钮
        clearButton = new JButton("清空");
        clearButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        clearButton.setBackground(Color.LIGHT_GRAY);
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        clearButton.addActionListener(e -> clearChat());
        
        // 输入状态进度条
        typingIndicator = new JProgressBar();
        typingIndicator.setIndeterminate(true);
        typingIndicator.setVisible(false);
        typingIndicator.setPreferredSize(new Dimension(100, 5));
        typingIndicator.setForeground(accentColor);
        typingIndicator.setBorder(null);
        
        // 组装输入面板
        textInputPanel.add(inputField, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new BorderLayout(5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(clearButton, BorderLayout.WEST);
        buttonPanel.add(sendButton, BorderLayout.EAST);
        
        textInputPanel.add(buttonPanel, BorderLayout.EAST);
        
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 5));
        bottomPanel.setOpaque(false);
        bottomPanel.add(textInputPanel, BorderLayout.CENTER);
        
        JPanel statusPanel = new JPanel(new BorderLayout(5, 0));
        statusPanel.setOpaque(false);
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(typingIndicator, BorderLayout.EAST);
        
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        
        inputPanel.add(bottomPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        
        // 添加事件监听
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void initializeStyles() {
        // 定义用户消息样式
        userStyle = chatArea.addStyle("UserStyle", null);
        StyleConstants.setForeground(userStyle, Color.BLACK);
        StyleConstants.setBackground(userStyle, userBubbleColor);
        StyleConstants.setFontFamily(userStyle, chatFont.getFamily());
        StyleConstants.setFontSize(userStyle, chatFont.getSize());
        
        // 定义助手消息样式
        assistantStyle = chatArea.addStyle("AssistantStyle", null);
        StyleConstants.setForeground(assistantStyle, Color.BLACK);
        StyleConstants.setBackground(assistantStyle, aiBubbleColor);
        StyleConstants.setFontFamily(assistantStyle, chatFont.getFamily());
        StyleConstants.setFontSize(assistantStyle, chatFont.getSize());
        
        // 定义系统消息样式
        systemStyle = chatArea.addStyle("SystemStyle", null);
        StyleConstants.setForeground(systemStyle, Color.GRAY);
        StyleConstants.setFontFamily(systemStyle, chatFont.getFamily());
        StyleConstants.setFontSize(systemStyle, chatFont.getSize() - 1);
        StyleConstants.setItalic(systemStyle, true);
        
        // 定义超链接样式
        linkStyle = chatArea.addStyle("LinkStyle", null);
        StyleConstants.setForeground(linkStyle, Color.BLUE);
        StyleConstants.setUnderline(linkStyle, true);
        StyleConstants.setFontFamily(linkStyle, chatFont.getFamily());
        StyleConstants.setFontSize(linkStyle, chatFont.getSize());
    }

    private ImageIcon createDefaultAvatar() {
        // 创建一个默认的AI头像
        BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置渲染质量
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 填充圆形背景
        g2d.setColor(primaryColor);
        g2d.fillOval(0, 0, 40, 40);
        
        // 绘制文字
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "财";
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        g2d.drawString(text, (40 - textWidth) / 2, 20 + textHeight / 4);
        
        g2d.dispose();
        return new ImageIcon(image);
    }

    private JButton createSuggestionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(primaryColor);
        button.setBorder(BorderFactory.createLineBorder(primaryColor, 1, true));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(primaryColor);
                button.setForeground(Color.WHITE);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(primaryColor);
            }
        });
        
        // 添加点击处理
        button.addActionListener(e -> {
            inputField.setText(text);
            sendMessage();
        });
        
        return button;
    }

    private void showHelpDialog() {
        String helpText = "<html><body style='width: 350px; font-family: 微软雅黑; font-size: 14px;'>" +
                "<h2 style='color: #0078D4;'>财智助手使用指南</h2>" +
                "<p>财智助手是您的个人理财顾问，可以帮助您：</p>" +
                "<ul>" +
                "<li>分析您的收支情况</li>" +
                "<li>提供预算建议</li>" +
                "<li>回答理财问题</li>" +
                "<li>提供债务管理和投资指导</li>" +
                "<li>解释财务概念和术语</li>" +
                "</ul>" +
                "<p><b>使用方法：</b></p>" +
                "<ol>" +
                "<li>在输入框中输入您的问题</li>" +
                "<li>点击发送按钮或按回车键</li>" +
                "<li>可以点击上方的快速提问按钮</li>" +
                "</ol>" +
                "<p><b>提示：</b> 财智助手会根据您的交易数据提供个性化建议。越具体的问题会得到更精确的回答。</p>" +
                "</body></html>";
        
        JOptionPane.showMessageDialog(this, 
                helpText, 
                "财智助手使用指南", 
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearChat() {
        int option = JOptionPane.showConfirmDialog(this,
                "确定要清空所有聊天记录吗？", 
                "确认清空", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            chatArea.setText("");
            appendSystemMessage("聊天记录已清空。有什么可以帮您的？");
        }
    }
    
    /**
     * 生成交易数据摘要
     */
    private void generateTransactionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("以下是用户的财务数据摘要，请基于这些信息提供个性化的财务建议：\n\n");
        
        try {
            // 加载交易数据
            transactionController.loadTransactions();
            List<Transaction> transactions = transactionController.getTransactions();
            
            // 添加基本统计信息
            summary.append("1. 交易记录总数：").append(transactions.size()).append("\n");
            
            // 获取当月支出
            Map<String, Double> currentExpenses = transactionController.getCurrentMonthExpenses();
            double totalExpenses = currentExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
            summary.append("2. 当月总支出：¥").append(String.format("%.2f", totalExpenses)).append("\n");
            
            // 添加主要支出类别
            summary.append("3. 主要支出类别：\n");
            currentExpenses.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // 按金额降序排序
                .limit(5) // 获取前5个类别
                .forEach(entry -> summary.append("   - ").append(entry.getKey())
                                         .append(": ¥").append(String.format("%.2f", entry.getValue())).append("\n"));
            
            // 添加预算建议
            List<String> budgetSuggestions = transactionController.getBudgetSuggestions();
            if (!budgetSuggestions.isEmpty()) {
                summary.append("4. 系统预算建议：\n");
                for (String suggestion : budgetSuggestions) {
                    summary.append("   - ").append(suggestion).append("\n");
                }
            }
            
            // 最近交易记录
            summary.append("5. 最近交易记录（最多10条）：\n");
            transactions.stream()
                .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate())) // 按日期降序排序
                .limit(10) // 获取最近的10条记录
                .forEach(transaction -> {
                    summary.append("   - 日期: ").append(transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                           .append(", 类别: ").append(transaction.getCategory())
                           .append(", 金额: ¥").append(String.format("%.2f", transaction.getAmount()))
                           .append(", 描述: ").append(transaction.getDescription())
                           .append("\n");
                });
            
            summary.append("\n请基于以上数据为用户提供个性化的财务建议。分析用户的消费模式，指出可能的问题，并提供改进建议。");
            
            transactionSummary = summary.toString();
            
        } catch (IOException e) {
            transactionSummary = "无法加载用户交易数据，请提供一般性的财务建议。";
            e.printStackTrace();
        }
    }
    
    /**
     * 生成初始AI问候和建议
     */
    private void generateInitialAIGreeting() {
        // 显示加载状态
        setTypingStatus(true);
        
        // 发送到API并获取初始建议
        executorService.submit(() -> {
            try {
                // 等待500ms让UI更新
                Thread.sleep(500);
                
                // 添加问候标题
                appendAssistantHeader();
                
                // 使用一个特殊提示来生成初始建议
                String initialPrompt = "请基于我的财务数据提供一个友好的问候并给出3-5条个性化的财务建议。";
                getAIResponseStreaming(initialPrompt, true);
                
                // 回复完成后添加额外的换行
                appendNewline();
            } catch (Exception e) {
                // 如果API调用失败，显示默认欢迎消息
                appendAssistantMessage("您好！我是您的个人财务顾问「财智助手」。\n\n" +
                        "根据您的财务数据，我有以下建议：\n" +
                        "1. 您本月的支出主要集中在几个类别，建议关注是否有可以优化的空间\n" +
                        "2. 考虑设置每月预算计划，帮助您更好地控制支出\n" +
                        "3. 定期检查您的财务状况，及时调整理财策略\n\n" +
                        "有什么财务问题需要我帮助解答吗？");
                e.printStackTrace();
            } finally {
                // 无论是否发生错误，都结束加载状态
                SwingUtilities.invokeLater(() -> setTypingStatus(false));
            }
        });
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            // 显示用户消息
            appendUserMessage(message);
            inputField.setText("");
            
            // 禁用输入框和发送按钮，直到回复完成
            setInputEnabled(false);
            
            // 显示正在输入状态
            setTypingStatus(true);
            
            // 重置当前回复
            currentResponse = new StringBuilder();

            // 发送到API并获取响应
            executorService.submit(() -> {
                try {
                    // 显示AI标识但不换行，为后续流式内容做准备
                    appendAssistantHeader();
                    
                    // 流式获取回复
                    getAIResponseStreaming(message, false);
                    
                    // 回复完成后添加额外的换行
                    appendNewline();
                } catch (Exception e) {
                    appendSystemMessage("错误: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // 无论是否发生错误，都重新启用输入
                    SwingUtilities.invokeLater(() -> {
                        setInputEnabled(true);
                        setTypingStatus(false);
                    });
                }
            });
        }
    }
    
    private void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
        for (JButton button : quickSuggestionButtons) {
            button.setEnabled(enabled);
        }
    }

    private void setTypingStatus(boolean isTyping) {
        SwingUtilities.invokeLater(() -> {
            isAiTyping = isTyping;
            typingIndicator.setVisible(isTyping);
            statusLabel.setText(isTyping ? "财智助手正在思考..." : "");
        });
    }

    private void appendUserMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 创建用户消息样式
                doc.insertString(doc.getLength(), getCurrentTimeStamp() + " 您: \n", null);
                
                // 为消息添加气泡样式背景
                JTextPane tempPane = new JTextPane();
                StyledDocument tempDoc = tempPane.getStyledDocument();
                tempDoc.insertString(0, message, userStyle);
                
                // 创建气泡样式
                MatteBorder border = new MatteBorder(10, 15, 10, 15, userBubbleColor);
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_RIGHT);
                
                // 插入带样式的消息
                chatArea.setCaretPosition(doc.getLength());
                chatArea.insertComponent(createMessageBubble(message, userBubbleColor, true));
                doc.insertString(doc.getLength(), "\n\n", null);
                
                scrollToBottom();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private void appendAssistantHeader() {
        SwingUtilities.invokeLater(() -> {
            try {
                doc.insertString(doc.getLength(), getCurrentTimeStamp() + " 财智助手:\n", null);
                scrollToBottom();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private void appendAssistantMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                chatArea.setCaretPosition(doc.getLength());
                chatArea.insertComponent(createMessageBubble(message, aiBubbleColor, false));
                doc.insertString(doc.getLength(), "\n\n", null);
                scrollToBottom();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private void appendSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                doc.insertString(doc.getLength(), message + "\n\n", systemStyle);
                scrollToBottom();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private JTextPane createMessageBubble(String message, Color bgColor, boolean isUser) {
        JTextPane bubblePane = new JTextPane();
        bubblePane.setEditable(false);
        bubblePane.setBackground(bgColor);
        bubblePane.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        bubblePane.setFont(chatFont);
        
        // 设置最大宽度和自动换行
        int maxWidth = getWidth() - 100;
        bubblePane.setSize(new Dimension(maxWidth, Short.MAX_VALUE));
        
        try {
            StyledDocument doc = bubblePane.getStyledDocument();
            doc.insertString(0, message, null);
            
            // 检测并添加可点击的链接
            addHyperlinks(doc, message);
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        
        // 设置边距和圆角
        bubblePane.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(bgColor, 15, isUser), 
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        
        return bubblePane;
    }

    private void addHyperlinks(StyledDocument doc, String text) {
        // 简单的URL检测和添加超链接（可进一步优化）
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (word.startsWith("http://") || word.startsWith("https://")) {
                try {
                    int start = text.indexOf(word);
                    if (start >= 0) {
                        SimpleAttributeSet linkAttr = new SimpleAttributeSet();
                        StyleConstants.setForeground(linkAttr, Color.BLUE);
                        StyleConstants.setUnderline(linkAttr, true);
                        doc.setCharacterAttributes(start, word.length(), linkAttr, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 圆角边框类
    class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        private final boolean rightAligned;
        
        public RoundedBorder(Color color, int radius, boolean rightAligned) {
            this.color = color;
            this.radius = radius;
            this.rightAligned = rightAligned;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
        
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    private void appendStreamContent(String content) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 对于第一部分内容，创建新的消息气泡
                if (currentResponse.length() == 0) {
                    chatArea.setCaretPosition(doc.getLength());
                    chatArea.insertComponent(createMessageBubble(content, aiBubbleColor, false));
                    currentResponse.append(content);
                } else {
                    // 替换现有组件
                    currentResponse.append(content);
                    Component[] components = chatArea.getComponents();
                    if (components.length > 0) {
                        Component lastComponent = components[components.length - 1];
                        if (lastComponent instanceof JTextPane) {
                            JTextPane lastBubble = (JTextPane) lastComponent;
                            try {
                                lastBubble.setText(currentResponse.toString());
                                addHyperlinks(lastBubble.getStyledDocument(), currentResponse.toString());
                            } catch (Exception e) {
                                // 如果更新失败，创建新的消息气泡
                                chatArea.setCaretPosition(doc.getLength());
                                chatArea.insertComponent(createMessageBubble(currentResponse.toString(), aiBubbleColor, false));
                            }
                        } else {
                            // 如果最后一个组件不是文本面板，创建新的
                            chatArea.setCaretPosition(doc.getLength());
                            chatArea.insertComponent(createMessageBubble(currentResponse.toString(), aiBubbleColor, false));
                        }
                    } else {
                        // 如果没有组件，创建新的
                        chatArea.setCaretPosition(doc.getLength());
                        chatArea.insertComponent(createMessageBubble(currentResponse.toString(), aiBubbleColor, false));
                    }
                }
                scrollToBottom();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void appendNewline() {
        SwingUtilities.invokeLater(() -> {
            try {
                doc.insertString(doc.getLength(), "\n\n", null);
                scrollToBottom();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private String getCurrentTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return "[" + sdf.format(new Date()) + "]";
    }

    private void getAIResponseStreaming(String message, boolean includeTransactionData) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setDoOutput(true);

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "deepseek-chat");
        
        // 创建消息数组
        JSONArray messagesArray = new JSONArray();
        
        // 添加系统提示词
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);
        messagesArray.put(systemMessage);
        
        // 如果需要包含交易数据，添加交易数据消息
        if (includeTransactionData && transactionSummary != null) {
            JSONObject transactionDataMessage = new JSONObject();
            transactionDataMessage.put("role", "user");
            transactionDataMessage.put("content", transactionSummary);
            messagesArray.put(transactionDataMessage);
            
            // 添加AI回复占位符
            JSONObject aiPlaceholderMessage = new JSONObject();
            aiPlaceholderMessage.put("role", "assistant");
            aiPlaceholderMessage.put("content", "我已收到并分析了您的财务数据。");
            messagesArray.put(aiPlaceholderMessage);
        }
        
        // 添加用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messagesArray.put(userMessage);
        
        requestBody.put("messages", messagesArray);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);
        // 启用流式输出
        requestBody.put("stream", true);

        // 发送请求
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 读取流式响应
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6);
                    if (data.equals("[DONE]")) {
                        break;
                    }
                    
                    try {
                        JSONObject jsonResponse = new JSONObject(data);
                        JSONArray choices = jsonResponse.getJSONArray("choices");
                        if (choices.length() > 0) {
                            JSONObject choice = choices.getJSONObject(0);
                            JSONObject delta = choice.getJSONObject("delta");
                            if (delta.has("content")) {
                                String content = delta.getString("content");
                                // 累积响应
                                currentResponse.append(content);
                                // 实时显示增量内容
                                appendStreamContent(content);
                            }
                        }
                    } catch (Exception e) {
                        // 如果解析特定行失败，继续处理下一行
                        System.err.println("无法解析流数据行: " + data);
                    }
                }
            }
        } catch (Exception e) {
            // 如果API调用失败，显示错误信息
            String errorMessage = "\n抱歉，我现在无法连接到AI服务。请稍后再试。错误信息: " + e.getMessage();
            currentResponse.append(errorMessage);
            appendStreamContent(errorMessage);
            throw e;
        }
    }

    // 保留原方法以支持非流式API调用（备用方案）
    private String getAIResponse(String message, boolean includeTransactionData) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setDoOutput(true);

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "deepseek-chat");
        
        // 创建消息数组
        JSONArray messagesArray = new JSONArray();
        
        // 添加系统提示词
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);
        messagesArray.put(systemMessage);
        
        // 如果需要包含交易数据，添加交易数据消息
        if (includeTransactionData && transactionSummary != null) {
            JSONObject transactionDataMessage = new JSONObject();
            transactionDataMessage.put("role", "user");
            transactionDataMessage.put("content", transactionSummary);
            messagesArray.put(transactionDataMessage);
            
            // 添加AI回复占位符
            JSONObject aiPlaceholderMessage = new JSONObject();
            aiPlaceholderMessage.put("role", "assistant");
            aiPlaceholderMessage.put("content", "我已收到并分析了您的财务数据。");
            messagesArray.put(aiPlaceholderMessage);
        }
        
        // 添加用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messagesArray.put(userMessage);
        
        requestBody.put("messages", messagesArray);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);

        // 发送请求
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 读取响应
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        } catch (Exception e) {
            // 如果API调用失败，返回一个友好的响应
            return "抱歉，我现在无法连接到AI服务。请稍后再试。错误信息: " + e.getMessage();
        }

        try {
            // 解析响应
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (Exception e) {
            return "抱歉，我无法理解AI服务的响应。错误信息: " + e.getMessage();
        }
    }
} 