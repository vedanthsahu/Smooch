from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    simulator_base_url: str = "http://127.0.0.1:8090"

    # Local tier -- confirmed direction, see docs/adr/ADR-009-local-model-strategy.md
    ollama_base_url: str = "http://127.0.0.1:11434"
    ollama_default_model: str = "llama3.2:1b"

    # External/reasoning tier -- not wired yet, needs an API key.
    # See docs/adr/ADR-010-api-cloud-model-strategy.md.
    openrouter_api_key: str | None = None
    openrouter_base_url: str = "https://openrouter.ai/api/v1"

    class Config:
        env_prefix = "AI_ENGINE_"
        env_file = ".env"


settings = Settings()
