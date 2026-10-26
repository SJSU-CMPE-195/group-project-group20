from functools import lru_cache

from fastapi import Depends, FastAPI
from fastapi.middleware.cors import CORSMiddleware

from .config import Settings, get_settings
from .models import HealthResponse, RecommendationRequest, RecommendationResponse
from .recommendation_service import RecommendationService


# 195B Weeks 3-4: FastAPI application exposes health and recommendation endpoints.
app = FastAPI(
    title="FitFuel Recommendation API",
    # 195B Weeks 7-8 (v0.5.0): API metadata matches the Android testing/performance milestone.
    version="0.5.0",
)

# 195B Weeks 3-4: Development CORS permits local API inspection tools and future web clients.
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost", "http://127.0.0.1"],
    allow_credentials=False,
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)


# 195B Weeks 7-8 (v0.5.0): Reuse the OpenAI client/service for identical settings instead of rebuilding its HTTP client on every request.
@lru_cache(maxsize=4)
def _cached_recommendation_service(
    openai_enabled: bool,
    openai_api_key: str | None,
    openai_model: str,
) -> RecommendationService:
    return RecommendationService(
        Settings(
            openai_enabled=openai_enabled,
            openai_api_key=openai_api_key,
            openai_model=openai_model,
        )
    )


# 195B Weeks 3-4: Dependency factory creates the recommendation service from environment settings.
def get_recommendation_service(
    settings: Settings = Depends(get_settings),
) -> RecommendationService:
    return _cached_recommendation_service(
        settings.openai_enabled,
        settings.openai_api_key,
        settings.openai_model,
    )


# 195B Weeks 3-4: Health endpoint verifies backend startup before launching the Android app.
@app.get("/health", response_model=HealthResponse)
async def health(settings: Settings = Depends(get_settings)) -> HealthResponse:
    return HealthResponse(
        status="ok",
        openaiConfigured=bool(settings.openai_enabled and settings.openai_api_key),
        model=settings.openai_model,
    )


# 195B Weeks 3-4: Android posts current nutrition context and receives three ranked meal cards.
@app.post("/api/v1/recommendations", response_model=RecommendationResponse)
async def recommendations(
    request: RecommendationRequest,
    service: RecommendationService = Depends(get_recommendation_service),
) -> RecommendationResponse:
    return await service.recommend(request)
