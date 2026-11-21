"""Investigation request/response shapes -- see docs/09-ai-engine-architecture.md."""

from pydantic import BaseModel


class EvidenceSource(BaseModel):
    service_id: str
    source_type: str
    content: dict | list
    latency_ms: int
    available: bool = True


class InvestigationResult(BaseModel):
    service_id: str
    services_examined: list[str]
    diagnosis: str
    evidence: list[EvidenceSource]
    model: str
    provider: str
    tier: str
    total_latency_ms: int
