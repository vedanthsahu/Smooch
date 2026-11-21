"""Production Environment Simulator.

Synthetic scenario/telemetry generator -- see docs/22-production-simulator.md.
Level 2: Scenario A (queue consumer failure) wired end-to-end. Scenarios
B-E are not implemented yet -- see docs/03-scope-and-roadmap.md.
"""

import time

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from app.scenario_engine import (
    ScenarioState,
    SimulatorState,
    compute_dependent_metrics,
    compute_worker_metrics,
    compute_worker_status,
    synthetic_logs,
)

app = FastAPI(title="Production Simulator", version="0.2.0")

# In-memory simulated estate. Real per-service DBs/queues are explicitly out
# of scope -- see ADR-015-production-simulator-scope.md.
_SERVICES: dict[str, dict] = {
    "payment-api": {"production": "payments", "role": "dependent", "depends_on": "payment-worker"},
    "payment-worker": {"production": "payments", "role": "consumer"},
}

_state = SimulatorState()


class ServiceHealth(BaseModel):
    service_id: str
    status: str
    production: str


class ServiceMetrics(BaseModel):
    service_id: str
    metrics: dict


class ServiceLogs(BaseModel):
    service_id: str
    lines: list[str]


class ScenarioStartResponse(BaseModel):
    scenario: str
    target_service_id: str
    started_at: float


class ActionResponse(BaseModel):
    service_id: str
    action: str
    result: str


def _require_service(service_id: str) -> dict:
    service = _SERVICES.get(service_id)
    if service is None:
        raise HTTPException(status_code=404, detail=f"unknown service '{service_id}'")
    return service


def _status_for(service_id: str) -> str:
    now = time.time()
    scenario = _state.active_scenario
    if scenario is not None and scenario.target_service_id == service_id:
        return compute_worker_status(scenario, now)
    if scenario is not None and _SERVICES[service_id].get("depends_on") == scenario.target_service_id:
        worker_status = compute_worker_status(scenario, now)
        return "degraded" if worker_status in ("down", "recovering") else "healthy"
    return "healthy"


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "service": "simulator"}


@app.get("/services/{service_id}/health", response_model=ServiceHealth)
def service_health(service_id: str) -> ServiceHealth:
    service = _require_service(service_id)
    return ServiceHealth(service_id=service_id, status=_status_for(service_id), production=service["production"])


@app.get("/services/{service_id}/metrics", response_model=ServiceMetrics)
def service_metrics(service_id: str) -> ServiceMetrics:
    service = _require_service(service_id)
    now = time.time()
    scenario = _state.active_scenario

    if service["role"] == "consumer":
        metrics = compute_worker_metrics(scenario if scenario and scenario.target_service_id == service_id else None, now)
    else:
        relevant_scenario = scenario if scenario and service.get("depends_on") == scenario.target_service_id else None
        metrics = compute_dependent_metrics(relevant_scenario, now)

    return ServiceMetrics(service_id=service_id, metrics=metrics)


@app.get("/services/{service_id}/dependencies")
def service_dependencies(service_id: str) -> dict:
    """Lets a caller discover what else to gather evidence for -- see
    docs/22-production-simulator.md. payment-api depends on payment-worker,
    so an investigation limited to payment-api alone is missing the actual
    root cause."""
    service = _require_service(service_id)
    depends_on = service.get("depends_on")
    return {"service_id": service_id, "depends_on": [depends_on] if depends_on else []}


@app.get("/services/{service_id}/logs", response_model=ServiceLogs)
def service_logs(service_id: str) -> ServiceLogs:
    _require_service(service_id)
    now = time.time()
    scenario = _state.active_scenario
    lines = synthetic_logs(service_id, scenario, now)
    return ServiceLogs(service_id=service_id, lines=lines)


@app.post("/scenarios/queue-consumer-failure/start", response_model=ScenarioStartResponse)
def start_queue_consumer_failure() -> ScenarioStartResponse:
    """Scenario A -- see docs/22-production-simulator.md."""
    now = time.time()
    _state.active_scenario = ScenarioState(
        name="queue-consumer-failure", target_service_id="payment-worker", started_at=now
    )
    return ScenarioStartResponse(scenario="queue-consumer-failure", target_service_id="payment-worker", started_at=now)


@app.post("/scenarios/reset")
def reset_scenario() -> dict:
    _state.active_scenario = None
    return {"status": "reset"}


@app.post("/actions/{service_id}/restart", response_model=ActionResponse)
def restart_service(service_id: str) -> ActionResponse:
    _require_service(service_id)
    scenario = _state.active_scenario
    if scenario is None or scenario.target_service_id != service_id:
        return ActionResponse(service_id=service_id, action="restart", result="no-op: service already healthy")

    scenario.recovering_at = time.time()
    return ActionResponse(service_id=service_id, action="restart", result="restart accepted, draining backlog")
