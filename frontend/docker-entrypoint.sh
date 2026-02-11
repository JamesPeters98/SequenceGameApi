#!/bin/sh
set -eu

mkdir -p /app/dist

cat > /app/dist/runtime-env.js <<EOF
window.__RUNTIME_CONFIG__ = {
  VITE_API_URL: ${VITE_API_URL:+\"$VITE_API_URL\"},
  VITE_API_BEARER_TOKEN: ${VITE_API_BEARER_TOKEN:+\"$VITE_API_BEARER_TOKEN\"}
};
EOF

exec bun run preview --host 0.0.0.0 --port 4173
