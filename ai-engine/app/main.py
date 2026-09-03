"""Adaptive Incident Reasoning engine -- see docs/09-ai-engine-architecture.md.

Level 3: sequential evidence gathering + one model call, via the Model
Gateway (Ollama local tier). Router (multi-strategy decisioning) and
concurrent evidence gathering are not built yet -- see
docs/03-scope-and-roadmap.md Level 5.
"""

import httpx
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from app.config import settings
from app.investigation import InvestigationResult, investigate
from app.model_gateway import GenerationResult, ModelUnavailableError, Tier, generate

app = FastAPI(title="Adaptive Incident Reasoning Engine", version="0.3.0")


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "service": "ai-engine"}


@app.get("/health/deep")
async def health_deep() -> dict:
    """Proves the AI engine -> Simulator call path (Level 0 walking skeleton)."""
    async with httpx.AsyncClient(timeout=5.0) as client:
        try:
            response = await client.get(f"{settings.simulator_base_url}/health")
            response.raise_for_status()
        except httpx.HTTPError as exc:
            raise HTTPException(status_code=502, detail=f"simulator unreachable: {exc}") from exc

    return {
        "status": "ok",
        "service": "ai-engine",
        "simulator": response.json(),
    }


class GenerateRequest(BaseModel):
    prompt: str
    tier: Tier = "local"
    model: str | None = None


@app.post("/generate", response_model=GenerationResult)
async def generate_endpoint(request: GenerateRequest) -> GenerationResult:
    """Direct passthrough to the Model Gateway -- for testing the gateway
    itself. Not part of the incident-investigation path yet (docs/09)."""
    try:
        return await generate(request.prompt, tier=request.tier, model=request.model)
    except ModelUnavailableError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc


@app.get("/investigate/{service_id}", response_model=InvestigationResult)
async def investigate_endpoint(service_id: str) -> InvestigationResult:
    """Called by the backend when an incident is created -- see
    docs/07-system-architecture.md request lifecycle."""
    return await investigate(service_id)
