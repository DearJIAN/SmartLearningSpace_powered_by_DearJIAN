@echo off
chcp 65001 >nul
title SmartLearningSpace - Launcher

echo ==========================================
echo   SmartLearningSpace - One-Click Launcher
echo ==========================================
echo.

set "PROJECT_ROOT=%~dp0"
set "VUE_DIR=%PROJECT_ROOT%vue-demo"
set "SPRING_DIR=%PROJECT_ROOT%smart-campus-backend"
set "FLASK_DIR=%PROJECT_ROOT%my_yolo_web"

:: Check if conda is available
where conda >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Conda not found in PATH!
    echo.
    echo Please ensure Anaconda or Miniconda is installed
    echo and added to your system PATH.
    echo.
    pause
    exit /b 1
)

:: Prompt for conda environment name
echo Please enter your conda environment name for this project.
echo Example: newyolo
echo.
set /p CONDA_ENV="Conda environment name: "

if "%CONDA_ENV%"=="" (
    echo [ERROR] Environment name cannot be empty!
    pause
    exit /b 1
)

echo.
echo [1/4] Activating conda environment: %CONDA_ENV% ...

:: Get conda base path and activate
for /f "tokens=*" %%a in ('conda info --base') do set "CONDA_BASE=%%a"

if not exist "%CONDA_BASE%\Scripts\activate.bat" (
    echo [ERROR] Cannot find conda activate script!
    echo   Tried: %CONDA_BASE%\Scripts\activate.bat
    pause
    exit /b 1
)

call "%CONDA_BASE%\Scripts\activate.bat" %CONDA_ENV%
if errorlevel 1 (
    echo [ERROR] Failed to activate environment: %CONDA_ENV%
    echo.
    echo Please check:
    echo   1. The environment name is correct
    echo   2. The environment exists: conda env list
    echo   3. Conda is properly installed
    echo.
    pause
    exit /b 1
)

:: Verify python is available after activation
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python not found after activating %CONDA_ENV%!
    pause
    exit /b 1
)

for /f "tokens=*" %%a in ('python --version 2^>^&1') do (
    echo   Activated: %%a
)

echo.
echo [2/4] Checking MySQL (port 3306)...
echo   Make sure MySQL is running before proceeding.
echo.

echo [3/4] Installing frontend dependencies...
cd /d "%VUE_DIR%"
call npm install
if errorlevel 1 (
    echo [ERROR] npm install failed!
    pause
    exit /b 1
)

echo.
echo ==========================================
echo   Three services will start in separate windows
echo   Order: Flask -^> Spring Boot -^> Vue
echo   Close each window to stop that service
echo ==========================================
echo.
pause

echo [Flask] Starting AI Vision + Digital Human (port 5000)...
start "LEAR-CODE Flask" cmd /k "cd /d "%FLASK_DIR%" && python -u app.py"

timeout /t 3 /nobreak >nul

echo [Spring Boot] Starting Core Backend (port 8080)...
echo   Note: Spring Boot takes 30-60s to start, please wait...
start "LEAR-CODE Spring Boot" cmd /k "cd /d "%SPRING_DIR%" && mvn spring-boot:run"

timeout /t 10 /nobreak >nul

echo [Vue] Starting Frontend (port 5173)...
start "LEAR-CODE Vue Frontend" cmd /k "cd /d "%VUE_DIR%" && npm run dev"

echo.
echo ==========================================
echo   Startup complete!
echo.
echo   Vue Frontend:  http://localhost:5173
echo   Spring Boot:   http://localhost:8080
echo   Flask AI:      http://localhost:5000
echo.
echo   Browse to: http://localhost:5173
echo ==========================================
echo.
echo Press any key to close this window (services keep running)...
pause >nul
