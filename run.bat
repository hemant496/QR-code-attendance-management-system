@echo off
title QR Code Attendance System
echo Starting QR Code Attendance System...
cd /d "%~dp0"
java -jar target\QRCodeAttendanceSystem-1.0-SNAPSHOT.jar
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with code %ERRORLEVEL%. Press any key to close...
    pause
)
