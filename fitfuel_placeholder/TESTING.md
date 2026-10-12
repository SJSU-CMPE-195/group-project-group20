## Test layers

| Layer | Location | Purpose |
|---|---|---|
| JVM nutrition unit tests | `app/src/test/.../NutritionCalculatorTest.kt` | Verify BMR, goal adjustments, protein targets, and missing-input behavior. |
| JVM recommendation tests | `app/src/test/.../RecommendationEngineTest.kt` | Verify local fallback ranking and disliked-tag filtering. |
| Room DAO/repository tests | `app/src/androidTest/.../data/local/` | Verify SQLite-backed day persistence, tag preferences, and save-recommended-meal total updates. |
| Compose end-to-end | `app/src/androidTest/.../FitFuelEndToEndTest.kt` | Verify Daily input is saved to Room, displayed in User Stats, and survives Activity recreation. |
| Backend API tests | `backend/tests/` | Verify health, request validation, goal/preference ranking, response contracts, and service caching. |

## Backend tests

```bash
cd backend
pytest
```

Expected v0.5.0 result: **9 passing tests** with OpenAI disabled in the test configuration, so no billable API request is required.

## Android JVM tests

Run from Android Studio, or from a generated Gradle wrapper:

```bash
./gradlew testDebugUnitTest
```

## Android instrumented tests

Start an emulator/device first, then run:

```bash
./gradlew connectedDebugAndroidTest
```

The Compose suite verifies that all four primary navigation buttons remain visible, then clears its test database, enters sample Daily values, saves them, checks User Stats, recreates `MainActivity`, and verifies the Room-backed values remain available.

## Manual regression checklist

- Save multiple dates and confirm calendar saved-day indicators remain after app restart.
- Confirm User Stats totals match the sum of persisted daily values.
- Open Recommendations with the backend stopped and verify Android local fallback still returns meals.
- Start the backend without an OpenAI key and verify `backend_fallback` is reported.
- Enable an OpenAI key and verify `openai` is reported when the API call succeeds.
- Save a meal and verify positive tags appear on User Stats and influence later rankings.
- Choose **Not for me** repeatedly and verify strongly disliked tags are excluded when enough alternatives exist.
- Press **Refresh Recommendations** and confirm existing cards remain visible while the progress indicator is shown.
