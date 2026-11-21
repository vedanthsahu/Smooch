"""Local-tier model provider -- see docs/adr/ADR-009-local-model-strategy.md."""

import time

import httpx

from app.config import settings
from app.exceptions import ModelUnavailableError
from app.schemas.generation import GenerationResult


class OllamaRepository:
    def __init__(self, base_url: str | None = None, timeout: float = 60.0) -> None:
        self._base_url = base_url or settings.ollama_base_url
        self._timeout = timeout

    async def generate(self, prompt: str, model: str | None = None) -> GenerationResult:
        model = model or settings.ollama_default_model
        start = time.perf_counter()
        async with httpx.AsyncClient(timeout=self._timeout) as client:
            try:
                response = await client.post(
                    f"{self._base_url}/api/generate",
                    json={"model": model, "prompt": prompt, "stream": False},
                )
                response.raise_for_status()
            except httpx.HTTPError as exc:
                raise ModelUnavailableError(
                    f"ollama unreachable or model '{model}' not available: {exc}"
                ) from exc

        data = response.json()
        latency_ms = round((time.perf_counter() - start) * 1000)
        return GenerationResult(
            text=data.get("response", ""),
            provider="ollama",
            model=model,
            tier="local",
            tokens_in=data.get("prompt_eval_count"),
            tokens_out=data.get("eval_count"),
            latency_ms=latency_ms,
        )


def get_ollama_repository() -> OllamaRepository:
    return OllamaRepository()
