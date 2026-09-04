"""Shapes returned by the Production Simulator -- see docs/22-production-simulator.md.
Kept minimal/loose (dict payloads) since the simulator's metrics/log shapes are
scenario-specific and not yet stable enough to lock into strict schemas."""

from pydantic import BaseModel


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


class ServiceDependencies(BaseModel):
    service_id: str
    depends_on: list[str]
