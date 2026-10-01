@echo off
echo ===================================================
echo   Starting SmartDine Platform Server...
echo ===================================================
set "JAVA_HOME=C:\Users\LEYON\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Using Java from: %JAVA_HOME%
java -version

echo.
echo Launching Spring Boot application on http://localhost:8080 ...
call .\gradlew.bat bootRun
pause
