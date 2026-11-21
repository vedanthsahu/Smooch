from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    simulator_base_url: str = "http://127.0.0.1:8090"

    # Local tier -- confirmed direction, see docs/adr/ADR-009-local-model-strategy.md
    ollama_base_url: str = "http://127.0.0.1:11434"
    ollama_default_model: str = "llama3.2:1b"

    # External/reasoning tier -- see docs/adr/ADR-010-api-cloud-model-strategy.md.
    # Resolves docs/20-open-questions-and-risks.md Q2: OpenRouter is the
    # initial external provider (Bedrock is account-wide blocked pending an
    # AWS Support case, see infra/aws/bedrock/).
    openrouter_api_key: str | None = None
    openrouter_base_url: str = "https://openrouter.ai/api/v1"
    # Free-tier model catalog rotates/gets rate-limited upstream -- verified
    # working 2026-11-21. If this starts failing, check
    # https://openrouter.ai/models?max_price=0 for a current replacement.
    openrouter_default_model: str = "nvidia/nemotron-3.5-lightning:free"

    class Config:
        env_prefix = "AI_ENGINE_"
        env_file = ".env"


settings = Settings()
