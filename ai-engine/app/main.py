"""Adaptive Incident Reasoning engine -- see docs/09-ai-engine-architecture.md.

Level 0: bare health check + one proven call to the Simulator, no router/model
gateway/evidence-gathering logic yet.
"""

import httpx
from fastapi import FastAPI, HTTPException

from app.config import settings

app = FastAPI(title="Adaptive Incident Reasoning Engine", version="0.1.0")


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
