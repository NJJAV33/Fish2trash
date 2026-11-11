@echo off
rem Compiles the game into out\. Works from any folder.
setlocal enabledelayedexpansion
cd /d "%~dp0.."
call scripts\jdk.bat || exit /b 1

if exist out rmdir /s /q out
mkdir out

rem List every source file, with forward slashes so javac reads the paths correctly.
(for /r src %%f in (*.java) do (
    set "file=%%f"
    echo "!file:\=/!"
)) > out\sources.txt

rem --release 17 keeps the code compatible with Java 17 even on a newer JDK.
"%JAVAC%" --release 17 -Xlint:all -d out @out\sources.txt
if errorlevel 1 exit /b 1
del out\sources.txt
