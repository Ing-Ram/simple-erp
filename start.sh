#!/usr/bin/env bash
#
# Starts SimpleERP for local development: the Spring Boot backend (H2 + demo seed) on :8080
# and the Vite frontend on :5173. Ctrl-C stops both.
#
# Kept compatible with the Bash 3.2 that ships on macOS (no `wait -n`, no associative arrays).
#
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND="$ROOT/backend"
FRONTEND="$ROOT/frontend"
BACKEND_PORT=8080
FRONTEND_PORT=5173

backend_pid=""
frontend_pid=""

# Kill whatever is listening on a TCP port (e.g. a backend orphaned by a previous run).
free_port() {
  local port="$1"
  local pids
  pids="$(lsof -ti "tcp:$port" 2>/dev/null || true)"
  if [ -n "$pids" ]; then
    echo "Port $port is in use (PID(s): $pids) — freeing it."
    # shellcheck disable=SC2086
    kill $pids 2>/dev/null || true
    sleep 1
  fi
}

cleanup() {
  trap - EXIT INT TERM
  echo ""
  echo "Shutting down…"
  [ -n "$frontend_pid" ] && kill "$frontend_pid" 2>/dev/null || true
  [ -n "$backend_pid" ]  && kill "$backend_pid"  2>/dev/null || true
  # Sweep the ports too, in case a child process (forked JVM, vite) outlived its parent.
  free_port "$BACKEND_PORT"
  free_port "$FRONTEND_PORT"
  wait 2>/dev/null || true
}
trap cleanup EXIT INT TERM

# --- Preflight: claim the dev ports ----------------------------------------
free_port "$BACKEND_PORT"
free_port "$FRONTEND_PORT"

# --- Backend ----------------------------------------------------------------
# fork=false keeps the app in Maven's own JVM, so this PID is the real server and
# killing it actually stops the backend (the default forked JVM would be orphaned).
echo "Starting backend on http://localhost:$BACKEND_PORT …"
mvn -q -f "$BACKEND/pom.xml" -Dspring-boot.run.fork=false spring-boot:run &
backend_pid=$!

# Wait until the API answers before bringing up the UI.
printf "Waiting for backend"
for _ in $(seq 1 60); do
  if curl -sf "http://localhost:$BACKEND_PORT/api/v1/finance/customers" >/dev/null 2>&1; then
    echo " — up."
    break
  fi
  printf "."
  sleep 1
done

# --- Frontend ---------------------------------------------------------------
if [ ! -d "$FRONTEND/node_modules" ]; then
  echo "Installing frontend dependencies…"
  npm --prefix "$FRONTEND" install
fi

echo "Starting frontend on http://localhost:$FRONTEND_PORT …"
npm --prefix "$FRONTEND" run dev &
frontend_pid=$!

echo ""
echo "SimpleERP is running:"
echo "  • Frontend  http://localhost:$FRONTEND_PORT"
echo "  • Backend   http://localhost:$BACKEND_PORT/api/v1/finance/dashboard"
echo "Press Ctrl-C to stop both."

# Stay alive until either process exits (portable substitute for `wait -n`),
# then the EXIT trap tears the other one down.
while kill -0 "$backend_pid" 2>/dev/null && kill -0 "$frontend_pid" 2>/dev/null; do
  sleep 1
done
