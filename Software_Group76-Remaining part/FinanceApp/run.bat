@echo off
echo 编译并运行个人财务管理软件...
if not exist bin mkdir bin
javac -d bin -cp "src\main\java;lib\json-20230227.jar" src\main\java\com\financeapp\view\MainFrame.java
if %errorlevel% neq 0 (
    echo 编译失败!
    pause
    exit /b 1
)
echo 编译成功!
echo 正在启动应用程序...
java -cp "bin;lib\json-20230227.jar" com.financeapp.view.MainFrame
pause 