# Personal Finance Management Software

A simple personal finance management system for recording and analyzing personal income and expenses.

## Features

- Transaction Records: Add, import, and delete transactions
- AI Classification: Automatically categorize transactions with user correction support
- Statistical Analysis: View monthly expense statistics and budget recommendations
- Festival Detection: Automatically identify and tag consumption during Chinese New Year
- CSV Storage: Use simple CSV files to store data

## System Requirements

- Java 21 (LTS version)
- Swing GUI library (part of the Java standard library)
- JUnit 5 (for running tests)

## Directory Structure

```
FinanceApp/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── financeapp/
│   │               ├── controller/
│   │               │   └── TransactionController.java
│   │               ├── model/
│   │               │   ├── Transaction.java
│   │               │   ├── AIClassifier.java
│   │               │   ├── BudgetCalculator.java
│   │               │   └── SimpleBudgetCalculator.java
│   │               ├── service/
│   │               ├── config/
│   │               ├── constants/
│   │               ├── view/
│   │               │   ├── MainFrame.java
│   │               │   ├── TransactionPanel.java
│   │               │   ├── CategoryPanel.java
│   │               │   └── StatisticsPanel.java
│   │               └── util/
│   │                   └── CSVHandler.java
│   └── test/
│       └── java/
│           └── com/
│               └── financeapp/
│                   ├── TransactionTest.java
│                   └── AIClassifierTest.java
├── data/
│   ├── transactions.csv
│   ├── corrections.log
│   ├── users.csv
│   └── test_import.csv
├── pom.xml
└── README.md
```

## Quick Start

1. Compile and build the project using Maven:
   ```
   mvn clean package
   ```

2. Run the application:
   ```
   java -jar target/finance-app-1.0-SNAPSHOT.jar
   ```

3. Run tests:
   ```
   mvn test
   ```

## Usage Instructions

### Add Transaction

1. Fill in the date, amount, category (optional), and notes in the "Transaction Entry" tab
2. Click the "Add" button to save the transaction
3. The system will automatically attempt to AI classify unclassified transactions

### Import CSV

1. Click the "Import CSV" button in the "Transaction Entry" tab
2. Select a CSV file in the correct format (format: date, category, amount, notes)
3. The system will import all transaction records and AI classify them

### Correct Category

1. Select the transaction to modify in the "Category Management" tab
2. Enter the correct category in the "New Category" text box
3. Click the "Update Category" button to save the correction
4. The system will remember this correction for future similar transactions

### View Statistics

1. Select the year and month to view in the "Statistics View" tab
2. Click the "Query" button to display the expense statistics for that month
3. View the budget recommendations below

## TODO List

- Implement a more complete lunar calendar conversion logic
- Add more category rules
- Support chart export functionality
- Implement budget setting functionality
- Add multi-currency support

## License

This project is licensed under the MIT License. 