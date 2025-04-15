# Personal Finance Management Software

A financial management system for recording and analyzing personal income and expenses.

## Features

- Transaction Records: Add, import, and delete transaction records
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
├── bin/                          # Compiled class files
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── financeapp/
│   │               ├── controller/    # Controller classes
│   │               │   ├── AuthController.java
│   │               │   └── TransactionController.java
│   │               ├── model/         # Model classes
│   │               │   ├── Transaction.java
│   │               │   ├── BudgetCalculator.java
│   │               │   └── SimpleBudgetCalculator.java
│   │               ├── service/       # Service classes
│   │               ├── config/        # Configuration classes
│   │               ├── constants/     # Constant classes
│   │               ├── view/          # View classes
│   │               │   ├── MainFrame.java          # Main window
│   │               │   ├── LoginPanel.java         # Login panel
│   │               │   ├── RegisterPanel.java      # Registration panel
│   │               │   ├── TransactionPanel.java   # Transaction panel
│   │               │   ├── CategoryPanel.java      # Category panel
│   │               │   ├── StatisticsPanel.java    # Statistics panel
│   │               │   ├── DashboardPanel.java     # Dashboard panel
│   │               │   ├── ExpenseAlertPanel.java  # Expense Alert panel
│   │               │   ├── LocalConsumptionPanel.java  # Local Consumption panel
│   │               │   └── LocalizedFinancePanel.java  # Localized Finance panel
│   │               └── util/          # Utility classes
│   │                   └── CSVHandler.java     # CSV handling utility
│   └── test/
│       └── java/
│           └── com/
│               └── financeapp/       # Test classes
│                   └── TransactionTest.java
├── data/                          # Data directory
│   ├── transactions.csv          # Transaction records
│   ├── users.csv                 # User information
│   └── test_import.csv           # Test import file
├── pom.xml                       # Maven project configuration (currently unused)
├── run.bat                       # One-click compile and run batch file
└── README.md                     # Project documentation
```

## Quick Start

### Using Batch File (Recommended)

1. Double-click the `run.bat` file in the project root directory, or execute in command line:
   ```
   .\run.bat
   ```
   This command will automatically compile all Java source files and start the application.

### Manual Compilation and Execution

1. Create a bin directory in the project root (if it doesn't exist):
   ```
   mkdir bin
   ```

2. Compile all Java source files:
   ```
   javac -d bin -cp src\main\java src\main\java\com\financeapp\view\*.java src\main\java\com\financeapp\model\*.java src\main\java\com\financeapp\controller\*.java src\main\java\com\financeapp\util\*.java
   ```

3. Run the application:
   ```
   java -cp "bin;data" com.financeapp.view.MainFrame
   ```

## Usage Instructions

### Adding Transaction Records

1. Fill in the date, amount, category (optional), and notes in the "Transaction Records" tab
2. Click the "Add" button to save the transaction record

### Importing CSV Files

1. Click the "Import CSV" button in the "Transaction Records" tab
2. Select a CSV file in the correct format (format: date,category,amount,notes)
3. The system will import all transaction records

### Managing Categories

1. Select the transaction to modify in the "Category Management" tab
2. Enter the correct category in the "New Category" text box
3. Click the "Update Category" button to save the changes

### Viewing Statistics

1. Select the year and month to view in the "Statistics View" tab
2. Click the "Query" button to display the expense statistics for that month
3. View the budget recommendations below

## TODO List

- Implement more complete lunar calendar conversion logic
- Add more category rules
- Support chart export functionality
- Implement budget setting functionality
- Add multi-currency support

## License

This project is licensed under the MIT License. 