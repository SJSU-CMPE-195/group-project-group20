# FitFuel — Meal Planning & Nutrition Tracking

FitFuel is an Android meal-planning and nutrition-tracking app. It calculates
calorie and protein targets from user input, persists daily nutrition records
with Room, learns simple meal-tag preferences from feedback, and returns three
goal-aware meal recommendations. Recommendations are served by a FastAPI backend
(with an optional, server-side-only OpenAI step) and fall back to deterministic
on-device recommendations when the backend or OpenAI is unavailable.

## Team

| Name | GitHub | Email |
|------|--------|-------|
| Tanish Shah | [@stanish28](https://github.com/stanish28) | tanish.shah@sjsu.edu |
| Ishan Sikka | [@ishansikka](https://github.com/ishansikka) | ishan.sikka@sjsu.edu |
| Eric Tran | [@erttransjsu](https://github.com/erttransjsu) | eric.tran05@sjsu.edu |
| Zihao He | [@zihaohe2022](https://github.com/zihaohe2022) | zihao.he01@sjsu.edu |

**Advisor:** Mark Ammar Rayes

## Project Description

Many people beginning a fitness program struggle to turn nutrition goals into
daily meal decisions. FitFuel lets a user enter personal and daily nutrition
data, calculates calorie and protein targets (Mifflin–St Jeor based), tracks
progress by date, and recommends meals that fit the remaining needs and selected
fitness goal.

The recommendation path is a controlled pipeline: the Android app owns the meal
catalog and nutrition values, a FastAPI service ranks the candidate meals, and
OpenAI is optional for rewriting the natural-language explanation only (it never
changes nutrition values). If the backend or OpenAI is unavailable, FitFuel
continues with deterministic local recommendations that use the same ranking
logic as the backend.

## Prototype Scope

The implemented prototype supports this end-to-end flow:

1. Enter weight, height, age, sex, fitness goal, calories burned, calories
   eaten, and protein eaten.
2. Calculate calorie and protein targets.
3. Save daily entries to a Room/SQLite database.
4. Review saved days and cumulative user statistics.
5. Request three meal recommendations for the selected day.
6. Use the FastAPI backend when reachable, with a backend-side deterministic
   fallback and an on-device fallback.
7. Save a recommended meal as eaten or dismiss it as “Not for me.”
8. Persist meal-tag preferences so later recommendations reflect feedback.

The fitness-band connection is a **documented, planned** BLE integration path —
it is not yet implemented or tested. The app is fully usable with manual and
sample inputs while that sensor path is developed. See
[docs/hardware-fitness-band-connectivity.md](docs/hardware-fitness-band-connectivity.md).

## System Architecture

```text
Android (Jetpack Compose UI)
        |
   FitFuelViewModel  ──────────────►  Room / SQLite (daily entries, tag prefs)
        |
   RecommendationRepository
        |                         ┌───────────────────────────────┐
        ├── RemoteRecommendation ─┤ FastAPI service  /api/v1/...  │
        |                         │   └─ optional server-side GPT │
        |                         │   └─ deterministic fallback   │
        |                         └───────────────────────────────┘
        └── on-device fallback (RecommendationEngine, same scoring)
```

The Android emulator reaches the backend at `http://10.0.2.2:8000/`
(`BACKEND_BASE_URL` in the app's `build.gradle.kts`). The planned fitness-band
path would feed BLE telemetry into the same daily-nutrition state used by manual
entry.

## Prerequisites

- Android Studio with Android SDK 35, JDK 17
- An Android emulator or device (API 24+)
- Python 3.10+ and pip
- Optional: an OpenAI API key for the live GPT explanation path
- Optional: a BLE-capable device for the (future) telemetry integration

The OpenAI key must remain server-side. Never place it in Android source, commit
it, or show it in the demo video.

## Installation

```bash
# Clone
git clone https://github.com/SJSU-CMPE-195/group-project-group20.git
cd group-project-group20

# Backend dependencies
cd fitfuel_placeholder/backend
python3 -m pip install -r requirements.txt

# (Optional) live OpenAI path: create a local env file and set your key
cp .env.example .env
# edit .env: OPENAI_ENABLED=true and OPENAI_API_KEY=...   (leave disabled for the no-key path)
```

Open `fitfuel_placeholder/` in Android Studio and let Gradle sync for the app.

## Running the Prototype

Start the backend (mock mode — no key, no billable requests):

```bash
cd fitfuel_placeholder/backend
OPENAI_ENABLED=false python3 -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Then run the Android app from Android Studio on an emulator or device. The
backend exposes:

- `GET /health` — service + OpenAI configuration status
- `POST /api/v1/recommendations` — ranks the candidate meals

When OpenAI is disabled or unavailable, responses are tagged
`source: "backend_fallback"`. If the app cannot reach the backend at all, it uses
`source: "android_fallback"`.

## Testing

Backend (no key, no billable request — **verified: 9 passing**):

```bash
cd fitfuel_placeholder/backend
python3 -m pytest -q
```

Android unit and instrumented tests run from Android Studio. If you prefer the
command line and no Gradle wrapper is checked in yet, generate one first:

```bash
cd fitfuel_placeholder
gradle wrapper            # once, if ./gradlew is not present
./gradlew testDebugUnitTest          # JVM unit tests (nutrition + recommendation)
./gradlew connectedDebugAndroidTest  # Room + Compose end-to-end (needs an emulator/device)
```

> Status note: the backend suite is verified. The Android suite is provided in
> `app/src/test` and `app/src/androidTest` and should be run in Android Studio /
> on an emulator as the final verification step before submission.

## Demo

The demo shows one complete user journey: enter a profile and nutrition values →
save the day → open Recommendations → show the three ranked meals and the source
label → save one meal / dismiss one → return to User Stats and show the updated
totals and preferences.

**Demo video:** https://drive.google.com/file/d/1-pBeTOaMbrHi1RQev1v0bt4YqWlTw0h_/view?usp=sharing

> Before submitting, confirm this link is shared (public or unlisted) and shows a
> single 3–5 minute journey; replace it if it is not the final recording.

## Technical Stack

| Area | Technology |
|---|---|
| Mobile UI | Kotlin, Jetpack Compose, Material 3 |
| App state | Android ViewModel, coroutines, StateFlow |
| Local persistence | Room, SQLite |
| Backend | Python, FastAPI, Pydantic, Uvicorn |
| AI (optional) | Server-side OpenAI call, deterministic fallback |
| Networking | Retrofit, Gson, OkHttp |
| Hardware (planned) | BLE GATT telemetry contract |
| Build & tests | Gradle, JUnit, Android instrumentation, pytest |

## What’s Next

- Build and validate the physical BLE fitness-band integration, then feed real
  telemetry into the daily activity/calorie fields.
- Run and harden the Android instrumented test suite on CI.
- Replace cleartext dev HTTP with HTTPS if the backend is hosted.
- Finalize the Expo presentation and UX polish.
