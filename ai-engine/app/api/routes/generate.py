from fastapi import APIRouter, Depends, HTTPException

from app.exceptions import ModelUnavailableError
from app.schemas.generation import GenerateRequest, GenerationResult
from app.services.model_gateway_service import ModelGatewayService, get_model_gateway_service

router = APIRouter()


@router.post("/generate", response_model=GenerationResult)
async def generate_endpoint(
    request: GenerateRequest,
    model_gateway: ModelGatewayService = Depends(get_model_gateway_service),
) -> GenerationResult:
    """Direct passthrough to the Model Gateway -- for testing the gateway
    itself. Not part of the incident-investigation path (see investigation.py)."""
    try:
        return await model_gateway.generate(request.prompt, tier=request.tier, model=request.model)
    except ModelUnavailableError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
