# 195B Weeks 5–6 Implementation

## Added

- Persistent Room table for meal-tag affinity and positive/negative feedback counts.
- Non-destructive Room migration from database version 1 to version 2.
- `TagPreferenceRepository` so preference state remains outside Compose and Room APIs stay outside the ViewModel.
- Positive learning signal when a recommended meal is saved as eaten.
- Explicit **Not for me** action that records negative tag signals and refreshes recommendations.
- User page display of learned preferred meal tags.
- Expanded cuisine/flavor tags in the demo meal catalog.
- Android recommendation requests now include the persisted tag preference profile.
- FastAPI/OpenAI prompts now use positive and negative tag affinities after nutritional constraints.
- Deterministic backend and Android fallback ranking use the same tag-affinity scoring behavior.
- Filtering removes strongly disliked tags when at least three alternatives remain and enforces the remaining calorie ceiling when at least three meals fit.
- Backend tests for positive preference ranking and strong-dislike filtering.

## Preference scoring

- **Save as eaten:** +2 affinity for every tag on the meal and +1 positive signal.
- **Not for me:** -2 affinity for every tag on the meal and +1 negative signal.
- Affinity is capped to the range -20 through +20.
- Tags with affinity <= -4 are considered strongly disliked for filtering.

## Recommendation priority

1. Preserve at least three usable candidate meals.
2. Filter strongly disliked tags when enough alternatives exist.
3. Filter to meals within remaining calories when at least three fit.
4. Score calorie and protein fit against the selected day's goal.
5. Apply positive/negative tag affinity as a personalization adjustment.
6. GPT receives only the filtered candidate list and is instructed that nutrition constraints outrank preferences.
