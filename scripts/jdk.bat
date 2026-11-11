@echo off
rem Finds a JDK 17 or newer and sets JAVAC and JAVA.
rem Not run directly: build.bat, run.bat and test.bat load it with "call scripts\jdk.bat".

set "JAVAC="
set "JAVA="

rem Prefer JAVA_HOME if it points at a JDK that can compile for Java 17.
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" (
    "%JAVA_HOME%\bin\javac.exe" --release 17 -version >nul 2>&1 && (
        set "JAVAC=%JAVA_HOME%\bin\javac.exe"
        set "JAVA=%JAVA_HOME%\bin\java.exe"
    )
)
if defined JAVAC exit /b 0

rem Fall back to whatever JDK is on the PATH.
javac --release 17 -version >nul 2>&1 && (
    set "JAVAC=javac"
    set "JAVA=java"
)
if defined JAVAC exit /b 0

echo Need JDK 17+. Install one, or point JAVA_HOME at it. 1>&2
exit /b 1
