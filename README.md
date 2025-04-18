# Personal Finance Management Software

A comprehensive personal finance management system for recording and analyzing personal income and expenses, with integrated intelligent financial advisory services.

## Features

- **Transaction Management**: Add, import, edit, and delete transaction records
- **Statistical Analysis**: View monthly expense statistics and budget recommendations
- **Festival Detection**: Automatically identify and tag consumption during Chinese New Year
- **Data Persistence**: Use simple CSV files to securely store data
- **User Authentication**: Support for multiple user accounts
- **AI Financial Advisor**: Integrated intelligent AI assistant providing personalized financial advice
- **Expense Alerts**: Monitor unusual spending patterns and provide alerts
- **Localized Financial Analysis**: Analyze consumption patterns based on geographic location
- **Budget Calculator**: Provide intelligent budget suggestions and analysis

## System Requirements

- Java 21 (LTS version)
- Swing GUI library (part of the Java standard library)
- JUnit 5 (for running tests)
- External dependencies: org.json (for JSON processing)

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
│   │               │   ├── AuthController.java       # Authentication controller
│   │               │   └── TransactionController.java # Transaction controller
│   │               ├── model/         # Model classes
│   │               │   ├── Transaction.java          # Transaction model
│   │               │   ├── User.java                 # User model
│   │               │   ├── BudgetCalculator.java     # Budget calculator interface
│   │               │   ├── SimpleBudgetCalculator.java # Simple budget calculator implementation
│   │               │   ├── AlertService.java         # Alert service model
│   │               │   └── AIClassifier.java         # AI classifier model
│   │               ├── service/       # Service classes
│   │               │   ├── AIChatService.java        # AI chat service
│   │               │   └── TransactionService.java   # Transaction service
│   │               ├── view/          # View classes
│   │               │   ├── MainFrame.java            # Main window
│   │               │   ├── LoginPanel.java           # Login panel
│   │               │   ├── RegisterPanel.java        # Registration panel
│   │               │   ├── TransactionPanel.java     # Transaction panel
│   │               │   ├── CategoryManagementPanel.java # Category management panel
│   │               │   ├── StatisticsPanel.java      # Statistics panel
│   │               │   ├── DashboardPanel.java       # Dashboard panel
│   │               │   ├── ExpenseAlertPanel.java    # Expense alert panel
│   │               │   ├── LocalConsumptionPanel.java # Local consumption panel
│   │               │   ├── LocalizedFinancePanel.java # Localized finance panel
│   │               │   ├── AIChatPanel.java          # AI chat panel
│   │               │   └── components/               # UI components
│   │               │       └── ChatBubbleFactory.java # Chat bubble factory
│   │               └── util/          # Utility classes
│   │                   ├── CSVHandler.java           # CSV handling utility
│   │                   ├── PasswordUtils.java        # Password utility
│   │                   ├── UserDAO.java              # User data access object
│   │                   └── UIConstants.java          # UI constants
│   └── test/
│       └── java/
│           └── com/
│               └── financeapp/       # Test classes
│                   └── TransactionTest.java
├── data/                          # Data directory
│   ├── transactions.csv          # Transaction records
│   ├── users.csv                 # User information
│   ├── corrections.log           # Correction log
│   └── test_import.csv           # Test import file
├── lib/                          # Library dependencies
├── pom.xml                       # Maven project configuration
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
   javac -d bin -cp "src\main\java;lib\*" src\main\java\com\financeapp\view\*.java src\main\java\com\financeapp\model\*.java src\main\java\com\financeapp\controller\*.java src\main\java\com\financeapp\util\*.java src\main\java\com\financeapp\service\*.java
   ```

3. Run the application:
   ```
   java -cp "bin;data;lib\*" com.financeapp.view.MainFrame
   ```

## Usage Instructions

### User Authentication

1. Register a new account or log in with existing credentials
2. The system will automatically load your personal transaction data

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

### Using the AI Financial Advisor

1. Select the "AI Advisor" tab in the navigation menu
2. The system will automatically analyze your transaction data and provide initial financial advice
3. Type your financial questions in the input box or select from the recommended questions
4. The AI will respond to your questions in real-time and provide personalized financial advice

## Technical Implementation Details

### AI Chat Advisory System
- **AIChatService**: Handles communication with the AI API, provides streaming response support
- **AIChatPanel**: Provides a user-friendly chat interface, supports real-time display of AI responses
- **ChatBubbleFactory**: Generates chat bubble UI components, provides a unified chat interface style
- **Markdown Conversion**: Converts Markdown format in AI responses to easy-to-read plain text

### Data Persistence
- **CSV Storage**: Uses CSV files to store transaction records and user information
- **User Authentication**: Supports basic user registration and login functionality
- **Data Import/Export**: Supports importing transaction records from CSV files

### UI Design
- **Modern Interface**: Employs modern UI design for a pleasant user experience
- **Responsive Layout**: Adapts to different screen sizes and resolutions
- **Interactive Components**: Provides interactive charts and visualization components

## Development Roadmap

- Implement more complete lunar calendar conversion logic
- Add more category classification rules
- Support chart export functionality
- Implement budget setting functionality
- Add multi-currency support
- Create mobile application version
- Expand AI advisor capabilities to support more financial scenarios
- Add data backup and recovery features
- Implement financial goal setting and tracking

## Contribution Guidelines

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Submit a pull request

## License

This project is licensed under the MIT License.

## Development Team

Software Engineering Group 76 