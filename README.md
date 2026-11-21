# Smooch — AI-Assisted Incident Command for SLA Protection

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![Python](https://img.shields.io/badge/Python-3.12-3776AB?logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-009688?logo=fastapi&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-pgvector-4169E1?logo=postgresql&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20RDS%20%7C%20S3-232F3E?logo=amazonaws&logoColor=white)
![Status](https://img.shields.io/badge/status-active%20development-yellow)

A personal, from-scratch engineering project that explores whether an AI-assisted
incident-command system can meaningfully reduce the avoidable latency and human
error in production incident response — without taking away human authority over
risky operational actions.

> This is a learning/portfolio project, not a product. It is explicitly **not**
> a ServiceNow, PagerDuty, or Datadog clone, not a generic AI chatbot platform,
> and not an autonomous DevOps agent. See [Scope](#scope) below.

---

## Table of contents

- [The problem](#the-problem)
- [The core idea](#the-core-idea)
- [Architecture](#architecture)
- [Tech stack](#tech-stack)
- [Current build status](#current-build-status)
- [AWS deployment](#aws-deployment)
- [Getting started (local)](#getting-started-local)
- [Project structure](#project-structure)
- [Scope](#scope)
- [Documentation](#documentation)

---

## The problem

Production incidents consume a finite SLA window. A meaningful share of that
window is often lost not to the failure itself, but to everything around it:
noticing the alert, finding the right production/service/owner, gathering logs
and metrics, searching for similar past incidents, deciding what to do, and
getting the right approval before acting.

**The question this project tries to answer:**

> Can an AI-assisted incident-command system reduce that avoidable
> investigation/coordination/decision latency and human error, while
> preserving human authority over high-risk production actions?

## The core idea

The system is built around **Adaptive Incident Reasoning** — for each part of
an investigation, it picks the cheapest, fastest, safest execution strategy
that can actually do the job:

```
Question                                    →  Strategy
──────────────────────────────────────────────────────────────────
Who owns Payment API?                       →  Database lookup
Is Payment API healthy right now?           →  Deterministic tool call
Summarize these logs                        →  Small local model (Ollama)
Have we seen this incident before?           →  Retrieval (pgvector)
What's the most likely root cause given
  logs + metrics + deployment history?      →  Stronger external model
Should we roll back production?             →  Policy + human approval
```

Known deterministic signals (is the service returning HTTP 500?) stay
deterministic — an LLM is only used where the problem is genuinely ambiguous,
contextual, or reasoning-heavy. A **Production Environment Simulator** provides
a small, realistic estate with reproducible failure scenarios, so the system's
diagnosis accuracy and response time can actually be measured against a known
baseline instead of just demoed.

## Architecture

```
 React + TypeScript (frontend)
            │
            ▼
 Java + Spring Boot (backend)
   · auth · RBAC · users/teams/productions
   · incidents · SLA policies · audit · persistence
            │
            ▼
 Python + FastAPI (ai-engine)
   · adaptive routing  · tool orchestration
   · retrieval/memory  · evaluation
            │
   ┌────────┼─────────────────┐
   ▼        ▼                 ▼
PostgreSQL  Model Gateway   Production Simulator
(+pgvector)   │                (FastAPI, scenario-based
            ┌─┴──────────┐      failure injection)
            ▼            ▼
         Ollama       OpenRouter
        (local)      (external)
```

**Why this split:** Spring Boot owns everything that's fundamentally
CRUD/RBAC/persistence — identity, teams, incidents, SLA policies, audit. Python
owns everything AI-heavy — routing, retrieval, tool orchestration, evaluation.
New feature work going forward follows that same line: Java for RBAC/CRUD/small
services, Python for the AI-reasoning surface.

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | [React](https://react.dev/) 19 + TypeScript + [Vite](https://vite.dev/) |
| Application backend | [Java 21](https://openjdk.org/) + [Spring Boot](https://spring.io/projects/spring-boot) 4 (Spring Data JPA, Flyway, Validation) |
| AI engine | [Python](https://www.python.org/) 3.12 + [FastAPI](https://fastapi.tiangolo.com/) + [Pydantic](https://docs.pydantic.dev/) + [httpx](https://www.python-httpx.org/) |
| Production simulator | FastAPI, scenario-based synthetic telemetry generator |
| Structured data | [PostgreSQL](https://www.postgresql.org/) + [pgvector](https://github.com/pgvector/pgvector) |
| Object storage | AWS S3 (local dev: [MinIO](https://min.io/)) |
| Local model tier | [Ollama](https://ollama.com/) (CPU-first, small quantized models) |
| External model tier | [OpenRouter](https://openrouter.ai/) (model-provider-agnostic gateway) |
| Cloud | [AWS](https://aws.amazon.com/) — EC2, RDS, S3, IAM (progressive adoption, see below) |

## Current build status

| Level | Capability | Status |
|---|---|---|
| 0 | Walking skeleton — all services health-chained | ✅ Done |
| 1 | Identity/RBAC domain (users, roles, teams, productions, assignments) | ✅ Done |
| 2 | Deterministic threshold-based incident detection | ✅ Done |
| 3 | Single-threaded AI investigation (sequential evidence + one model call) | ✅ Done |
| 4 | Tool registry, risk levels, human approval gate | ⏳ Not started |
| 5 | Concurrent evidence gathering (measured vs. Level 3 baseline) | ⏳ Not started |
| 6 | Retrieval/memory over past incidents (pgvector) | ⏳ Not started |
| 7 | Notification/escalation | ⏳ Not started |
| 8 | Observability (OpenTelemetry tracing) | ⏳ Not started |
| 9 | Evaluation (MTTA/MTTD/MTTR, SLA-breach rate, real baseline) | ⏳ Not started |
| 10 | Progressive AWS adoption | 🟡 In progress — IAM, EC2, S3, RDS provisioned and verified; Bedrock blocked at AWS's end, deferred |
| 11 | Scaling | Not anticipated unless 8/9 show a real bottleneck |

## AWS deployment

The application runs fully locally with Docker Compose — AWS is a deliberate,
**incremental** learning track layered on top, not a hard dependency for
day-to-day development:

- **IAM** — least-privilege, per-service scoped credentials (never the
  account's admin user for application work)
- **EC2** — hosts the Production Simulator as an independent process, so the
  incident system investigates something genuinely running separately from
  the application backend, not localhost
- **RDS (PostgreSQL + pgvector)** — the structured system of record once a
  cloud target is in play
- **S3** — incident report/evidence export artifacts
- **Amazon Bedrock** — planned as an additional model-gateway provider
  alongside Ollama/OpenRouter; not yet wired in

Each AWS service is adopted only once it answers a concrete question (can the
engine reliably invoke the model? what changes when Postgres moves off
localhost? what's the latency/cost delta?) — see the commit history on
`feature/aws-*` branches for how each piece was provisioned and verified.

## Getting started (local)

Requirements: Docker, Java 21, Python 3.12, Node 20+.

```bash
# 1. Shared infra (Postgres+pgvector, MinIO)
cp .env.example .env
docker compose up -d

# 2. Backend (Spring Boot) — local H2 by default, no Postgres required
cd backend
cp .env.example .env   # fill in if using the postgres profile
./mvnw spring-boot:run

# 3. AI engine (FastAPI)
cd ai-engine
cp .env.example .env
python -m venv .venv && .venv/Scripts/activate  # or source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8091

# 4. Production Simulator
cd simulator
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8090

# 5. Frontend
cd frontend
npm install
npm run dev
```

Default ports: frontend `5173`, backend `8080`, ai-engine `8091`, simulator
`8090`, Postgres `5433` (host), MinIO `9000`/`9001`.

## Project structure

```
backend/      Spring Boot application (RBAC, incidents, SLA, persistence)
ai-engine/    FastAPI AI orchestration service (routing, model gateway, tools)
simulator/    FastAPI Production Environment Simulator
frontend/     React + TypeScript UI
infra/        Provisioning scripts and non-secret infra manifests (Postgres init,
              AWS resource records under infra/aws/)
docs/         In-depth design docs and ADRs (kept local, not published — see below)
```

## Scope

This project is deliberately **not**:

- a ServiceNow, PagerDuty, or Datadog clone
- a full ITSM system or CMDB
- a generic enterprise chatbot or AI platform
- an autonomous DevOps agent that acts on production without approval
- a distributed/Kubernetes/Kafka system — none of that is justified yet

The goal is a **small, deep domain**: a focused incident-command system with
real engineering substance (RBAC, concurrency, observability, evaluation)
behind it, not a technology showcase.

## Documentation

Detailed design docs (product definition, architecture, data model, AI engine
design, evaluation strategy, ADRs for every significant decision) exist and are
kept thorough — they're just not published in this public repo, since they're
working planning notes rather than polished external documentation. This
README is written to stand on its own for anyone evaluating the project from
GitHub.
