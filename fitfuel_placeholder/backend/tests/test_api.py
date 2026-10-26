from fastapi.testclient import TestClient

from app.config import Settings, get_settings
from app.main import app


# 195B Weeks 3-4: Tests force fallback mode so no API key or billable request is required.
def override_settings() -> Settings:
    return Settings(openai_enabled=False, openai_api_key=None)


app.dependency_overrides[get_settings] = override_settings
client = TestClient(app)


# 195B Weeks 3-4: Shared three-meal fixture exercises the public request contract.
CANDIDATES = [
    {
        "id": "caesar_salad",
        "name": "Caesar Salad",
        "calories": 350,
        "protein": 12,
        "tags": ["italian", "salad", "savory", "light", "lower-calorie"],
        "emoji": "🥗",
        "description": "A lighter option.",
    },
    {
        "id": "carbonara",
        "name": "Spaghetti Carbonara",
        "calories": 500,
        "protein": 18,
        "tags": ["italian", "pasta", "savory", "comfort", "higher-calorie"],
        "emoji": "🍝",
        "description": "A richer pasta option.",
    },
    {
        "id": "filet_mignon",
        "name": "Filet Mignon",
        "calories": 550,
        "protein": 40,
        "tags": ["beef", "steak", "savory", "high-protein", "premium"],
        "emoji": "🥩",
        "description": "A protein-forward option.",
    },
]


def test_health() -> None:
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"
    assert response.json()["openaiConfigured"] is False


def test_sample_recommendations_without_day_data() -> None:
    response = client.post(
        "/api/v1/recommendations",
        json={
            "date": "2026-08-05",
            "goal": None,
            "remainingCalories": None,
            "remainingProtein": None,
            "hasUserData": False,
            "candidates": CANDIDATES,
        },
    )
    assert response.status_code == 200
    payload = response.json()
    assert payload["source"] == "sample"
    assert len(payload["recommendations"]) == 3


def test_goal_aware_fallback_without_openai_key() -> None:
    response = client.post(
        "/api/v1/recommendations",
        json={
            "date": "2026-08-05",
            "goal": "Gain Muscle",
            "remainingCalories": 650,
            "remainingProtein": 45,
            "hasUserData": True,
            "candidates": CANDIDATES,
        },
    )
    assert response.status_code == 200
    payload = response.json()
    assert payload["source"] == "backend_fallback"
    assert len(payload["recommendations"]) == 3
    assert payload["recommendations"][0]["id"] == "filet_mignon"


# 195B Weeks 5-6: Equal-macro candidates isolate tag affinity so preference-driven ranking is directly measurable.
def test_positive_tag_preference_changes_ranking() -> None:
    equal_candidates = [
        {
            "id": "meal_a",
            "name": "Meal A",
            "calories": 400,
            "protein": 25,
            "tags": ["italian"],
            "emoji": "A",
            "description": "A",
        },
        {
            "id": "meal_b",
            "name": "Meal B",
            "calories": 400,
            "protein": 25,
            "tags": ["mexican"],
            "emoji": "B",
            "description": "B",
        },
        {
            "id": "meal_c",
            "name": "Meal C",
            "calories": 400,
            "protein": 25,
            "tags": ["japanese"],
            "emoji": "C",
            "description": "C",
        },
        {
            "id": "meal_d",
            "name": "Meal D",
            "calories": 400,
            "protein": 25,
            "tags": ["mediterranean"],
            "emoji": "D",
            "description": "D",
        },
    ]
    response = client.post(
        "/api/v1/recommendations",
        json={
            "date": "2026-08-10",
            "goal": "Maintain",
            "remainingCalories": 500,
            "remainingProtein": 30,
            "hasUserData": True,
            "candidates": equal_candidates,
            "tagPreferences": [
                {
                    "tag": "mexican",
                    "affinityScore": 8,
                    "positiveSignals": 4,
                    "negativeSignals": 0,
                }
            ],
        },
    )
    assert response.status_code == 200
    assert response.json()["recommendations"][0]["id"] == "meal_b"


# 195B Weeks 5-6: A strongly disliked tag is removed when three acceptable alternatives remain.
def test_strong_dislike_filters_candidate() -> None:
    candidates = [
        {
            "id": "spicy",
            "name": "Spicy Meal",
            "calories": 350,
            "protein": 20,
            "tags": ["spicy"],
            "emoji": "S",
            "description": "S",
        },
        {
            "id": "mild_a",
            "name": "Mild A",
            "calories": 350,
            "protein": 20,
            "tags": ["mild"],
            "emoji": "A",
            "description": "A",
        },
        {
            "id": "mild_b",
            "name": "Mild B",
            "calories": 350,
            "protein": 20,
            "tags": ["quick"],
            "emoji": "B",
            "description": "B",
        },
        {
            "id": "mild_c",
            "name": "Mild C",
            "calories": 350,
            "protein": 20,
            "tags": ["savory"],
            "emoji": "C",
            "description": "C",
        },
    ]
    response = client.post(
        "/api/v1/recommendations",
        json={
            "date": "2026-08-10",
            "goal": "Maintain",
            "remainingCalories": 500,
            "remainingProtein": 25,
            "hasUserData": True,
            "candidates": candidates,
            "tagPreferences": [
                {
                    "tag": "spicy",
                    "affinityScore": -6,
                    "positiveSignals": 0,
                    "negativeSignals": 3,
                }
            ],
        },
    )
    assert response.status_code == 200
    ids = [item["id"] for item in response.json()["recommendations"]]
    assert "spicy" not in ids
    assert len(ids) == 3


# 195B Weeks 7-8 (v0.5.0): Contract test rejects requests that cannot produce the required three recommendation cards.
def test_rejects_less_than_three_candidates() -> None:
    response = client.post(
        "/api/v1/recommendations",
        json={
            "date": "2026-08-14",
            "goal": "Maintain",
            "remainingCalories": 500,
            "remainingProtein": 25,
            "hasUserData": True,
            "candidates": CANDIDATES[:2],
        },
    )
    assert response.status_code == 422


# 195B Weeks 7-8 (v0.5.0): Pydantic validation prevents impossible negative catalog nutrition from entering ranking logic.
def test_rejects_negative_candidate_nutrition() -> None:
    invalid = [dict(candidate) for candidate in CANDIDATES]
    invalid[0]["calories"] = -1

    response = client.post(
        "/api/v1/recommendations",
        json={
            "date": "2026-08-14",
            "goal": "Maintain",
            "remainingCalories": 500,
            "remainingProtein": 25,
            "hasUserData": True,
            "candidates": invalid,
        },
    )
    assert response.status_code == 422


# 195B Weeks 7-8 (v0.5.0): End-to-end API contract verifies exactly three distinct catalog-controlled meals are returned unchanged.
def test_recommendation_contract_preserves_catalog_values() -> None:
    response = client.post(
        "/api/v1/recommendations",
        json={
            "date": "2026-08-14",
            "goal": "Gain Muscle",
            "remainingCalories": 700,
            "remainingProtein": 50,
            "hasUserData": True,
            "candidates": CANDIDATES,
        },
    )

    assert response.status_code == 200
    payload = response.json()
    recommendations = payload["recommendations"]
    assert len(recommendations) == 3
    assert len({item["id"] for item in recommendations}) == 3

    source_by_id = {item["id"]: item for item in CANDIDATES}
    for recommendation in recommendations:
        source = source_by_id[recommendation["id"]]
        assert recommendation["name"] == source["name"]
        assert recommendation["calories"] == source["calories"]
        assert recommendation["protein"] == source["protein"]
        assert recommendation["tags"] == source["tags"]
