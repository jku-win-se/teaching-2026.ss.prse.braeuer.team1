#!/usr/bin/env bash
#
# Performance verification for NFR-01:
#   "Response time < 2 seconds under normal load (up to 10 devices)"
#
# Measures response time for every user-facing API endpoint, executes each
# request N times, and reports min / avg / p95 / max in milliseconds.
#
# Requires:
#   - Docker stack running (docker compose up)
#   - Seed user alice@example.com / password123 created by StartupDataLoader
#   - curl, awk, sort, bc

set -euo pipefail

BASE="${BASE:-http://localhost:8080}"
USER_EMAIL="${USER_EMAIL:-alice@example.com}"
USER_PASSWORD="${USER_PASSWORD:-password123}"
ITERATIONS="${ITERATIONS:-30}"
TARGET_DEVICE_COUNT="${TARGET_DEVICE_COUNT:-10}"
LIMIT_SECONDS="${LIMIT_SECONDS:-2.0}"
OUT_FILE="${1:-$(dirname "$0")/results.md}"

# --- helpers ---------------------------------------------------------------

stat_line() {
  # Reads whitespace-separated milliseconds from stdin, prints
  #   "min avg p95 max"
  sort -n | awk '
    { a[NR]=$1 }
    END {
      n = NR
      min = a[1]; max = a[n]; sum = 0
      for (i = 1; i <= n; i++) sum += a[i]
      avg = sum / n
      p95i = int(n * 0.95); if (p95i < 1) p95i = 1
      printf "%.0f %.0f %.0f %.0f\n", min, avg, a[p95i], max
    }
  '
}

measure() {
  # measure <label> <method> <url> [data-file]
  local label="$1" method="$2" url="$3" data="${4:-}"
  local samples=()
  for i in $(seq 1 "$ITERATIONS"); do
    local t
    if [[ -n "$data" ]]; then
      t=$(curl -s -o /dev/null -w '%{time_total}' \
        -X "$method" -H 'Content-Type: application/json' \
        --data "$data" "$BASE$url")
    else
      t=$(curl -s -o /dev/null -w '%{time_total}' \
        -X "$method" "$BASE$url")
    fi
    local ms
    ms=$(awk -v t="$t" 'BEGIN { printf "%.0f", t * 1000 }')
    samples+=("$ms")
  done
  local stats
  stats=$(printf '%s\n' "${samples[@]}" | stat_line)
  local min avg p95 max
  read -r min avg p95 max <<<"$stats"
  local verdict="PASS"
  local p95_sec
  p95_sec=$(awk -v p="$p95" 'BEGIN { printf "%.3f", p / 1000 }')
  if awk -v p="$p95_sec" -v l="$LIMIT_SECONDS" 'BEGIN { exit !(p > l) }'; then
    verdict="FAIL"
  fi
  printf '| %-32s | %4d | %4d | %4d | %4d | %-4s |\n' \
    "$label" "$min" "$avg" "$p95" "$max" "$verdict"
}

# --- setup: ensure 10 devices ---------------------------------------------

echo "Login..." >&2
curl -sf -X POST "$BASE/api/users/login" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"$USER_EMAIL\",\"password\":\"$USER_PASSWORD\"}" >/dev/null

ROOMS=$(curl -sf "$BASE/api/rooms?userId=1")
ROOM_ID=$(echo "$ROOMS" | awk -F'"id":' '{print $2}' | awk -F',' '{print $1}')

# count current devices via room iteration
existing=$(curl -sf "$BASE/api/rooms/$ROOM_ID/devices" | grep -oE '"id":[0-9]+' | wc -l | tr -d ' ')
echo "Existing devices in room $ROOM_ID: $existing" >&2

added_ids=()
while [ "$existing" -lt "$TARGET_DEVICE_COUNT" ]; do
  resp=$(curl -sf -X POST "$BASE/api/rooms/$ROOM_ID/devices" \
    -H 'Content-Type: application/json' \
    -d "{\"name\":\"PerfTestDevice-$existing\",\"type\":\"SWITCH\",\"powerConsumptionWatt\":10}") \
    || { echo "ERROR: device creation failed" >&2; exit 1; }
  id=$(echo "$resp" | grep -oE '"id":[0-9]+' | head -1 | grep -oE '[0-9]+')
  added_ids+=("$id")
  existing=$((existing + 1))
done
echo "Devices after setup: $existing" >&2

cleanup() {
  echo "Cleanup: removing ${#added_ids[@]} test devices..." >&2
  for id in "${added_ids[@]}"; do
    curl -sf -X DELETE "$BASE/api/devices/$id" -o /dev/null || true
  done
}
trap cleanup EXIT

# --- run measurements ------------------------------------------------------

DEVICE_ID=$(curl -sf "$BASE/api/rooms/$ROOM_ID/devices" | grep -oE '"id":[0-9]+' | head -1 | grep -oE '[0-9]+')

results_table=$(
  printf '| %-32s | %4s | %4s | %4s | %4s | %-4s |\n' \
    "Endpoint" "min" "avg" "p95" "max" "≤2s"
  printf '|%s|%s|%s|%s|%s|%s|\n' \
    "$(printf -- '-%.0s' {1..34})" \
    "$(printf -- '-%.0s' {1..6})" \
    "$(printf -- '-%.0s' {1..6})" \
    "$(printf -- '-%.0s' {1..6})" \
    "$(printf -- '-%.0s' {1..6})" \
    "$(printf -- '-%.0s' {1..6})"
  echo "Measuring..." >&2
  measure 'POST /api/users/login'              POST "/api/users/login"             "{\"email\":\"$USER_EMAIL\",\"password\":\"$USER_PASSWORD\"}"
  measure 'GET  /api/rooms?userId=1'           GET  "/api/rooms?userId=1"
  measure "GET  /api/rooms/$ROOM_ID/devices"   GET  "/api/rooms/$ROOM_ID/devices"
  measure 'GET  /api/rules?userId=1'           GET  "/api/rules?userId=1"
  measure 'GET  /api/schedules?userId=1'       GET  "/api/schedules?userId=1"
  measure 'GET  /api/scenes?userId=1'          GET  "/api/scenes?userId=1"
  measure 'GET  /api/energy/dashboard?userId=1' GET "/api/energy/dashboard?userId=1"
  measure 'GET  /api/activity-logs?userId=1'   GET  "/api/activity-logs?userId=1"
  measure 'GET  /api/notifications?userId=1'   GET  "/api/notifications?userId=1"
  measure "PUT  /api/devices/$DEVICE_ID/state" PUT  "/api/devices/$DEVICE_ID/state" '{"switchedOn":true}'
)

# --- write report ----------------------------------------------------------

ts=$(date -u '+%Y-%m-%d %H:%M:%S UTC')
host=$(uname -s)-$(uname -m)

cat >"$OUT_FILE" <<EOF
# Performance verification — NFR-01

**Generated:** $ts
**Host:** $host
**Iterations per endpoint:** $ITERATIONS
**Device count during test:** $existing
**Limit (NFR-01):** $LIMIT_SECONDS seconds for p95

All values in **milliseconds**.

$results_table

**NFR-01 status:** $(echo "$results_table" | grep -c FAIL) endpoint(s) exceeded the $LIMIT_SECONDS s threshold (p95).

## How to reproduce

\`\`\`bash
docker compose up -d
./docs/performance/run-perf-test.sh
\`\`\`

The script logs in as the seed Owner, ensures the database holds at least
$TARGET_DEVICE_COUNT devices (creating temporary ones if necessary), measures
each user-facing endpoint $ITERATIONS times, and removes the temporary
devices at the end.

Configurable via environment variables: \`BASE\`, \`ITERATIONS\`,
\`TARGET_DEVICE_COUNT\`, \`LIMIT_SECONDS\`.
EOF

echo "Report written to: $OUT_FILE" >&2
cat "$OUT_FILE"
