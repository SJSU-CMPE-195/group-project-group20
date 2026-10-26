from app.main import _cached_recommendation_service


# 195B Weeks 7-8 (v0.5.0): Service cache test guards the optimization that reuses the backend/OpenAI HTTP client between identical requests.
def test_recommendation_service_is_reused_for_same_settings() -> None:
    _cached_recommendation_service.cache_clear()
    first = _cached_recommendation_service(False, None, "gpt-5.6")
    second = _cached_recommendation_service(False, None, "gpt-5.6")

    assert first is second
    assert _cached_recommendation_service.cache_info().hits == 1
