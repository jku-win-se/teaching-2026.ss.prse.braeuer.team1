# Performance verification — NFR-01

**Generated:** 2026-05-22 09:29:09 UTC
**Host:** Darwin-arm64
**Iterations per endpoint:** 30
**Device count during test:** 10
**Limit (NFR-01):** 2.0 seconds for p95

All values in **milliseconds**.

| Endpoint                         |  min |  avg |  p95 |  max | ≤2s |
|----------------------------------|------|------|------|------|------|
| POST /api/users/login            |   80 |   82 |   84 |   91 | PASS |
| GET  /api/rooms?userId=1         |    4 |    5 |    5 |    6 | PASS |
| GET  /api/rooms/1/devices        |    4 |    5 |    6 |    6 | PASS |
| GET  /api/rules?userId=1         |    4 |    5 |    6 |    6 | PASS |
| GET  /api/schedules?userId=1     |    4 |    5 |    5 |    5 | PASS |
| GET  /api/scenes?userId=1        |    5 |    5 |    6 |    7 | PASS |
| GET  /api/energy/dashboard?userId=1 |    5 |    7 |    8 |    8 | PASS |
| GET  /api/activity-logs?userId=1 |    4 |    4 |    5 |    7 | PASS |
| GET  /api/notifications?userId=1 |    3 |    4 |    4 |    5 | PASS |
| PUT  /api/devices/1/state        |    3 |    3 |    3 |   12 | PASS |

**NFR-01 status:** 0 endpoint(s) exceeded the 2.0 s threshold (p95).

## How to reproduce

```bash
docker compose up -d
./docs/performance/run-perf-test.sh
```

The script logs in as the seed Owner, ensures the database holds at least
10 devices (creating temporary ones if necessary), measures
each user-facing endpoint 30 times, and removes the temporary
devices at the end.

Configurable via environment variables: `BASE`, `ITERATIONS`,
`TARGET_DEVICE_COUNT`, `LIMIT_SECONDS`.
