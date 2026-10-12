# FitFuel FastAPI Backend — 195B Weeks 3–6

The backend receives the selected day's nutrition context and the Android app's persisted meal-tag preference profile. It filters the candidate catalog, asks OpenAI to rank exactly three existing meals, validates the returned IDs, and returns catalog-controlled calories/protein. If OpenAI is unavailable, the same preference-aware deterministic ranking runs on the backend.

## Setup

```bash
cd backend
python -m venv .venv
```

Windows PowerShell:

```powershell
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
Copy-Item .env.example .env
```

macOS/Linux:

```bash
source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
```

Add the server-side API key to `.env`:

```text
OPENAI_API_KEY=your_key_here
OPENAI_MODEL=gpt-5.6
OPENAI_ENABLED=true
```

## Run

```bash
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Verify:

```text
http://127.0.0.1:8000/health
http://127.0.0.1:8000/docs
```

The Android emulator reaches this server through `http://10.0.2.2:8000/`.

## Personalization behavior

- Android sends `tagPreferences` containing affinity and feedback counts.
- Strongly disliked tags are filtered when at least three alternatives remain.
- Meals over the remaining calorie budget are filtered when at least three meals fit the budget.
- Nutrition/fitness-goal fit remains the primary ranking signal.
- Learned positive/negative tags adjust ranking after nutritional fit.
- OpenAI sees the same filtered candidate list and preference context as the deterministic fallback.

## Tests

```bash
pytest
```

Tests disable OpenAI calls and verify health, sample recommendations, goal-aware fallback ranking, positive tag personalization, and strong-dislike filtering.

## 195B Weeks 7-8 — v0.5.0 (2026-08-14)

- Recommendation service instances are reused for identical backend settings so the OpenAI HTTP client is not recreated for every request.
- API coverage now includes candidate-count validation, negative nutrition validation, exact/distinct catalog response contracts, and service-cache behavior.
- The backend test suite contains 9 tests and runs without an OpenAI key or billable request.

```bash
pytest
```
