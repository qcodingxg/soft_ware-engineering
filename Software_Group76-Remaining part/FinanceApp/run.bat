@echo off
echo 运行个人财务管理软件...
if not exist target\finance-app-1.0-SNAPSHOT-jar-with-dependencies.jar (
    echo 正在构建应用程序...
    call mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo 构建失败!
        pause
        exit /b 1
    )
)
echo 正在启动应用程序...
java -jar target\finance-app-1.0-SNAPSHOT-jar-with-dependencies.jar
pause