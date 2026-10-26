# Kit
- AGP 8.9.2
- Kotlin 2.1.20
- Jetpack Compose
- Gradle wrapper 8.11.1
- Alpha (Android app + local FastAPI backend)

# Usage
- Import, exclude folders when prompted by Android Studio
- Let Gradle sync
- If this is your first time using Android Studio, set up an emulator (recommend using Medium Phone API 36)
- Build automatically, then run
- Cold Booting the emulator is safe, but do not wipe emulator data or uninstall the app if you want Room data to persist between runs.

# Immediate future plans 
- Optimize app background usage 
- Bug fixing and seeking common crashes
- Hardware implementation


# 195B Weeks 1-2 Persistence
- Room stores each saved `DayEntry` in the on-device SQLite database.
- `FitFuelViewModel` exposes Room-backed state to every Compose screen.
- `DayEntryRepository` separates UI state from database implementation details.
- Saving a recommended meal updates the selected day through the repository.

# 195B Weeks 3-4 Backend and GPT Integration
- A Python FastAPI service now exposes `/health` and `/api/v1/recommendations`.
- The OpenAI API key remains in `backend/.env`; it is never stored in the Android app.
- OpenAI Structured Outputs returns exactly three ranked meal IDs and short explanations.
- Calories, protein, tags, names, and images remain controlled by the Android meal catalog.
- Retrofit and OkHttp connect the recommendation screen to the local backend.
- If the backend or OpenAI is unavailable, FitFuel falls back to deterministic local ranking.

## Start the backend

```bash
cd backend
python -m venv .venv
```

Windows PowerShell:

```powershell
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
Copy-Item .env.example .env
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

macOS/Linux:

```bash
source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

Set `OPENAI_API_KEY` in `backend/.env` to enable GPT. Without a key, the backend still runs and uses its deterministic fallback.

The Android emulator uses `http://10.0.2.2:8000/` to reach the host computer. For a physical phone, change `BACKEND_BASE_URL` in `app/build.gradle.kts` to the computer's LAN address, such as `http://192.168.1.20:8000/`.

## Test the backend

```bash
cd backend
pytest
```

# 195B Weeks 5-6 Tag Personalization
- Room now stores a persistent affinity score for each normalized meal tag.
- Saving a recommended meal records positive signals for that meal's tags.
- Choosing **Not for me** records negative signals and immediately re-ranks recommendations.
- The User page shows the strongest learned positive tags and their signal counts.
- Recommendation requests send the learned tag profile to FastAPI alongside calorie/protein context.
- Both GPT and deterministic fallback ranking prioritize nutrition constraints first, then tag preferences.
- Strongly disliked tags are filtered when at least three acceptable alternatives remain.
- The database includes a version 1 → 2 migration, so existing saved daily entries are retained.

# 195B Weeks 7-8 Testing and Responsiveness
- Added JVM tests for nutrition calculations and local recommendation ranking.
- Added instrumented Room tests for daily entries and tag preferences.
- Added Compose end-to-end persistence coverage across Activity recreation.
- Expanded FastAPI validation/contract tests; backend suite currently passes 9 tests.
- Automatic recommendation loads now deduplicate unchanged requests; manual refresh bypasses the cache.
- Backend recommendation service/OpenAI client is reused for identical settings.
- Calendar and User-page derived work is memoized; recommendation list items have stable keys/content types.
- Existing recommendation cards remain visible during refresh with a progress indicator.
- Critical Compose controls now expose stable semantics tags for automated testing.

See `WEEKS_7_8.md` and `TESTING.md` for the detailed test matrix and run instructions.
