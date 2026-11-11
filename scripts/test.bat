@echo off
rem Builds the game, compiles the tests in test\, and runs them.
rem Exits with 1 if any test fails.
setlocal
cd /d "%~dp0.."
call scripts\jdk.bat || exit /b 1

call scripts\build.bat || exit /b 1
if exist out-test rmdir /s /q out-test
mkdir out-test
"%JAVAC%" --release 17 -Xlint:all -cp out -d out-test test\*.java
if errorlevel 1 exit /b 1
rem Headless: the tests never open a window, so they also run without a display.
"%JAVA%" -Djava.awt.headless=true -cp "out;out-test" TestRunner
