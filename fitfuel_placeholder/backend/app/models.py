from pydantic import BaseModel, Field, model_validator


# 195B Weeks 3-4: Android supplies its catalog so nutrition values remain controlled by FitFuel, not GPT.
class MealCandidate(BaseModel):
    id: str
    name: str
    calories: int = Field(ge=0)
    protein: int = Field(ge=0)
    tags: list[str]
    emoji: str
    description: str


# 195B Weeks 5-6: Backend receives the Android-persisted tag score instead of maintaining duplicate preference state.
class TagPreferenceContext(BaseModel):
    tag: str
    affinityScore: int = Field(ge=-20, le=20)
    positiveSignals: int = Field(default=0, ge=0)
    negativeSignals: int = Field(default=0, ge=0)


# 195B Weeks 3-4: Request context mirrors the selected Room-backed day in the Android app.
class RecommendationRequest(BaseModel):
    date: str
    goal: str | None = None
    remainingCalories: int | None = None
    remainingProtein: int | None = None
    hasUserData: bool = False
    candidates: list[MealCandidate]
    # 195B Weeks 5-6: Preference context is optional for backward compatibility with older Android builds.
    tagPreferences: list[TagPreferenceContext] = Field(default_factory=list)

    @model_validator(mode="after")
    def require_three_candidates(self) -> "RecommendationRequest":
        if len(self.candidates) < 3:
            raise ValueError("At least three candidate meals are required.")
        return self


# 195B Weeks 3-4: Structured Output schema restricts GPT to meal IDs and explanations only.
class RankedMealChoice(BaseModel):
    mealId: str
    reason: str


# 195B Weeks 3-4: GPT must return exactly three ranked choices.
class MealRanking(BaseModel):
    choices: list[RankedMealChoice] = Field(min_length=3, max_length=3)


# 195B Weeks 3-4: Response includes catalog-controlled nutrition values plus the GPT explanation.
class RecommendedMealResponse(MealCandidate):
    reason: str


# 195B Weeks 3-4: Source and message fields expose fallback behavior to the Android UI.
class RecommendationResponse(BaseModel):
    recommendations: list[RecommendedMealResponse]
    source: str
    message: str | None = None


# 195B Weeks 3-4: Health response reports whether the server is configured to call OpenAI.
class HealthResponse(BaseModel):
    status: str
    openaiConfigured: bool
    model: str
