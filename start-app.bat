@echo off
title SmartHome – Docker Launcher

echo ============================================
echo  SmartHome – Starting via Docker Compose
echo ============================================
echo.

:: Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not running or not installed.
    echo         Please start Docker Desktop and try again.
    echo.
    pause
    exit /b 1
)

echo [INFO] Building and starting all services (detached) ...
echo        This may take a few minutes on the first run.
echo.

docker compose up --build -d

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] docker compose exited with an error.
    pause
    exit /b %errorlevel%
)

:: Wait until the frontend is reachable, then open the browser
echo [INFO] Waiting for frontend to become ready on http://localhost:3000 ...
set RETRIES=30
:wait_loop
curl -s -o nul -w "%%{http_code}" http://localhost:3000 | findstr /r "^[23]" >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] Frontend is ready – opening browser ...
    start "" "http://localhost:3000"
    goto show_logs
)
set /a RETRIES-=1
if %RETRIES% leq 0 (
    echo [WARN] Frontend did not respond in time – opening browser anyway ...
    start "" "http://localhost:3000"
    goto show_logs
)
timeout /t 2 /nobreak >nul
goto wait_loop

:show_logs
echo.
echo [INFO] Streaming logs (Ctrl+C to stop) ...
echo.
docker compose logs -f

echo.
echo [INFO] All services are still running in the background.
echo        To stop them, run:  docker compose down
pause
