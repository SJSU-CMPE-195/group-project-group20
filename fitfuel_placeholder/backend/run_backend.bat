@echo off
REM 195B Weeks 3-4: Windows helper starts the local FastAPI development server.
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
