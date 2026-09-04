"""Level 3 investigation -- sequential evidence gathering + one model call.
See docs/09-ai-engine-architecture.md request lifecycle and
docs/03-scope-and-roadmap.md Level 3 (concurrency is deliberately Level 5,
built against this sequential baseline -- see docs/12-concurrency-and-efficiency.md).
"""

import time

import httpx
from pydantic import ValidationError

from app.exceptions import ModelUnavailableError
from app.repositories.simulator_repository import SimulatorRepository, get_simulator_repository
from app.schemas.generation import GenerationResult
from app.schemas.investigation import EvidenceSource, InvestigationResult
from app.services.model_gateway_service import ModelGatewayService, get_model_gateway_service


class InvestigationService:
    def __init__(self, simulator: SimulatorRepository, model_gateway: ModelGatewayService) -> None:
        self._simulator = simulator
        self._model_gateway = model_gateway

    async def investigate(self, service_id: str) -> InvestigationResult:
        start = time.perf_counter()

        dependencies = await self._discover_dependencies(service_id)
        services_examined = [service_id, *dependencies]

        # Sequential on purpose at Level 3 -- see docs/12-concurrency-and-efficiency.md
        # for why concurrency is deliberately introduced later, against this baseline.
        evidence: list[EvidenceSource] = []
        for sid in services_examined:
            evidence.append(await self._fetch_metrics(sid))
            evidence.append(await self._fetch_logs(sid))

        prompt = self._build_prompt(service_id, services_examined, evidence)

        try:
            generation: GenerationResult = await self._model_gateway.generate(prompt, tier="local")
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

    async def _discover_dependencies(self, service_id: str) -> list[str]:
        try:
            deps = await self._simulator.service_dependencies(service_id)
            return deps.depends_on
        except (httpx.HTTPError, ValidationError):
            return []

    async def _fetch_metrics(self, service_id: str) -> EvidenceSource:
        start = time.perf_counter()
        try:
            metrics = await self._simulator.service_metrics(service_id)
            return EvidenceSource(
                service_id=service_id,
                source_type="metrics",
                content=metrics.model_dump(),
                latency_ms=round((time.perf_counter() - start) * 1000),
            )
        except (httpx.HTTPError, ValidationError):
            return EvidenceSource(
                service_id=service_id, source_type="metrics", content={},
                latency_ms=round((time.perf_counter() - start) * 1000), available=False,
            )

    async def _fetch_logs(self, service_id: str) -> EvidenceSource:
        start = time.perf_counter()
        try:
            logs = await self._simulator.service_logs(service_id)
            return EvidenceSource(
                service_id=service_id,
                source_type="logs",
                content=logs.model_dump(),
                latency_ms=round((time.perf_counter() - start) * 1000),
            )
        except (httpx.HTTPError, ValidationError):
            return EvidenceSource(
                service_id=service_id, source_type="logs", content={},
                latency_ms=round((time.perf_counter() - start) * 1000), available=False,
            )

    def _build_prompt(self, service_id: str, services_examined: list[str], evidence: list[EvidenceSource]) -> str:
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


def get_investigation_service() -> InvestigationService:
    """FastAPI dependency factory -- see app/api/routes/*."""
    return InvestigationService(get_simulator_repository(), get_model_gateway_service())
