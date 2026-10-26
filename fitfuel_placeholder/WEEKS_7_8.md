## Testing additions

- Added pure JVM tests for BMR/calorie and protein target calculations.
- Added JVM coverage for the Android deterministic recommendation fallback and strong-dislike filtering.
- Added instrumented Room tests for daily-entry persistence/upsert behavior.
- Added instrumented Room tests for tag preference accumulation, normalization, and affinity clamping.
- Added repository integration coverage proving that saving a recommended meal persists calories/protein and recomputes remaining targets.
- Added Compose regression coverage for all four navigation destinations and an end-to-end persistence scenario covering Daily input → Save Day → Room → User Stats → Activity recreation.
- Expanded FastAPI tests to cover malformed candidate counts, invalid negative nutrition values, exact/distinct recommendation response contracts, and backend recommendation-service reuse.

## Performance and responsiveness additions

- Automatic recommendation requests now reuse results when date, daily nutrition state, and preference signals have not changed.
- Manual **Refresh Recommendations** bypasses that cache so users can explicitly request a new ranking.
- Backend recommendation services are cached for identical settings so the OpenAI HTTP client is not rebuilt on every request.
- Calendar-cell generation is memoized per displayed month.
- User cumulative statistics and preferred-tag sorting are memoized against their source maps.
- Core Compose-facing domain models are marked immutable to improve stability inference.
- Recommendation list items use stable IDs/content types for better lazy-list reuse.
- Existing recommendation cards stay visible during refresh while a lightweight progress bar communicates network activity.
- Stable Compose semantics/test tags were added to critical navigation, input, save, refresh, and meal-card controls.

## Version

Android `versionName` is now `0.5.0` with `versionCode = 5`. The FastAPI metadata version is also `0.5.0`.
