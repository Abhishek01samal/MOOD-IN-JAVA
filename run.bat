@echo off
REM ============================================================
REM  MoodTune — Build and Run Script (Windows)
REM ============================================================
REM
REM  Prerequisites:
REM    • JDK 11+  (javac and java on PATH)
REM    • mysql-connector-java-X.X.X.jar in the lib\ folder
REM    • MySQL running with schema.sql imported
REM
REM  Usage:  double-click run.bat  or  run from Command Prompt
REM ============================================================

setlocal EnableDelayedExpansion

set PROJECT_DIR=%~dp0
set SRC_DIR=%PROJECT_DIR%src
set BIN_DIR=%PROJECT_DIR%bin
set LIB_DIR=%PROJECT_DIR%lib

REM ── Build Classpath ─────────────────────────────────────────────────────────
set "CP=%BIN_DIR%"
for %%F in ("%LIB_DIR%\*.jar") do (
    set "CP=!CP!;%%F"
)

REM ── Compile ───────────────────────────────────────────────────────────────────
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"
echo Compiling sources...

if exist "%TEMP%\sources.txt" del "%TEMP%\sources.txt"
powershell -Command "Get-ChildItem -Path '%SRC_DIR%' -Recurse -Filter *.java | ForEach-Object { '\"' + ($_.FullName -replace '\\', '/') + '\"' } | Out-File -FilePath '%TEMP%\sources.txt' -Encoding ascii"
javac -cp "!CP!" -d "%BIN_DIR%" @"%TEMP%\sources.txt"

if errorlevel 1 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)
echo Compilation complete.

REM ── Run ───────────────────────────────────────────────────────────────────────
echo Launching MoodTune...
java -cp "%CP%" ^
     -Dawt.useSystemAAFontSettings=on ^
     -Dswing.aatext=true ^
     com.moodtune.ui.MoodTuneApp

pause
