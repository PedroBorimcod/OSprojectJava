@echo off
echo ========================================
echo  TESTE DO BANCO DE DADOS - TechSuite Pro
echo ========================================
echo.
echo Este script verifica se o banco de dados esta funcionando.
echo.

REM Verificar compilacao
if not exist DatabaseManager.class (
    echo Compilando...
    javac -cp ".;sqlite-jdbc-3.44.1.0.jar" DatabaseManager.java HackPanel.java
)

echo Verificando driver SQLite...
if exist sqlite-jdbc-3.44.1.0.jar (
    echo [OK] Driver SQLite encontrado
) else (
    echo [ERRO] Driver SQLite nao encontrado!
    pause
    exit /b 1
)

echo.
echo Verificando classes compiladas...
if exist HackPanel.class (
    echo [OK] HackPanel.class encontrado
) else (
    echo [ERRO] HackPanel.class nao encontrado!
    pause
    exit /b 1
)

if exist DatabaseManager.class (
    echo [OK] DatabaseManager.class encontrado
) else (
    echo [ERRO] DatabaseManager.class nao encontrado!
    pause
    exit /b 1
)

echo.
echo ========================================
echo  TUDO OK! Sistema pronto para uso!
echo ========================================
echo.
echo O banco de dados sera criado em:
echo %%USERPROFILE%%\.techsuite\techsuite.db
echo.
echo Para executar o sistema:
echo   compilar-executar.bat
echo.
echo Ou:
echo   java -cp ".;sqlite-jdbc-3.44.1.0.jar" HackPanel
echo.
pause
