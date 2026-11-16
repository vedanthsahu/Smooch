# Incident Command AI — Reference

Personal portfolio/learning project (not the user's day job). Repo folder is
named `5_Hierarchy_AI` (historical — see "Naming history" below); GitHub
remote is `https://github.com/vedanthsahu/Smooch.git`. Product name:
**AI-Assisted Incident Command for SLA Protection**.

This file is a dense orientation doc for a peer engineer or another AI
picking this project back up — not a tutorial, not a resume artifact. For
deep detail on any topic, the canonical source is `docs/` (26 numbered docs
+ 15 ADRs, all gitignored except root `README.md` — deliberate, these are
personal planning notes, not meant to be public). `CLAUDE.md` at the repo
root is the existing orientation entry point for Claude Code sessions;
treat this file as a verified, code-grounded companion to it, written
2026-11-16.

## What it actually is

Production incidents eat a finite SLA window; a lot of that window is lost
to detection, context-gathering, investigation and coordination rather than
the actual fix. The system's thesis: reduce that avoidable latency via
**Adaptive Incident Reasoning** — picking the cheapest/fastest/safest
execution strategy per step (deterministic logic → tool → retrieval →
local model → external model → human escalation) — and prove the SLA-time
savings against a first-class **Production Environment Simulator** with
reproducible, scenario-based failures, rather than just demoing it.

Explicitly NOT: a ServiceNow/PagerDuty/Datadog clone, a full ITSM system, an
autonomous DevOps agent, or a generic chatbot platform.

**Naming history**: originally scoped as a generic "hierarchical AI
routing" chatbot platform. Pivoted to the incident-command framing on
2026-09-02 because routing-as-a-technique had no product reason to exist on
its own. Don't resurrect the old framing from git history — it predates
the current commit `206e1cd` (Level 0 skeleton) in spirit even though the
actual repo history only contains the post-pivot code.

**Why it exists**: the user wanted a portfolio project with real
engineering depth (Java/Spring Boot, React, Python AI engine, RBAC,
concurrency, observability, evaluation) instead of another thin
LLM-wrapper demo.

## Current build state (verified against code, 2026-11-16)

`CLAUDE.md`'s claim ("React, Spring Boot, Python engine/simulator, Ollama
integration, incident detection/investigation, PostgreSQL migrations, and
S3 code exist... OpenRouter remains a stub") **checks out and is accurate,
if anything slightly conservative** — the Python ai-engine is a real
layered app (`api/routes` → `services` → `repositories` → `schemas`), not
the thin stub a shallow directory listing suggests.

Mapped against the Level 0–11 roadmap in `docs/03-scope-and-roadmap.md`:

| Level | What it is | Status |
|---|---|---|
| 0 | Walking skeleton, 4 services health-chained | Done |
| 1 | Identity/RBAC domain (users/roles/teams/productions/services/assignments) | Done |
| 2 | Deterministic threshold-based detection, no LLM call | Done — `DetectionService.java` |
| 3 | Single-threaded AI investigation (sequential evidence gathering + one model call) | Done — `InvestigationService` (ai-engine) |
| 4 | Tool registry, risk levels, human approval gate | **Not started** |
| 5 | Concurrent evidence gathering (fan-out, measured vs Level 3 baseline) | **Not started** |
| 6 | Retrieval/memory (pgvector or Milvus) over past incidents | **Not started** — pgvector extension is enabled in Postgres (`infra/postgres/init/001-enable-pgvector.sql`) but nothing queries it yet |
| 7 | Notification/escalation | **Not started** |
| 8 | Observability (OpenTelemetry tracing) | **Not started** |
| 9 | Evaluation (MTTA/MTTD/MTTR, SLA-breach rate, real baseline) | **Not started** |
| 10 | Progressive AWS adoption | **In progress** — S3 and RDS-profile code exists (latest commit), AWS experiments not yet run |
| 11 | Scaling | Not anticipated unless 8/9 show a real bottleneck |

**Discrepancy worth flagging**: the product's headline mechanism —
*adaptive* tier selection (deterministic → tool → retrieval → local model →
external model → human) — is **not actually implemented as routing logic**.
`InvestigationService.investigate()` (ai-engine) hardcodes
`tier="local"` on every call; there is no decision logic choosing between
tiers yet. What *is* real and does embody the "deterministic beats AI"
principle: Java's `DetectionService` does pure threshold-based incident
creation with zero model calls, and only *then* triggers the Python
investigation pipeline. So the Java↔Python split is itself the first real
adaptive step (deterministic layer before AI layer); genuine multi-tier
routing (e.g. confidence-based escalation to an external model or to a
human) is still future work, consistent with `docs/20-open-questions-and-risks.md`
Q6 ("confidence-score threshold for triggering human escalation" —
explicitly unresolved, "must be tuned against Level 9 evaluation data, not
guessed in advance").

## Git / branch state

All branches are **linear, no divergence** — they're checkpoints along one
commit line, not parallel work:

```
206e1cd  added level 0 skeleton (simulator/ai-engine/backend/frontend health-chain)
adcbcc5  changed readme
c61197f  added level 1 identity/rbac + level 2 deterministic detection + level 3
         AI investigation pipeline w/ ollama — full flow verified e2e vs simulator
8f722a3  upgraded the python engine, implemented modular design
cabc5cc  initiating DB migration scripts and docker images
4987222  added service files and connection files for S3 and RDS   <- HEAD (feature/awsconfig, feature/migration)
```

`feature/awsconfig` and `feature/migration` are co-tipped at the latest
commit — effectively the current frontier. `feature/modular-wsl` is one
commit behind, `feature/ollama` two behind, `main` three behind (at the
Level 1–3 commit). Working tree is clean on `feature/awsconfig`. No CI —
`.github/` only contains a `modernize/java-upgrade` tooling hook scaffold,
not real GitHub Actions workflows.

**Working style**: the user reviews and commits everything themselves in
this repo (partly to actually understand it, partly for their GitHub
streak). Don't commit here unless explicitly asked.

## Topology — 4 services

```
React+TS frontend (Vite)  --->  Spring Boot backend (:8080)
                                      |  HTTP
                                      v
                              Python ai-engine (FastAPI, :8091)
                                      |  HTTP
                                      v
                              Python simulator (FastAPI, :8090)
```

- **frontend** (`frontend/src/`) — React+TS+Vite. `api/health.ts`,
  `api/incidents.ts`; components: `IncidentList`, `IncidentDetail`,
  `SlaCountdown`. Thin — matches Level 0–3 scope, no auth UI, no
  tool-approval UI yet (those are Level 1 SSO polish / Level 4 territory).
  `frontend/dist/` exists locally but is gitignored (`frontend/.gitignore`)
  — just a build artifact, not committed.
- **backend** (`backend/src/main/java/com/incidentcommand/backend/`) — Java
  21, Spring Boot **4.1.1** (not 3.x — check this before assuming common
  Spring 3 idioms). Packages: `identity/` (RBAC), `production/` (productions
  /services/assignments), `incident/` (detection, lifecycle, investigation
  orchestration, SLA, reporting), `storage/` (S3), `aiengine/` (HTTP client
  to the Python service), `simulator/` (HTTP client to the simulator),
  `config/`, `health/`.
- **ai-engine** (`ai-engine/app/`) — Python FastAPI, "Adaptive Incident
  Reasoning Engine" (its own `main.py` title, v0.4.0). Layered:
  `api/routes/` (health, generate, investigation) → `services/`
  (`investigation_service.py`, `model_gateway_service.py`) →
  `repositories/` (`ollama_repository.py` — real, calls `/api/generate`;
  `openrouter_repository.py` — stub, raises `NotImplementedError`;
  `simulator_repository.py`) → `schemas/`.
- **simulator** (`simulator/app/`) — Python FastAPI, "Production Simulator"
  v0.2.0. Pure in-memory synthetic state — **no real queues, no real
  per-service databases, by explicit design** (ADR-015, see Risk R2 below).
  Only Scenario A (`queue-consumer-failure`) is implemented; metrics/logs
  are computed live from elapsed time since scenario/recovery start, so
  state is reproducible by construction. Currently models 2 synthetic
  services (`payment-api` depends on `payment-worker`). Scenarios B–E
  (bad deployment, DB saturation, dependency timeout, intermittent
  failures) are documented in `docs/22-production-simulator.md` but not
  built.

## Data architecture

- **PostgreSQL + pgvector** (`pgvector/pgvector:pg16` via docker-compose,
  host port **5433** not 5432 — deliberately remapped because this machine
  already has a native Postgres bound to 5432 that was silently
  intercepting connections). pgvector extension is enabled
  (`infra/postgres/init/001-enable-pgvector.sql`) but nothing queries
  vectors yet (Level 6, unbuilt).
- **MinIO** (S3-compatible, local stand-in for AWS S3) also in
  docker-compose, ports 9000 (S3 API) / 9001 (console).
- **Two Spring profiles, cleanly separated**:
  - Default/local: file-based **H2** (`./data/incidentcommand`), Hibernate
    `ddl-auto=update`, Flyway disabled, H2 console on. Zero-setup local dev.
  - `postgres` profile: real Postgres via `DB_HOST`/`DB_PORT`/`DB_NAME`/
    `DB_USER`/`DB_PASSWORD` (from `backend/.env`, gitignored, loaded via
    spring-dotenv), `sslmode=require`, **Flyway owns the schema**
    (`spring.flyway.enabled=true`), Hibernate only validates
    (`ddl-auto=validate`), targets AWS RDS by default per
    `docs/19-aws-strategy.md` Phase 4.
  - Flyway migrations exist: `V1__identity.sql`, `V2__production.sql`,
    `V3__incidents.sql` (`backend/src/main/resources/db/migration/`).
- This resolves the **H2-for-local-dev blocker noted in earlier session
  memory as of 2026-09-02** — Docker + real Postgres + pgvector are now
  fully wired; H2 is a deliberate zero-setup *local* path, not a stand-in
  forced by missing Docker.
- `backend/data/incidentcommand.mv.db` / `.trace.db` — live local H2 files
  from actual dev runs, not committed artifacts to read into.

## Identity / RBAC

Three fixed global roles seeded idempotently on every startup
(`RbacSeeder.java`, a `CommandLineRunner`): **USER, OPERATOR, ADMIN**.
Permissions (also seeded, fixed list — no runtime creation yet):
`incidents.read`, `incidents.investigate`, `incidents.approve`,
`production.read`, `production.manage`, `teams.manage`, `users.manage`,
`roles.manage`, `sla_policy.manage`, `audit.read`, `analytics.read`. Real
role/permission *editing* is explicitly deferred to a later ADMIN UI
capability — right now the seeder is the only way roles/permissions get
created. Domain: `User`, `Role`, `Team`, `Permission` + controllers for
users/teams. **No Google SSO wired yet** despite being Level 1 scope per
`docs/03-scope-and-roadmap.md` — `docs/20-open-questions-and-risks.md`
explicitly flags "current authentication, production-scoped authorization,
approval enforcement... need acceptance evidence. Domain entities alone do
not close these gaps." Treat auth as **modeled but not enforced** until
verified otherwise.

## Incident domain

`ProductionEnvironment` → `ProductionService` → `ProductionAssignment`
(team/user ↔ service). `Incident` (with `IncidentCategory`, `Severity`,
`IncidentStatus`), `IncidentEvent` (timeline), `Investigation` (AI
investigation result persisted via `InvestigationPersistenceService`),
`SlaPolicy` (seed values **P1=30min, P2=90min, P3=240min**, explicitly
marked "illustrative" in code comments and in `docs/20-open-questions-and-risks.md`
Q5 — not finalized). `IncidentReportExportService` generates a report and
hands it to `S3StorageService`.

**Detection loop** (`DetectionService.java`, `@Scheduled` every
`incidentcommand.detection.poll-interval-ms` = 5000ms default): polls every
known `ProductionService`, asks the simulator for health, and:
- if unhealthy → `incidentLifecycleService.openIfNeeded()`, then triggers
  `investigationService.investigateAndPersist()` (async HTTP+model call,
  deliberately **not** run inside the detection method's transaction —
  see code comment on why detection is NOT `@Transactional` at this level).
- if healthy → `resolveIfOpen()`, then exports a report via S3.

Known simplification (documented in code): resolution fires on the
**first** healthy check rather than N consecutive ones (the full spec in
`docs/23-incident-lifecycle-and-sla.md` calls for a verification window) —
acceptable for Level 2/3, flagged to revisit if noisy in practice.

## AI investigation pipeline (ai-engine)

`InvestigationService.investigate(service_id)`:
1. Discover dependencies via simulator (`service_dependencies` endpoint).
2. For the service + each dependency, sequentially fetch metrics and logs
   from the simulator (deliberately sequential at Level 3 — see
   `docs/12-concurrency-and-efficiency.md`; concurrency is Level 5, built
   later *against this sequential baseline* so the performance claim is
   measured, not asserted).
3. Build a prompt instructing the model to name a root cause + one
   remediation action in 2–3 sentences, **"only use numbers that actually
   appear in the evidence... do not invent metrics"** — a real
   anti-hallucination guard in the prompt itself.
4. Call `ModelGatewayService.generate(prompt, tier="local")` → routes to
   `OllamaRepository` (hits `POST {ollama_base_url}/api/generate`, default
   model `llama3.2:1b`, configurable via `AI_ENGINE_OLLAMA_DEFAULT_MODEL`).
5. On model failure (`ModelUnavailableError`), the diagnosis degrades to a
   literal `"[diagnosis unavailable: ...]"` string rather than throwing —
   the investigation record still persists with evidence, just without a
   diagnosis. Matches the "evidence-only outcome for human investigation
   when inference is unavailable" mitigation in `docs/20-open-questions-and-risks.md` R3.

`ModelGatewayService` is the single swap point for local vs external
models — `OpenRouterRepository` exists as a real class (not a free
function) specifically so wiring the actual HTTP call later doesn't change
any call site, but today it immediately raises `ModelUnavailableError` if
no API key is configured, and `NotImplementedError` if one is. This is the
**single biggest unimplemented piece** blocking the "adaptive" part of the
product thesis — per `docs/20-open-questions-and-risks.md`, the next
concrete build step is "implement the existing OpenRouter stub first."

`generate.py` route (`POST /generate`) is a direct passthrough to the
Model Gateway for testing the gateway itself — explicitly not part of the
incident-investigation call path.

## Storage / AWS

`S3StorageService` (Java): thin wrapper over AWS SDK v2 `S3Client`. Failures
are **logged and swallowed, never thrown** — an S3 export is a nice-to-have
side effect and must never break the core incident lifecycle it's attached
to. Also fail-soft when S3 isn't configured at all
(`incidentcommand.s3.enabled=false` by default on the H2/local profile;
`${S3_ENABLED:true}` on the `postgres` profile, bucket from
`AWS_S3_BUCKET`, region `ap-south-1` default). S3 is explicitly scoped as
"artifacts only (exported reports, archives), never the system of record"
per `docs/08-data-architecture.md`.

RDS/EC2/Bedrock are all still **optional Level 10 experiments**, gated on
local correctness first per `docs/18-deployment-strategy.md` — "AWS phases
begin only after the corresponding local piece is done, never in parallel
with unfinished core logic" (Risk R9). Known historical blockers (Google
OAuth creds, OpenRouter/AgentRouter key, AWS account setup, Docker) — the
**Docker/Postgres blocker is resolved** (docker-compose + pgvector image
exist and work); Google OAuth and OpenRouter key are still open per the
auth and model-gateway state above. AWS account: $100 credit / 161-day
window as of 2026-09-02, region `ap-south-1` — treat as stale, re-verify
current balance/expiry before assuming it's still available (per
`docs/20-open-questions-and-risks.md`'s own instruction not to treat
historical balances as current facts).

## Working agreement / engineering principles (from `CLAUDE.md`, verified against code)

- *"Every technology must earn its place"* — e.g. Redis (`ADR-005`) is
  explicitly **not adopted**; `docs/20-open-questions-and-risks.md` Q9 says
  "drop from Level 0-7 scope; revisit only if a real external-API
  rate-limit problem appears." Milvus (`ADR-006`) likewise unused — pgvector
  was chosen instead (consistent with the enabled-but-unused pgvector
  extension found in `infra/`).
- *"Deterministic engineering beats AI wherever the signal is already
  known"* — concretely real: `DetectionService` is 100% threshold logic,
  zero model calls, confirmed in code and comments.
- *"No production-affecting action ever executes without human approval in
  the MVP"* — **not yet exercised**, because there's no action/tool-calling
  layer at all yet (that's Level 4, unbuilt). The principle is stated and
  presumably will gate Level 4's `restart_service`-style write tool, but
  nothing currently executes any production-affecting action, safely or
  otherwise.
- *"Do not fabricate evaluation numbers / SLA-protected claims"* — no
  evaluation harness exists yet (Level 9), so this is a forward constraint,
  not something to check against current behavior. The anti-hallucination
  instruction baked directly into the investigation prompt (step 3 above)
  is the closest current enforcement.

## Biggest risks worth remembering (from `docs/20-open-questions-and-risks.md`)

- **R2**: simulator scope creep toward real infrastructure is the single
  biggest concrete risk — no real queues/DBs, ever, without a new ADR.
- **R5/R7**: the project's headline claim is SLA-time-saved; a fabricated
  or hand-waved "typical human MTTR" baseline would be the most damaging
  credibility gap in the whole project, since the entire thesis rests on
  that number. No baseline protocol has been run yet (Level 9 territory).
- **R6**: a model's self-reported confidence score must never be treated as
  calibrated/meaningful without separate measurement — relevant once
  confidence scores appear in the UI (not yet built).
- **Q4**: service-to-service auth between Spring Boot ↔ Python ↔ Simulator
  is still an open question — suggested default is a shared-secret header
  for local/EC2-phase dev, not yet implemented as far as this review found.

## How to resume work here

1. Read `CLAUDE.md`, then `docs/20-open-questions-and-risks.md` (the
   project's own instruction: re-read this one every session).
2. Check `docs/25-build-blueprint.md` for the current recommended execution
   order — as of the last doc update, next steps are: complete the safe
   local workflow, implement the OpenRouter stub, measure routing/
   concurrency, add scoped memory/scenarios, then optional AWS experiments.
3. Don't commit without being asked. Don't silently implement a `Proposed`
   ADR that has real cost/scope consequences.
4. If picking up AWS work: verify current credit balance/expiry first,
   don't trust the 2026-09-02 figures above.
