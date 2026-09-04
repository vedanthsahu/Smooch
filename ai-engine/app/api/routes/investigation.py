from fastapi import APIRouter, Depends

from app.schemas.investigation import InvestigationResult
from app.services.investigation_service import InvestigationService, get_investigation_service

router = APIRouter()


@router.get("/investigate/{service_id}", response_model=InvestigationResult)
async def investigate_endpoint(
    service_id: str,
    investigation_service: InvestigationService = Depends(get_investigation_service),
) -> InvestigationResult:
    """Called by the backend when an incident is created -- see
    docs/07-system-architecture.md request lifecycle."""
    return await investigation_service.investigate(service_id)
