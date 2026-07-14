@echo off
setlocal
cd /d "%~dp0"

if not exist "bin\bookmanagement\Main.class" (
  echo Classes not found. Running compile.bat ...
  call compile.bat
  if errorlevel 1 exit /b 1
)

rem java.library.path=lib  → cần mssql-jdbc_auth-*.x64.dll cho Windows Authentication
java -Djava.library.path=lib -cp "bin;lib\*;resources" bookmanagement.Main
endlocal
