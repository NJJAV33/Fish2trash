@echo off
rem Builds the game, then starts it.
setlocal
cd /d "%~dp0.."
call scripts\jdk.bat || exit /b 1

call scripts\build.bat || exit /b 1
"%JAVA%" -cp out Main
