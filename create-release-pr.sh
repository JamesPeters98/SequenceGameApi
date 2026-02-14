#!/usr/bin/env bash
set -euo pipefail

gh pr create \
  --base main \
  --head staging \
  --title "Release: $(date +%Y-%m-%d)" \
  --body "Promote staging to production"
