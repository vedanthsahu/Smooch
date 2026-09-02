"""Production Environment Simulator.

Synthetic scenario/telemetry generator -- see docs/22-production-simulator.md.
Level 0: bare health check + one hardcoded simulated service, no scenario engine yet.
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(title="Production Simulator", version="0.1.0")

# In-memory simulated estate. Real scenario engine (state machines, scripted
# transitions) lands at Level 2 -- see docs/22-production-simulator.md.
_SERVICES = {
    "payment-api": {"production": "payments", "status": "healthy"},
}


class ServiceHealth(BaseModel):
    service_id: str
    status: str
    production: str


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "service": "simulator"}


@app.get("/services/{service_id}/health", response_model=ServiceHealth)
def service_health(service_id: str) -> ServiceHealth:
    service = _SERVICES.get(service_id)
    if service is None:
        raise HTTPException(status_code=404, detail=f"unknown service '{service_id}'")
    return ServiceHealth(service_id=service_id, status=service["status"], production=service["production"])
