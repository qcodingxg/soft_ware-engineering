package com.financeapp.view.components;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;


/**
 * 聊天气泡工厂类，用于创建聊天界面的各种气泡组件
 */
public class ChatBubbleFactory {
    // 界面颜色定义
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185); // 深蓝色
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219); // 蓝色
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241); // 浅灰色
    private static final Color TEXT_COLOR = new Color(44, 62, 80); // 深灰色
    private static final Color USER_BUBBLE_COLOR = new Color(230, 230, 230); // 浅灰色
    private static final Color BOT_BUBBLE_COLOR = new Color(212, 230, 241); // 浅蓝色
    
    // 字体设置
    private static final Font MESSAGE_FONT = new Font("SansSerif", Font.PLAIN, 15);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 13);
    
    /**
     * 创建用户消息气泡（右侧对齐）
     */
    public static JPanel createUserMessageBubble(String message) {
        // 外部面板，用于布局
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(Color.WHITE);
        outerPanel.setBorder(new EmptyBorder(5, 80, 5, 10)); // 增加左侧留白，使消息更靠右

        // 消息面板
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(USER_BUBBLE_COLOR);
        messagePanel.setBorder(new CompoundBorder(
            new RoundedBorder(10, USER_BUBBLE_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));

        // 消息文本
        JTextArea textArea = new JTextArea(message);
        textArea.setFont(MESSAGE_FONT);
        textArea.setForeground(new Color(30, 30, 30)); // 更深的文本颜色，增加对比度
        textArea.setBackground(USER_BUBBLE_COLOR);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBorder(null);
        
        // 根据内容计算适合的宽度，最大不超过屏幕宽度的3/4
        int preferredWidth = calculateBubbleWidth(message, textArea, true);
        
        // 估计需要的行数和高度
        int textPadding = 30; // 文本区域内边距(左右两侧各15)
        int estimatedLines = estimateLineCount(message, textArea, preferredWidth - textPadding);
        int lineHeight = textArea.getFontMetrics(textArea.getFont()).getHeight();
        int estimatedHeight = estimatedLines * lineHeight + 10; // 额外增加一点高度作为缓冲
        
        // 设置首选尺寸
        textArea.setPreferredSize(new Dimension(preferredWidth, estimatedHeight));

        messagePanel.add(textArea, BorderLayout.CENTER);
        outerPanel.add(messagePanel, BorderLayout.EAST); // 靠右对齐

        // 添加"You"标签
        JLabel userLabel = new JLabel("You");
        userLabel.setFont(LABEL_FONT);
        userLabel.setForeground(new Color(80, 80, 80));
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        labelPanel.setBackground(Color.WHITE);
        labelPanel.add(userLabel);
        
        // 垂直布局面板
        JPanel verticalPanel = new JPanel();
        verticalPanel.setLayout(new BoxLayout(verticalPanel, BoxLayout.Y_AXIS));
        verticalPanel.setBackground(Color.WHITE);
        verticalPanel.add(labelPanel);
        verticalPanel.add(outerPanel);
        
        return verticalPanel;
    }

    /**
     * 创建AI消息气泡（左侧对齐）
     */
    public static JPanel createAIMessageBubble(String message) {
        // 外部面板，用于布局
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(Color.WHITE);
        outerPanel.setBorder(new EmptyBorder(5, 10, 5, 80)); // 增加右侧留白，使消息更靠左

        // 消息面板
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(BOT_BUBBLE_COLOR);
        messagePanel.setBorder(new CompoundBorder(
            new RoundedBorder(10, BOT_BUBBLE_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));

        // 消息文本
        JTextArea textArea = new JTextArea(message);
        textArea.setFont(MESSAGE_FONT);
        textArea.setForeground(new Color(30, 30, 30)); // 更深的文本颜色，增加对比度
        textArea.setBackground(BOT_BUBBLE_COLOR);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBorder(null);
        
        // 根据内容计算适合的宽度，最大不超过屏幕宽度的3/4
        int preferredWidth = calculateBubbleWidth(message, textArea, false);
        
        // 估计需要的行数和高度
        int textPadding = 30; // 文本区域内边距(左右两侧各15)
        int estimatedLines = estimateLineCount(message, textArea, preferredWidth - textPadding);
        int lineHeight = textArea.getFontMetrics(textArea.getFont()).getHeight();
        int estimatedHeight = estimatedLines * lineHeight + 10; // 额外增加一点高度作为缓冲
        
        // 设置首选尺寸
        textArea.setPreferredSize(new Dimension(preferredWidth, estimatedHeight));

        messagePanel.add(textArea, BorderLayout.CENTER);
        outerPanel.add(messagePanel, BorderLayout.WEST); // 靠左对齐
        
        // 添加"AI Financial Advisor"标签
        JLabel aiLabel = new JLabel("AI Financial Advisor");
        aiLabel.setFont(LABEL_FONT);
        aiLabel.setForeground(new Color(80, 80, 80));
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelPanel.setBackground(Color.WHITE);
        labelPanel.add(aiLabel);
        
        // 垂直布局面板
        JPanel verticalPanel = new JPanel();
        verticalPanel.setLayout(new BoxLayout(verticalPanel, BoxLayout.Y_AXIS));
        verticalPanel.setBackground(Color.WHITE);
        verticalPanel.add(labelPanel);
        verticalPanel.add(outerPanel);
        
        return verticalPanel;
    }

    /**
     * 创建流式AI消息气泡（左侧对齐，返回JTextArea以便后续更新内容）
     */
    public static JTextArea createStreamingAIMessageBubble(JPanel chatMessagesPanel, List<Component> messageComponents) {
        // 初始使用较小的宽度，后续会动态调整
        int initialWidth = 250;
        
        // 外部面板，用于布局
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(Color.WHITE);
        outerPanel.setBorder(new EmptyBorder(5, 10, 5, 80)); // 增加右侧留白，使消息更靠左

        // 消息面板
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(BOT_BUBBLE_COLOR);
        messagePanel.setBorder(new CompoundBorder(
            new RoundedBorder(10, BOT_BUBBLE_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));

        // 消息文本
        JTextArea textArea = new JTextArea();
        textArea.setFont(MESSAGE_FONT);
        textArea.setForeground(new Color(30, 30, 30)); // 更深的文本颜色，增加对比度
        textArea.setBackground(BOT_BUBBLE_COLOR);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBorder(null);
        
        // 初始设置较小宽度，后续会根据内容动态调整
        textArea.setPreferredSize(new Dimension(initialWidth, 20));

        messagePanel.add(textArea, BorderLayout.CENTER);
        outerPanel.add(messagePanel, BorderLayout.WEST); // 靠左对齐
        
        // 添加"AI Financial Advisor"标签
        JLabel aiLabel = new JLabel("AI Financial Advisor");
        aiLabel.setFont(LABEL_FONT);
        aiLabel.setForeground(new Color(80, 80, 80));
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelPanel.setBackground(Color.WHITE);
        labelPanel.add(aiLabel);
        
        // 垂直布局面板
        JPanel verticalPanel = new JPanel();
        verticalPanel.setLayout(new BoxLayout(verticalPanel, BoxLayout.Y_AXIS));
        verticalPanel.setBackground(Color.WHITE);
        verticalPanel.add(labelPanel);
        verticalPanel.add(outerPanel);
        
        // 添加到消息面板
        chatMessagesPanel.add(verticalPanel);
        messageComponents.add(verticalPanel);
        chatMessagesPanel.add(Box.createVerticalStrut(15)); // 添加一些底部间距
        
        // 更新UI
        chatMessagesPanel.revalidate();
        chatMessagesPanel.repaint();
        
        return textArea;
    }
    
    /**
     * 更新流式消息气泡的内容和大小
     */
    public static void updateStreamingMessageBubble(JTextArea textArea, String content, JPanel chatMessagesPanel) {
        // 添加内容到文本区域
        textArea.append(content);
        
        // 获取当前内容的总文本
        String fullText = textArea.getText();
        
        // 根据内容动态计算适合的宽度
        int preferredWidth = calculateBubbleWidth(fullText, textArea, false);
        
        // 估计需要的行数
        int textPadding = 30; // 文本区域内边距(左右两侧各15)
        int estimatedLines = estimateLineCount(fullText, textArea, preferredWidth - textPadding);
        int lineHeight = textArea.getFontMetrics(textArea.getFont()).getHeight();
        int estimatedHeight = estimatedLines * lineHeight + 10; // 额外增加一点高度作为缓冲
        
        // 设置新的首选大小
        textArea.setPreferredSize(new Dimension(preferredWidth, estimatedHeight));
        
        // 强制重新布局并验证大小
        Container parent = textArea.getParent();
        while (parent != null) {
            parent.invalidate();
            parent = parent.getParent();
        }
        chatMessagesPanel.revalidate();
        chatMessagesPanel.repaint();
    }
    
    /**
     * 创建标题面板
     */
    public static JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // 创建标题标签
        JLabel titleLabel = new JLabel("AI Financial Advisor");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.WEST);
        
        // 创建状态标签
        JLabel statusLabel = new JLabel("Online");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(Color.WHITE);
        panel.add(statusLabel, BorderLayout.EAST);
        
        // 设置圆角
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(5, PRIMARY_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        return panel;
    }
    
    /**
     * 创建进度条面板
     */
    public static JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(5, SECONDARY_COLOR),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // 创建进度条标签
        JLabel progressLabel = new JLabel("Analyzing your transaction data... 📊");
        progressLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        progressLabel.setForeground(TEXT_COLOR);
        
        // 创建进度条
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setForeground(SECONDARY_COLOR);
        progressBar.setBackground(BACKGROUND_COLOR);
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(250, 15));
        
        // 创建包含进度条和文本的面板
        JPanel progressContentPanel = new JPanel(new BorderLayout(10, 5));
        progressContentPanel.setOpaque(false);
        progressContentPanel.add(progressLabel, BorderLayout.NORTH);
        progressContentPanel.add(progressBar, BorderLayout.CENTER);
        
        panel.add(progressContentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 计算文本在给定宽度下需要的行数
     */
    private static int estimateLineCount(String text, JTextArea textArea, int width) {
        // 创建一个临时的字体度量对象用于计算
        FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
        
        // 考虑到单词换行，这里做一个粗略估计
        int charWidth = fm.charWidth('a'); // 使用字母'a'的宽度作为平均字符宽度
        int charsPerLine = Math.max(1, width / charWidth);
        
        // 计算文本的行数（考虑已有的换行符）
        String[] lines = text.split("\n");
        int totalLines = 0;
        
        for (String line : lines) {
            // 每行文本可能需要自动换行，所以计算它需要的额外行数
            int lineLength = line.length();
            int linesNeeded = (int) Math.ceil((double) lineLength / charsPerLine);
            totalLines += Math.max(1, linesNeeded);
        }
        
        return totalLines;
    }
    
    /**
     * 获取屏幕宽度的3/4作为消息气泡的最大宽度
     */
    private static int getMaxBubbleWidth() {
        // 获取屏幕尺寸
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // 返回屏幕宽度的3/4
        return (int)(screenSize.getWidth() * 0.75);
    }
    
    /**
     * 根据文本内容计算适合的气泡宽度，最大不超过屏幕宽度的3/4
     */
    private static int calculateBubbleWidth(String message, JTextArea textArea, boolean isUserMessage) {
        // 获取最大宽度
        int maxWidth = getMaxBubbleWidth();
        
        // 计算每个字符的平均宽度
        FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
        int avgCharWidth = fm.charWidth('a');
        
        // 查找消息中最长的一行
        String[] lines = message.split("\n");
        int maxLineLength = 0;
        for (String line : lines) {
            maxLineLength = Math.max(maxLineLength, line.length());
        }
        
        // 计算基于最长行的预估宽度
        int estimatedWidth = maxLineLength * avgCharWidth;
        
        // 根据消息长度设置最小宽度
        int minWidth = isUserMessage ? 200 : 250; // 用户消息和AI消息的最小宽度不同
        
        // 添加一些内边距
        estimatedWidth += 50;
        
        // 返回适合的宽度，不小于最小宽度，不大于最大宽度
        return Math.max(minWidth, Math.min(estimatedWidth, maxWidth));
    }
    
    /**
     * 自定义圆角边框
     */
    public static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        
        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
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
            return new Insets(this.radius, this.radius, this.radius, this.radius);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = this.radius;
            return insets;
        }
    }
} 