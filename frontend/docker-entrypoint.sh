#!/bin/sh
set -eu

mkdir -p /app/dist

bun -e '
  const fs = require("node:fs");
  const config = {};
  if (process.env.VITE_API_URL?.trim()) {
    config.VITE_API_URL = process.env.VITE_API_URL.trim();
  }
  if (process.env.VITE_API_BEARER_TOKEN?.trim()) {
    config.VITE_API_BEARER_TOKEN = process.env.VITE_API_BEARER_TOKEN.trim();
  }
  fs.writeFileSync("/app/dist/runtime-env.js", `window.__RUNTIME_CONFIG__ = ${JSON.stringify(config)};\n`);
'

exec bun run preview --host 0.0.0.0 --port 4173
