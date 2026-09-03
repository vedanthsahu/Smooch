"""Model Gateway -- see docs/09-ai-engine-architecture.md.

A single interface behind which local (Ollama) and external (OpenRouter,
later Bedrock) models are interchangeable. The router picks a *tier*; this
module picks the concrete provider for that tier. Non-streaming for now --
matches Level 3's "single-threaded, one model call" scope in
docs/03-scope-and-roadmap.md; streaming lands when the UI needs it.
"""

import time
from typing import Literal

import httpx
from pydantic import BaseModel

from app.config import settings

Tier = Literal["local", "external"]


class GenerationResult(BaseModel):
    text: str
    provider: str
    model: str
    tier: Tier
    tokens_in: int | None = None
    tokens_out: int | None = None
    latency_ms: int


class ModelUnavailableError(RuntimeError):
    """Raised when the requested tier has no usable provider -- e.g. the
    external tier before an OpenRouter API key is configured. Callers should
    treat this as a routing signal (fall back or escalate to human), never
    silently swallow it."""


async def generate(prompt: str, tier: Tier = "local", model: str | None = None) -> GenerationResult:
    if tier == "local":
        return await _generate_ollama(prompt, model or settings.ollama_default_model)
    if tier == "external":
        return await _generate_openrouter(prompt, model)
    raise ValueError(f"unknown tier '{tier}'")


async def _generate_ollama(prompt: str, model: str) -> GenerationResult:
    start = time.perf_counter()
    async with httpx.AsyncClient(timeout=60.0) as client:
        try:
            response = await client.post(
                f"{settings.ollama_base_url}/api/generate",
                json={"model": model, "prompt": prompt, "stream": False},
            )
            response.raise_for_status()
        except httpx.HTTPError as exc:
            raise ModelUnavailableError(f"ollama unreachable or model '{model}' not available: {exc}") from exc

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


async def _generate_openrouter(prompt: str, model: str | None) -> GenerationResult:
    if not settings.openrouter_api_key:
        raise ModelUnavailableError(
            "external tier has no OpenRouter API key configured -- "
            "see docs/20-open-questions-and-risks.md Q2"
        )
    # Not implemented yet -- wire this up once AI_ENGINE_OPENROUTER_API_KEY is set.
    raise NotImplementedError("OpenRouter provider not implemented yet")
