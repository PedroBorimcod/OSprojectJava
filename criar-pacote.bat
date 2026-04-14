@echo off
echo === TechSuite Pro - Criador de Pacote ===
echo.

REM Verificar se o SQLite JDBC existe
if not exist sqlite-jdbc-*.jar (
    echo Baixando driver SQLite JDBC...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.44.1.0/sqlite-jdbc-3.44.1.0.jar' -OutFile 'sqlite-jdbc-3.44.1.0.jar'"
)

echo Compilando todos os arquivos...
javac -cp ".;sqlite-jdbc-*.jar" *.java
if errorlevel 1 (
    echo Erro na compilacao!
    pause
    exit /b 1
)

echo.
echo Extraindo SQLite JDBC para incluir no JAR...
if exist temp_jar rmdir /s /q temp_jar
mkdir temp_jar
cd temp_jar

REM Extrair o conteudo do SQLite JDBC
jar xf ..\sqlite-jdbc-*.jar

REM Copiar as classes do TechSuite
copy ..\*.class .

REM Criar o JAR unico com tudo incluido
jar cfm ..\TechSuitePro.jar ..\MANIFEST.MF *.class org sqlite

cd ..
rmdir /s /q temp_jar

echo.
echo ========================================
echo TechSuitePro.jar criado com sucesso!
echo ========================================
echo.
echo Para executar: java -jar TechSuitePro.jar
echo.
echo Ou use o script compilar-executar.bat para desenvolvimento
echo.
pause
