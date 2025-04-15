package com.financeapp.util;

import com.financeapp.model.Transaction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV文件处理工具类
 * 负责读写交易数据到CSV文件
 */
public class CSVHandler {
    private static final String DATA_DIRECTORY = "./data";
    private static final String TRANSACTIONS_FILE = DATA_DIRECTORY + "/transactions.csv";
    
    /**
     * 保存交易列表到CSV文件
     * @param transactions 要保存的交易列表
     * @throws IOException 如果保存过程中发生IO错误
     */
    public void saveTransactions(List<Transaction> transactions) throws IOException {
        // 确保目录存在
        ensureDirectoryExists();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRANSACTIONS_FILE))) {
            // 写入标题行
            writer.write("日期,分类,金额,备注");
            writer.newLine();
            
            // 写入每行交易
            for (Transaction transaction : transactions) {
                writer.write(transaction.toCSV());
                writer.newLine();
            }
        }
    }
    
    /**
     * 从CSV文件加载交易列表
     * @return 交易列表
     * @throws IOException 如果加载过程中发生IO错误
     */
    public List<Transaction> loadTransactions() throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        
        // 如果文件不存在，返回空列表
        if (!Files.exists(Paths.get(TRANSACTIONS_FILE))) {
            return transactions;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            // 跳过标题行
            String line = reader.readLine();
            
            // 读取每行交易
            while ((line = reader.readLine()) != null) {
                try {
                    Transaction transaction = Transaction.fromCSV(line);
                    transactions.add(transaction);
                } catch (IllegalArgumentException e) {
                    System.err.println("解析交易行出错：" + e.getMessage());
                    // 继续处理下一行
                }
            }
        }
        
        return transactions;
    }
    
    /**
     * 添加单个交易到CSV文件（追加模式）
     * @param transaction 要添加的交易
     * @throws IOException 如果添加过程中发生IO错误
     */
    public void addTransaction(Transaction transaction) throws IOException {
        // 确保目录存在
        ensureDirectoryExists();
        
        boolean fileExists = Files.exists(Paths.get(TRANSACTIONS_FILE));
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRANSACTIONS_FILE, true))) {
            // 如果文件不存在，先写入标题行
            if (!fileExists) {
                writer.write("日期,分类,金额,备注");
                writer.newLine();
            }
            
            // 写入交易
            writer.write(transaction.toCSV());
            writer.newLine();
        }
    }
    
    /**
     * 确保数据目录存在
     */
    private void ensureDirectoryExists() {
        File directory = new File(DATA_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
} 