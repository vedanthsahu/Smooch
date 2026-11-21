"""External/reasoning-tier model provider -- see docs/adr/ADR-010-api-cloud-model-strategy.md.
OpenRouter's API is OpenAI-compatible (chat completions), see
https://openrouter.ai/docs/api-reference/chat-completion.
"""

import time

import httpx

from app.config import settings
from app.exceptions import ModelUnavailableError
from app.schemas.generation import GenerationResult


class OpenRouterRepository:
    def __init__(
        self, api_key: str | None = None, base_url: str | None = None, timeout: float = 60.0
    ) -> None:
        self._api_key = api_key or settings.openrouter_api_key
        self._base_url = base_url or settings.openrouter_base_url
        self._timeout = timeout

    async def generate(self, prompt: str, model: str | None = None) -> GenerationResult:
        if not self._api_key:
            raise ModelUnavailableError(
                "external tier has no OpenRouter API key configured -- "
                "see docs/20-open-questions-and-risks.md Q2"
            )
        model = model or settings.openrouter_default_model
        start = time.perf_counter()
        async with httpx.AsyncClient(timeout=self._timeout) as client:
            try:
                response = await client.post(
                    f"{self._base_url}/chat/completions",
                    headers={"Authorization": f"Bearer {self._api_key}"},
                    json={"model": model, "messages": [{"role": "user", "content": prompt}]},
                )
                response.raise_for_status()
            except httpx.HTTPError as exc:
                raise ModelUnavailableError(
                    f"openrouter unreachable or model '{model}' unavailable: {exc}"
                ) from exc

        data = response.json()
        latency_ms = round((time.perf_counter() - start) * 1000)
        choice = data["choices"][0]["message"]
        usage = data.get("usage", {})
        return GenerationResult(
            text=choice.get("content") or "",
            provider=data.get("provider", "openrouter"),
            model=model,
            tier="external",
            tokens_in=usage.get("prompt_tokens"),
            tokens_out=usage.get("completion_tokens"),
            latency_ms=latency_ms,
        )


def get_openrouter_repository() -> OpenRouterRepository:
    return OpenRouterRepository()
