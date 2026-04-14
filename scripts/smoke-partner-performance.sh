#!/usr/bin/env bash
set -euo pipefail

BASE_GATEWAY="${BASE_GATEWAY:-http://localhost:8082}"

echo "== checking gateway =="
curl -fsS "${BASE_GATEWAY}/actuator/health" >/dev/null
echo "gateway ok"

echo "== checking partner-performance routes =="
curl -fsS "${BASE_GATEWAY}/api/partner-performance/v1/leaderboard?period=30d&metric=redemptionRate&limit=10" >/dev/null
echo "leaderboard ok"

curl -fsS "${BASE_GATEWAY}/api/partner-performance/v1/alerts?open=true" >/dev/null
echo "alerts ok"

echo "All smoke tests passed."
