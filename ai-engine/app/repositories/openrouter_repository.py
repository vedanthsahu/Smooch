"""External/reasoning-tier model provider -- see docs/adr/ADR-010-api-cloud-model-strategy.md.
Not implemented yet: needs AI_ENGINE_OPENROUTER_API_KEY. Kept as a real class
(not a free function) so wiring the actual HTTP call later doesn't change
the call site in ModelGatewayService -- see docs/20-open-questions-and-risks.md Q2.
"""

from app.config import settings
from app.exceptions import ModelUnavailableError
from app.schemas.generation import GenerationResult


class OpenRouterRepository:
    def __init__(self, api_key: str | None = None, base_url: str | None = None) -> None:
        self._api_key = api_key or settings.openrouter_api_key
        self._base_url = base_url or settings.openrouter_base_url

    async def generate(self, prompt: str, model: str | None = None) -> GenerationResult:
        if not self._api_key:
            raise ModelUnavailableError(
                "external tier has no OpenRouter API key configured -- "
                "see docs/20-open-questions-and-risks.md Q2"
            )
        raise NotImplementedError("OpenRouter provider not implemented yet")


def get_openrouter_repository() -> OpenRouterRepository:
    return OpenRouterRepository()
