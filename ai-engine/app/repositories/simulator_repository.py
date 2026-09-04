"""All HTTP access to the Production Simulator lives here -- nothing else in
the codebase should import httpx and talk to the simulator directly. See
docs/22-production-simulator.md.
"""

import httpx

from app.config import settings
from app.schemas.simulator import ServiceDependencies, ServiceHealth, ServiceLogs, ServiceMetrics


class SimulatorRepository:
    def __init__(self, base_url: str | None = None, timeout: float = 10.0) -> None:
        self._base_url = base_url or settings.simulator_base_url
        self._timeout = timeout

    async def health(self) -> dict:
        async with httpx.AsyncClient(timeout=self._timeout) as client:
            response = await client.get(f"{self._base_url}/health")
            response.raise_for_status()
            return response.json()

    async def service_health(self, service_id: str) -> ServiceHealth:
        async with httpx.AsyncClient(timeout=self._timeout) as client:
            response = await client.get(f"{self._base_url}/services/{service_id}/health")
            response.raise_for_status()
            return ServiceHealth.model_validate(response.json())

    async def service_metrics(self, service_id: str) -> ServiceMetrics:
        async with httpx.AsyncClient(timeout=self._timeout) as client:
            response = await client.get(f"{self._base_url}/services/{service_id}/metrics")
            response.raise_for_status()
            return ServiceMetrics.model_validate(response.json())

    async def service_logs(self, service_id: str) -> ServiceLogs:
        async with httpx.AsyncClient(timeout=self._timeout) as client:
            response = await client.get(f"{self._base_url}/services/{service_id}/logs")
            response.raise_for_status()
            return ServiceLogs.model_validate(response.json())

    async def service_dependencies(self, service_id: str) -> ServiceDependencies:
        async with httpx.AsyncClient(timeout=self._timeout) as client:
            response = await client.get(f"{self._base_url}/services/{service_id}/dependencies")
            response.raise_for_status()
            return ServiceDependencies.model_validate(response.json())


def get_simulator_repository() -> SimulatorRepository:
    """FastAPI dependency factory -- see app/api/routes/*."""
    return SimulatorRepository()
