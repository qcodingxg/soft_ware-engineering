package com.financeapp.service;

import com.financeapp.model.Transaction;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionService {
    private List<Transaction> transactions;
    private AIClassifier aiClassifier;

    public TransactionService() {
        transactions = new ArrayList<>();
        aiClassifier = new AIClassifier();
    }

    public void addTransaction(Transaction transaction) {
        // 如果交易没有分类，使用AI分类器进行分类
        if (transaction.getCategory() == null || transaction.getCategory().isEmpty()) {
            String category = aiClassifier.classify(transaction);
            transaction.setCategory(category);
        }
        transactions.add(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }

    public List<Transaction> getTransactionsByCategory(String category) {
        return transactions.stream()
            .filter(t -> t.getCategory().equals(category))
            .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return transactions.stream()
            .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
            .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionsByMonth(int year, int month) {
        return transactions.stream()
            .filter(t -> t.getDate().getYear() == year && t.getDate().getMonthValue() == month)
            .collect(Collectors.toList());
    }

    public double getTotalIncome() {
        return transactions.stream()
            .filter(t -> t.getAmount() > 0)
            .mapToDouble(Transaction::getAmount)
            .sum();
    }

    public double getTotalExpense() {
        return transactions.stream()
            .filter(t -> t.getAmount() < 0)
            .mapToDouble(t -> Math.abs(t.getAmount()))
            .sum();
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    public Map<String, Double> getCategoryExpenses() {
        Map<String, Double> expenses = new HashMap<>();
        transactions.stream()
            .filter(t -> t.getAmount() < 0)
            .forEach(t -> expenses.merge(t.getCategory(), Math.abs(t.getAmount()), Double::sum));
        return expenses;
    }

    public void updateTransaction(Transaction oldTransaction, Transaction newTransaction) {
        int index = transactions.indexOf(oldTransaction);
        if (index != -1) {
            transactions.set(index, newTransaction);
        }
    }

    public void deleteTransaction(Transaction transaction) {
        transactions.remove(transaction);
    }

    public void recordCorrection(String description, String category) {
        aiClassifier.recordCorrection(description, category);
    }
} 