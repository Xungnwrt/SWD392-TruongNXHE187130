@echo off
setlocal
cd /d "%~dp0"

if not exist "bin" mkdir bin
if not exist "lib\*.jar" (
  echo [ERROR] Missing JDBC driver in lib\
  echo Download mssql-jdbc and put the jar into lib\
  echo Example: lib\mssql-jdbc-12.8.1.jre11.jar
  exit /b 1
)

dir /b lib\*.jar > nul 2>&1
if errorlevel 1 (
  echo [ERROR] No jar found in lib\
  exit /b 1
)

set CP=lib\*
javac -encoding UTF-8 -cp "%CP%" -d bin ^
  src\bookmanagement\model\*.java ^
  src\bookmanagement\util\*.java ^
  src\bookmanagement\dao\*.java ^
  src\bookmanagement\service\*.java ^
  src\bookmanagement\controller\*.java ^
  src\bookmanagement\presentation\*.java ^
  src\bookmanagement\Main.java

if errorlevel 1 (
  echo Compile FAILED
  exit /b 1
)

echo Compile OK. Run with run.bat
endlocal
