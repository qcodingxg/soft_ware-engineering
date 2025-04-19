# Personal Finance Management Software - Executable Version

This is the executable version of the Personal Finance Management System by Software Engineering Group 76. This version provides a one-click application launch without the need to compile source code.

## System Requirements

- Windows 10 or higher
- Java 21 (LTS version) installed
- Screen resolution of at least 1280×720

## How to Run

1. Ensure Java 21 is installed on your system. If not, download and install it from the [Oracle website](https://www.oracle.com/java/technologies/downloads/#java21) or [Adoptium](https://adoptium.net/temurin/releases/?version=21).

2. Double-click the `run_finance_app.bat` file to start the application.

3. If you are a first-time user, use the registration feature to create a new account. You can also use the following test account:
   - Username: test
   - Password: 1234

## File Structure

- `app/` - Application folder
  - `finance-app-1.0-SNAPSHOT-jar-with-dependencies.jar` - Main application file
  - `data/` - Data folder
    - `transactions.csv` - Transaction records
    - `users.csv` - User information
  - `lib/` - Dependencies

- `run_finance_app.bat` - Windows launch script

## FAQ

1. **Application won't start?**
   - Ensure Java 21 is properly installed
   - Run command prompt and type `java -version` to check your Java version

2. **Where are my data files?**
   - All data files are stored in the `app/data/` folder

3. **How do I back up my data?**
   - Copy the files in the `app/data/` folder to a secure location

4. **How to reset password?**
   - The current version does not support password reset, please make sure to remember your password

## Support and Feedback

For any questions or feedback, please contact Software Engineering Group 76. 