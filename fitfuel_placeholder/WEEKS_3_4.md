# 195B Weeks 3–4 Implementation

## Added

- Python FastAPI backend with health and recommendation endpoints.
- Server-side OpenAI integration using Structured Outputs.
- Retrofit/OkHttp Android client.
- Recommendation repository with Android fallback.
- ViewModel-owned loading, source, message, and recommendation state.
- Recommendation-screen loading indicator, source label, and manual refresh.
- Backend tests that do not require an OpenAI key.

## Request flow

1. The selected Room-backed day is read by `FitFuelViewModel`.
2. Android sends remaining calories, remaining protein, goal, date, and the local meal catalog to FastAPI.
3. FastAPI asks OpenAI to return exactly three meal IDs and reasons.
4. FastAPI validates the IDs and attaches unchanged catalog nutrition values.
5. Android displays the result and can persist a selected meal through the existing Room repository.
6. If FastAPI or OpenAI is unavailable, deterministic fallback recommendations are shown.
