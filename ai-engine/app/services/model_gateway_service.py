"""Model Gateway -- see docs/09-ai-engine-architecture.md.

A single interface behind which local (Ollama) and external (OpenRouter,
later Bedrock) models are interchangeable. The router picks a *tier*; this
service picks the concrete provider (repository) for that tier.
"""

from app.repositories.ollama_repository import OllamaRepository, get_ollama_repository
from app.repositories.openrouter_repository import OpenRouterRepository, get_openrouter_repository
from app.schemas.generation import GenerationResult, Tier


class ModelGatewayService:
    def __init__(self, ollama: OllamaRepository, openrouter: OpenRouterRepository) -> None:
        self._ollama = ollama
        self._openrouter = openrouter

    async def generate(self, prompt: str, tier: Tier = "local", model: str | None = None) -> GenerationResult:
        if tier == "local":
            return await self._ollama.generate(prompt, model)
        if tier == "external":
            return await self._openrouter.generate(prompt, model)
        raise ValueError(f"unknown tier '{tier}'")


def get_model_gateway_service() -> ModelGatewayService:
    """FastAPI dependency factory -- see app/api/routes/*."""
    return ModelGatewayService(get_ollama_repository(), get_openrouter_repository())
