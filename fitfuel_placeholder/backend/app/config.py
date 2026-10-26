from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


# 195B Weeks 3-4: Environment-backed settings keep the OpenAI key outside Android and source control.
class Settings(BaseSettings):
    openai_api_key: str | None = None
    openai_model: str = "gpt-5.6"
    openai_enabled: bool = True

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )


# 195B Weeks 3-4: Cached settings are shared across requests.
@lru_cache
def get_settings() -> Settings:
    return Settings()
