"""Adaptive Incident Reasoning engine -- see docs/09-ai-engine-architecture.md.

App wiring only. Route handlers live in app/api/routes/, business logic in
app/services/, external HTTP access in app/repositories/, data shapes in
app/schemas/ -- see each package for its responsibility.
"""

from fastapi import FastAPI

from app.api.routes import generate, health, investigation

app = FastAPI(title="Adaptive Incident Reasoning Engine", version="0.4.0")

app.include_router(health.router)
app.include_router(generate.router)
app.include_router(investigation.router)
