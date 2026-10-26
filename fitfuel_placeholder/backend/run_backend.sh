#!/usr/bin/env bash
# 195B Weeks 3-4: macOS/Linux helper starts the local FastAPI development server.
set -euo pipefail
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
