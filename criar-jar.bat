@echo off
echo === TechSuite Pro - Instalador ===
echo.
echo Criando arquivo JAR executavel...
jar cfm TechSuitePro.jar MANIFEST.MF *.class
echo.
echo TechSuitePro.jar criado com sucesso!
echo Para executar: java -jar TechSuitePro.jar
echo.
pause
