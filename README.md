# Personal Finance Management Software

A comprehensive personal finance management system for recording and analyzing personal income and expenses, with integrated intelligent financial advisory services.

## Repository Structure

This repository contains two main parts:

1. **Software_Group76-Running part** - Executable version of the application
2. **Software_Group76-Remaining part** - Source code and development version

## Executable Version (Software_Group76-Running part)

This is the ready-to-run version of the Personal Finance Management System. This version provides a one-click application launch without the need to compile source code.

### System Requirements

- Windows 10 or higher
- Java 21 (LTS version) installed
- Screen resolution of at least 1280×720

### How to Run

1. Ensure Java 21 is installed on your system. If not, download and install it from the [Oracle website](https://www.oracle.com/java/technologies/downloads/#java21) or [Adoptium](https://adoptium.net/temurin/releases/?version=21).

2. Double-click the `run_finance_app.bat` file to start the application.

3. If you are a first-time user, use the registration feature to create a new account. You can also use the following test account:
   - Username: test
   - Password: 1234

### File Structure

- `app/` - Application folder
  - `finance-app-1.0-SNAPSHOT-jar-with-dependencies.jar` - Main application file
  - `data/` - Data folder
    - `transactions.csv` - Transaction records
    - `users.csv` - User information
  - `lib/` - Dependencies

- `run_finance_app.bat` - Windows launch script
- `Quick Start Guide.txt` - Brief guide for running the application
- `bin/` - Binary files
- `Windows/` - Windows-specific resources
- `data/` - Shared data directory

## Source Code Version (Software_Group76-Remaining part)

This is the development version containing all source code of the Personal Finance Management System.

### Features

- **Transaction Management**: Add, import, edit, and delete transaction records
- **Statistical Analysis**: View monthly expense statistics and budget recommendations
- **Festival Detection**: Automatically identify and tag consumption during Chinese New Year
- **Data Persistence**: Use simple CSV files to securely store data
- **User Authentication**: Support for multiple user accounts
- **AI Financial Advisor**: Integrated intelligent AI assistant providing personalized financial advice
- **Expense Alerts**: Monitor unusual spending patterns and provide alerts
- **Localized Financial Analysis**: Analyze consumption patterns based on geographic location
- **Budget Calculator**: Provide intelligent budget suggestions and analysis
- **Data Visualization**: Interactive charts and graphs to visualize spending patterns
- **Multi-user Support**: Secure user authentication and profile management

### System Requirements for Development

- Java 21 (LTS version)
- Swing GUI library (part of the Java standard library)
- JUnit 5 (for running tests)
- External dependencies: org.json (for JSON processing)
- Maven (for dependency management)

### Directory Structure

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
│                   ├── TransactionTest.java         # Transaction tests
│                   ├── UserTest.java                # User authentication tests
│                   └── BudgetCalculatorTest.java    # Budget calculator tests
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

### Development Quick Start

#### Using Batch File (Recommended)

1. Double-click the `run.bat` file in the project root directory, or execute in command line:
   ```
   .\run.bat
   ```
   This command will automatically compile all Java source files and start the application.

#### Manual Compilation and Execution

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
- **Data Validation**: Validates input data to ensure integrity and consistency

### UI Design
- **Modern Interface**: Employs modern UI design for a pleasant user experience
- **Responsive Layout**: Adapts to different screen sizes and resolutions
- **Interactive Components**: Provides interactive charts and visualization components
- **User-Friendly Controls**: Intuitive controls and navigation for ease of use

## Architecture

### MVC Pattern
The application follows the Model-View-Controller (MVC) architecture pattern:
- **Model**: Contains business logic and data structures (Transaction, User, etc.)
- **View**: Provides the user interface (panels, frames, and components)
- **Controller**: Handles user input and updates the model and view accordingly

### Service Layer
- Acts as an intermediary between controllers and models
- Provides higher-level operations and business logic
- Includes services like TransactionService and AIChatService
- Implements business rules and transaction logic

### Data Access Layer
- Manages data persistence and retrieval
- Implements data access objects (DAOs) like UserDAO
- Handles CSV file operations through CSVHandler
- Ensures data integrity and consistency

### Components and Modularity
- Each component is designed to be modular and reusable
- Clear separation of concerns between different layers
- Event-driven communication between components
- Loose coupling between modules for better maintainability

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
- Integrate with online banking services (future enhancement)
- Add cloud synchronization capabilities

## Contribution Guidelines

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Submit a pull request

## License

This project is licensed under the MIT License.

## Development Team

Software Engineering Group 76 - 2024/2025 