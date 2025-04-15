@echo off
echo 正在编译项目...
mkdir bin 2>nul
javac -d bin -cp src\main\java src\main\java\com\financeapp\view\*.java src\main\java\com\financeapp\model\*.java src\main\java\com\financeapp\controller\*.java src\main\java\com\financeapp\util\*.java
if %errorlevel% neq 0 (
    echo 编译失败！
    pause
    exit /b %errorlevel%
)
echo 编译完成，正在启动应用程序...
java -cp "bin;data" com.financeapp.view.MainFrame
pause 