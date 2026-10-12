import json
import random
from math import fabs
from typing import Any

from .config import Settings
from .models import (
    MealCandidate,
    MealRanking,
    RecommendationRequest,
    RecommendationResponse,
    RecommendedMealResponse,
)


# 195B Weeks 3-4: FastAPI service coordinates GPT ranking, validation, and deterministic fallback.
class RecommendationService:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings
        self._client: Any = None
        if settings.openai_enabled and settings.openai_api_key:
            # 195B Weeks 3-4: Lazy import lets fallback mode and tests run before the OpenAI SDK is installed.
            from openai import AsyncOpenAI

            self._client = AsyncOpenAI(api_key=settings.openai_api_key)

    async def recommend(self, request: RecommendationRequest) -> RecommendationResponse:
        # 195B Weeks 5-6: Filtering is shared by sample, GPT, and deterministic paths so disliked tags stay consistent.
        candidates = self._filter_candidates(request)

        if not request.hasUserData:
            selected = random.sample(candidates, k=3)
            return RecommendationResponse(
                recommendations=[
                    self._to_response(
                        meal,
                        self._sample_reason(meal, request),
                    )
                    for meal in selected
                ],
                source="sample",
                message="Save a Daily entry to receive goal-aware GPT rankings; learned meal preferences are already applied.",
            )

        if self._client is not None:
            try:
                return await self._recommend_with_openai(request, candidates)
            except Exception:
                return self._fallback_response(
                    request,
                    candidates,
                    message="OpenAI request failed; personalized deterministic backend ranking was used.",
                )

        return self._fallback_response(
            request,
            candidates,
            message="OPENAI_API_KEY is not configured; personalized deterministic backend ranking was used.",
        )

    # 195B Weeks 3-4: Structured Outputs lets the backend parse a typed ranking instead of free-form text.
    async def _recommend_with_openai(
        self,
        request: RecommendationRequest,
        # 195B Weeks 5-6: GPT only sees candidates that passed deterministic preference/nutrition filtering.
        candidates: list[MealCandidate],
    ) -> RecommendationResponse:
        candidate_payload = [
            {
                "id": meal.id,
                "name": meal.name,
                "calories": meal.calories,
                "protein": meal.protein,
                "tags": meal.tags,
            }
            for meal in candidates
        ]

        # 195B Weeks 5-6: Positive and negative tag scores are explicit prompt context rather than implicit model memory.
        preference_payload = [
            {
                "tag": preference.tag,
                "affinityScore": preference.affinityScore,
                "positiveSignals": preference.positiveSignals,
                "negativeSignals": preference.negativeSignals,
            }
            for preference in request.tagPreferences
            if preference.affinityScore != 0
        ]

        response = await self._client.responses.parse(
            model=self._settings.openai_model,
            input=[
                {
                    "role": "system",
                    "content": (
                        "You rank existing FitFuel meals. Select exactly three distinct meal IDs. "
                        "Never invent or alter calories, protein, names, or IDs. Nutrition constraints "
                        "have priority: favor meals within remaining calories and meals that help close "
                        "the protein gap while respecting the fitness goal. After nutritional fit, use "
                        "tag affinity: positive scores indicate preferences and negative scores indicate "
                        "dislikes. Do not treat preference scores as permission to violate nutrition bounds. "
                        "Keep each reason concise and mention a matching preference when relevant."
                    ),
                },
                {
                    "role": "user",
                    "content": json.dumps(
                        {
                            "date": request.date,
                            "goal": request.goal,
                            "remainingCalories": request.remainingCalories,
                            "remainingProtein": request.remainingProtein,
                            "tagPreferences": preference_payload,
                            "candidates": candidate_payload,
                        }
                    ),
                },
            ],
            text_format=MealRanking,
        )

        ranking = response.output_parsed
        if ranking is None:
            raise ValueError("OpenAI returned no parsed ranking.")

        candidate_by_id = {meal.id: meal for meal in candidates}
        selected_ids = [choice.mealId for choice in ranking.choices]
        if len(set(selected_ids)) != 3 or any(meal_id not in candidate_by_id for meal_id in selected_ids):
            raise ValueError("OpenAI returned unknown or duplicate meal IDs.")

        # 195B Weeks 5-6: Response reports how many learned tags were available to the model for transparency.
        learned_count = sum(1 for preference in request.tagPreferences if preference.affinityScore != 0)
        return RecommendationResponse(
            recommendations=[
                self._to_response(candidate_by_id[choice.mealId], choice.reason)
                for choice in ranking.choices
            ],
            source="openai",
            message=(
                f"Ranked with {self._settings.openai_model} using current daily targets and "
                f"{learned_count} learned tag preference(s)."
            ),
        )

    # 195B Weeks 3-4: Backend fallback preserves useful recommendations during setup or API outages.
    def _fallback_response(
        self,
        request: RecommendationRequest,
        # 195B Weeks 5-6: The fallback ranks the same pre-filtered catalog used by GPT.
        candidates: list[MealCandidate],
        message: str,
    ) -> RecommendationResponse:
        ranked = sorted(
            candidates,
            key=lambda meal: self._score(meal, request),
            reverse=True,
        )[:3]

        return RecommendationResponse(
            recommendations=[
                self._to_response(meal, self._fallback_reason(meal, request))
                for meal in ranked
            ],
            source="backend_fallback",
            message=message,
        )

    # 195B Weeks 5-6: Strongly disliked tags are excluded when at least three alternatives remain; calorie fit is then enforced when possible.
    @classmethod
    def _filter_candidates(cls, request: RecommendationRequest) -> list[MealCandidate]:
        preference_scores = cls._preference_scores(request)
        strongly_disliked = {
            tag for tag, score in preference_scores.items() if score <= -4
        }

        preference_safe = [
            meal
            for meal in request.candidates
            if not any(tag.lower() in strongly_disliked for tag in meal.tags)
        ]
        candidates = preference_safe if len(preference_safe) >= 3 else list(request.candidates)

        if request.hasUserData and request.remainingCalories is not None:
            calories_left = max(request.remainingCalories, 0)
            within_budget = [meal for meal in candidates if meal.calories <= calories_left]
            if len(within_budget) >= 3:
                candidates = within_budget

        return candidates

    # 195B Weeks 5-6: Normalized preference map is reused by filtering, scoring, and explanations.
    @staticmethod
    def _preference_scores(request: RecommendationRequest) -> dict[str, int]:
        return {
            preference.tag.strip().lower(): preference.affinityScore
            for preference in request.tagPreferences
            if preference.tag.strip()
        }

    # 195B Weeks 3-4: Deterministic scoring mirrors the proof-of-concept intent without calling GPT.
    @classmethod
    def _score(cls, meal: MealCandidate, request: RecommendationRequest) -> float:
        calories_left = max(request.remainingCalories or 0, 0)
        protein_left = max(request.remainingProtein or 0, 0)
        score = 300.0 - fabs(calories_left - meal.calories)
        score += 120.0 if meal.calories <= calories_left else -100.0
        score += min(meal.protein, protein_left) * 6.0

        if request.goal == "Lose Weight":
            score -= meal.calories * 0.25
            score += meal.protein * 4.0
            if "lower-calorie" in meal.tags:
                score += 45.0
        elif request.goal == "Gain Muscle":
            score += meal.protein * 8.0
            score += meal.calories * 0.08
            if "high-protein" in meal.tags:
                score += 60.0
        else:
            score += meal.protein * 5.0

        # 195B Weeks 5-6: Tag affinity provides a bounded personalization bonus/penalty after nutrition scoring.
        preference_scores = cls._preference_scores(request)
        score += sum(preference_scores.get(tag.lower(), 0) for tag in meal.tags) * 25.0
        return score

    # 195B Weeks 3-4: Fallback explanations remain human-readable in the same UI as GPT output.
    @classmethod
    def _fallback_reason(cls, meal: MealCandidate, request: RecommendationRequest) -> str:
        calorie_note = (
            "fits the remaining calorie budget"
            if meal.calories <= max(request.remainingCalories or 0, 0)
            else "is the closest available comparison above the remaining calorie budget"
        )
        protein_note = (
            "provides strong protein support"
            if meal.protein >= max((request.remainingProtein or 0) // 2, 1)
            else "provides some protein toward the remaining target"
        )

        # 195B Weeks 5-6: Fallback rationale surfaces the strongest matching learned preferences.
        preference_scores = cls._preference_scores(request)
        matched = sorted(
            [tag for tag in meal.tags if preference_scores.get(tag.lower(), 0) > 0],
            key=lambda tag: preference_scores.get(tag.lower(), 0),
            reverse=True,
        )[:2]
        preference_note = (
            f" It also matches your learned preference for {', '.join(matched)}."
            if matched
            else ""
        )
        return (
            f"{meal.name} {calorie_note} and {protein_note} for the "
            f"{request.goal or 'current'} goal.{preference_note}"
        )

    # 195B Weeks 5-6: Sample explanations can acknowledge persisted preferences even before the selected day is configured.
    @classmethod
    def _sample_reason(cls, meal: MealCandidate, request: RecommendationRequest) -> str:
        preference_scores = cls._preference_scores(request)
        matched = [tag for tag in meal.tags if preference_scores.get(tag.lower(), 0) > 0][:2]
        if matched:
            return (
                "Sample recommendation shown until daily targets are saved; it also matches your "
                f"learned preference for {', '.join(matched)}."
            )
        return "Sample recommendation shown until daily calorie and protein targets are saved."

    # 195B Weeks 3-4: Catalog nutrition is copied unchanged into the response after ranking.
    @staticmethod
    def _to_response(meal: MealCandidate, reason: str) -> RecommendedMealResponse:
        return RecommendedMealResponse(**meal.model_dump(), reason=reason)
