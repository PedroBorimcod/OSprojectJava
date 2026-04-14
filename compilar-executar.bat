@echo off
echo === TechSuite Pro - Compilador e Executor ===
echo.

REM Verificar se o SQLite JDBC existe
if not exist sqlite-jdbc-*.jar (
    echo Baixando driver SQLite JDBC...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.44.1.0/sqlite-jdbc-3.44.1.0.jar' -OutFile 'sqlite-jdbc-3.44.1.0.jar'"
    if errorlevel 1 (
        echo Erro ao baixar o driver SQLite!
        pause
        exit /b 1
    )
)

echo Compilando DatabaseManager.java...
javac -cp ".;sqlite-jdbc-*.jar" DatabaseManager.java
if errorlevel 1 (
    echo Erro na compilacao do DatabaseManager!
    pause
    exit /b 1
)

echo Compilando HackPanel.java...
javac -cp ".;sqlite-jdbc-*.jar" HackPanel.java
if errorlevel 1 (
    echo Erro na compilacao do HackPanel!
    pause
    exit /b 1
)

echo.
echo Compilacao concluida com sucesso!
echo Iniciando TechSuite Pro...
echo.

java -cp ".;sqlite-jdbc-*.jar" HackPanel

pause
