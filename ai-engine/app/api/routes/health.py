from fastapi import APIRouter, Depends, HTTPException
from httpx import HTTPError

from app.repositories.simulator_repository import SimulatorRepository, get_simulator_repository

router = APIRouter()


@router.get("/health")
def health() -> dict:
    return {"status": "ok", "service": "ai-engine"}


@router.get("/health/deep")
async def health_deep(
    simulator: SimulatorRepository = Depends(get_simulator_repository),
) -> dict:
    """Proves the AI engine -> Simulator call path (Level 0 walking skeleton)."""
    try:
        simulator_health = await simulator.health()
    except HTTPError as exc:
        raise HTTPException(status_code=502, detail=f"simulator unreachable: {exc}") from exc

    return {"status": "ok", "service": "ai-engine", "simulator": simulator_health}
