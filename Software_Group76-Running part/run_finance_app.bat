@echo off
title Personal Finance Management System - Software Engineering Group 76
echo ================================================
echo          Personal Finance Management System - Launcher
echo           Software Engineering Group 76
echo ================================================
echo.
echo Checking Java environment...
echo.

:: Check if Java is installed
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java environment not detected, please install Java 21!
    echo You can download Java from the following websites:
    echo - Oracle: https://www.oracle.com/java/technologies/downloads/#java21
    echo - Adoptium: https://adoptium.net/temurin/releases/?version=21
    echo.
    echo After installation, please run this script again.
    echo.
    pause
    exit /b 1
)

echo [SUCCESS] Java environment detected
echo.
echo Launching Personal Finance Management System...
echo.

:: Enter application directory
cd "%~dp0app"

:: Run application
java -jar finance-app-1.0-SNAPSHOT-jar-with-dependencies.jar

:: If application exits, display information
echo.
if %ERRORLEVEL% EQ 0 (
    echo [INFO] Application has closed normally.
) else (
    echo [ERROR] Application exited abnormally, error code: %ERRORLEVEL%
    echo If the problem persists, please contact Software Engineering Group 76 for support.
)

echo.
pause 