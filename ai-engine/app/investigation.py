"""Level 3 investigation -- sequential evidence gathering + one model call.
See docs/09-ai-engine-architecture.md request lifecycle and
docs/03-scope-and-roadmap.md Level 3 (concurrency is deliberately Level 5,
built against this sequential baseline -- see docs/12-concurrency-and-efficiency.md).
"""

import time

import httpx
from pydantic import BaseModel

from app.config import settings
from app.model_gateway import GenerationResult, ModelUnavailableError, generate


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


async def _fetch_evidence(client: httpx.AsyncClient, service_id: str, source_type: str, path: str) -> EvidenceSource:
    start = time.perf_counter()
    try:
        response = await client.get(f"{settings.simulator_base_url}{path}")
        response.raise_for_status()
        content = response.json()
        return EvidenceSource(
            service_id=service_id,
            source_type=source_type,
            content=content,
            latency_ms=round((time.perf_counter() - start) * 1000),
        )
    except httpx.HTTPError:
        return EvidenceSource(
            service_id=service_id,
            source_type=source_type,
            content={},
            latency_ms=round((time.perf_counter() - start) * 1000),
            available=False,
        )


async def _discover_dependencies(client: httpx.AsyncClient, service_id: str) -> list[str]:
    try:
        response = await client.get(f"{settings.simulator_base_url}/services/{service_id}/dependencies")
        response.raise_for_status()
        return response.json().get("depends_on", [])
    except httpx.HTTPError:
        return []


def _build_prompt(service_id: str, services_examined: list[str], evidence: list[EvidenceSource]) -> str:
    evidence_text = "\n".join(
        f"- [{e.service_id}] {e.source_type} ({'unavailable' if not e.available else 'ok'}): {e.content}"
        for e in evidence
    )
    dependency_note = (
        f" Its dependency was also examined: {', '.join(s for s in services_examined if s != service_id)}."
        if len(services_examined) > 1
        else ""
    )
    return (
        "You are assisting with a production incident investigation. "
        f"Service '{service_id}' is showing abnormal behavior.{dependency_note}\n\n"
        f"Evidence gathered:\n{evidence_text}\n\n"
        "In 2-3 short sentences: state the most likely root cause (name the specific "
        "service it originates in), and recommend one concrete remediation action. "
        "Only use numbers that actually appear in the evidence above -- do not invent metrics."
    )


async def investigate(service_id: str) -> InvestigationResult:
    start = time.perf_counter()

    async with httpx.AsyncClient(timeout=10.0) as client:
        dependencies = await _discover_dependencies(client, service_id)
        services_examined = [service_id, *dependencies]

        # Sequential on purpose at Level 3 -- see docs/12-concurrency-and-efficiency.md
        # for why concurrency is deliberately introduced later, against this baseline.
        evidence: list[EvidenceSource] = []
        for sid in services_examined:
            evidence.append(await _fetch_evidence(client, sid, "metrics", f"/services/{sid}/metrics"))
            evidence.append(await _fetch_evidence(client, sid, "logs", f"/services/{sid}/logs"))

    prompt = _build_prompt(service_id, services_examined, evidence)

    try:
        generation: GenerationResult = await generate(prompt, tier="local")
        diagnosis = generation.text.strip()
    except ModelUnavailableError as exc:
        diagnosis = f"[diagnosis unavailable: {exc}]"
        generation = GenerationResult(text="", provider="none", model="none", tier="local", latency_ms=0)

    total_latency_ms = round((time.perf_counter() - start) * 1000)
    return InvestigationResult(
        service_id=service_id,
        services_examined=services_examined,
        diagnosis=diagnosis,
        evidence=evidence,
        model=generation.model,
        provider=generation.provider,
        tier=generation.tier,
        total_latency_ms=total_latency_ms,
    )
