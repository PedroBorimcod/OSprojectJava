@echo off
echo ========================================
echo  TECHSUITE PRO - BUILD FINAL
echo ========================================
echo.

echo Limpando builds anteriores...
del /Q *.class 2>nul

echo Compilando com SQLite JDBC...
javac -cp ".;sqlite-jdbc-3.44.1.0.jar" DatabaseManager.java HackPanel.java

if errorlevel 1 (
    echo.
    echo ERRO na compilacao!
    pause
    exit /b 1
)

echo.
echo Extraindo SQLite JDBC...
if exist temp_build rmdir /s /q temp_build
mkdir temp_build
cd temp_build

jar xf ..\sqlite-jdbc-3.44.1.0.jar
copy ..\HackPanel*.class . >nul
copy ..\DatabaseManager*.class . >nul

echo.
echo Criando JAR final...
jar cfm ..\TechSuitePro_v2.jar ..\MANIFEST.MF *.class org sqlite

cd ..
rmdir /s /q temp_build

echo.
echo ========================================
echo  BUILD CONCLUiDO COM SUCESSO!
echo ========================================
echo.
echo Arquivo criado: TechSuitePro_v2.jar
echo.
echo Para executar:
echo   java -jar TechSuitePro_v2.jar
echo.
echo Ou use o script: compilar-executar.bat
echo.
pause
