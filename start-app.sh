#!/usr/bin/env bash
set -euo pipefail

echo "============================================"
echo " SmartHome – Starting via Docker Compose"
echo "============================================"
echo

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "[ERROR] Docker is not running or not installed."
    echo "        Please start Docker Desktop and try again."
    exit 1
fi

echo "[INFO] Building and starting all services (detached) ..."
echo "       This may take a few minutes on the first run."
echo

docker compose up --build -d

# Wait until the frontend is reachable, then open the browser
echo "[INFO] Waiting for frontend to become ready on http://localhost:3000 ..."
RETRIES=30
until curl -s -o /dev/null -w "%{http_code}" http://localhost:3000 | grep -qE '^[23]'; do
    RETRIES=$((RETRIES - 1))
    if [ "$RETRIES" -le 0 ]; then
        echo "[WARN] Frontend did not respond in time – opening browser anyway ..."
        break
    fi
    sleep 2
done

echo "[INFO] Frontend is ready – opening browser ..."
# macOS uses 'open', Linux uses 'xdg-open'
if command -v open > /dev/null 2>&1; then
    open "http://localhost:3000"
elif command -v xdg-open > /dev/null 2>&1; then
    xdg-open "http://localhost:3000"
else
    echo "[WARN] Could not detect a browser opener. Visit http://localhost:3000 manually."
fi

echo
echo "[INFO] Streaming logs (Ctrl+C to stop) ..."
echo
docker compose logs -f

echo
echo "[INFO] All services are still running in the background."
echo "       To stop them, run:  docker compose down"
