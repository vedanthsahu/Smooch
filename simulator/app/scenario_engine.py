"""Scenario A -- queue consumer failure. See docs/22-production-simulator.md.

Deliberately NOT real infrastructure: no real queue, no real process to kill.
Metrics/logs are computed live from elapsed time since a scenario/recovery
started, so state is reproducible by construction (same elapsed time ->
same numbers) rather than depending on anything actually running.
"""

import time
from dataclasses import dataclass, field
from typing import Literal

ServiceStatus = Literal["healthy", "degraded", "down", "recovering"]

RAMP_SECONDS = 60.0          # time for the failure to reach max severity
RECOVERY_SECONDS = 20.0      # time for a restarted consumer to drain the backlog
MAX_QUEUE_DEPTH = 500
BASELINE_LATENCY_MS = 50
BASELINE_ERROR_RATE = 0.01
MAX_LATENCY_MS = 4000
MAX_ERROR_RATE = 0.85


@dataclass
class ScenarioState:
    name: str
    target_service_id: str
    started_at: float
    recovering_at: float | None = None


@dataclass
class SimulatorState:
    active_scenario: ScenarioState | None = None
    known_services: dict[str, str] = field(default_factory=dict)  # service_id -> production


def _clamp01(x: float) -> float:
    return max(0.0, min(1.0, x))


def _queue_depth(scenario: ScenarioState, now: float) -> float:
    if scenario.recovering_at is not None:
        depth_at_recovery_start = _queue_depth_raw(scenario, scenario.recovering_at)
        elapsed_recovery = now - scenario.recovering_at
        fraction_drained = _clamp01(elapsed_recovery / RECOVERY_SECONDS)
        return depth_at_recovery_start * (1 - fraction_drained)
    return _queue_depth_raw(scenario, now)


def _queue_depth_raw(scenario: ScenarioState, now: float) -> float:
    elapsed = now - scenario.started_at
    fraction = _clamp01(elapsed / RAMP_SECONDS)
    return MAX_QUEUE_DEPTH * fraction


def compute_worker_status(scenario: ScenarioState | None, now: float) -> ServiceStatus:
    if scenario is None:
        return "healthy"
    if scenario.recovering_at is not None:
        depth = _queue_depth(scenario, now)
        return "recovering" if depth > 1 else "healthy"
    return "down"


def compute_dependent_metrics(scenario: ScenarioState | None, now: float) -> dict:
    """Metrics for a service downstream of the failing consumer (e.g. payment-api)."""
    if scenario is None:
        return {
            "latency_ms": BASELINE_LATENCY_MS,
            "error_rate": BASELINE_ERROR_RATE,
            "queue_depth": 0,
        }
    depth = _queue_depth(scenario, now)
    severity = _clamp01(depth / MAX_QUEUE_DEPTH)
    return {
        "latency_ms": round(BASELINE_LATENCY_MS + severity * (MAX_LATENCY_MS - BASELINE_LATENCY_MS)),
        "error_rate": round(BASELINE_ERROR_RATE + severity * (MAX_ERROR_RATE - BASELINE_ERROR_RATE), 3),
        "queue_depth": round(depth),
    }


def compute_worker_metrics(scenario: ScenarioState | None, now: float) -> dict:
    if scenario is None:
        return {"queue_depth": 0, "consumer_status": "running"}
    depth = _queue_depth(scenario, now)
    consumer_status = "stopped" if scenario.recovering_at is None else "restarting"
    if scenario.recovering_at is not None and depth <= 1:
        consumer_status = "running"
    return {"queue_depth": round(depth), "consumer_status": consumer_status}


def synthetic_logs(service_id: str, scenario: ScenarioState | None, now: float) -> list[str]:
    if scenario is None or scenario.target_service_id != service_id:
        return [f"INFO {service_id} heartbeat ok"]

    status = compute_worker_status(scenario, now)
    if status == "down":
        return [
            f"ERROR {service_id} consumer thread exited unexpectedly: connection reset",
            f"WARN {service_id} queue backlog increasing, no active consumer",
        ]
    if status == "recovering":
        return [
            f"INFO {service_id} consumer restarted, draining backlog",
        ]
    return [f"INFO {service_id} heartbeat ok"]
