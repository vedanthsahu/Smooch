"""Model Gateway request/response shapes -- see docs/09-ai-engine-architecture.md."""

from typing import Literal

from pydantic import BaseModel

Tier = Literal["local", "external"]


class GenerateRequest(BaseModel):
    prompt: str
    tier: Tier = "local"
    model: str | None = None


class GenerationResult(BaseModel):
    text: str
    provider: str
    model: str
    tier: Tier
    tokens_in: int | None = None
    tokens_out: int | None = None
    latency_ms: int
