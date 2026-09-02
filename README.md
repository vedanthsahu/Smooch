# Incident Command AI

AI-assisted incident command for SLA protection. Production incidents consume a finite SLA window; this project explores whether adaptive AI routing (deterministic logic → tools → retrieval → local model → external model → human) can reduce avoidable investigation and coordination latency, against a controlled, reproducible Production Simulator — while keeping humans in control of anything that touches production.

Full design rationale, ADRs, and open questions live in `docs/` locally (not tracked in git — internal planning material).

## Architecture

```
frontend (React + TS, Vite)
   -> backend (Java, Spring Boot)
        -> ai-engine (Python, FastAPI)
             -> simulator (Python, FastAPI)
```

Each service owns a distinct responsibility — see `docs/07-system-architecture.md` locally for the full rationale. Nothing here is production infrastructure: the simulator is a synthetic telemetry generator (`docs/22-production-simulator.md`), not real services.

## Current status

**Level 0 walking skeleton.** Each service exposes a health endpoint; `backend` → `ai-engine` → `simulator` is proven to chain end-to-end. No persistence, no auth, no incident/AI logic yet. See `docs/03-scope-and-roadmap.md` for what's next.

## Running locally

Each service runs independently; start them in this order (each depends on the previous being reachable):

### 1. Simulator (port 8090)
```
cd simulator
python -m venv .venv
./.venv/Scripts/pip install -r requirements.txt      # .venv/bin/pip on macOS/Linux
./.venv/Scripts/python -m uvicorn app.main:app --port 8090
```

### 2. AI Engine (port 8091)
```
cd ai-engine
python -m venv .venv
./.venv/Scripts/pip install -r requirements.txt
./.venv/Scripts/python -m uvicorn app.main:app --port 8091
```

### 3. Backend (port 8080)
```
cd backend
./mvnw spring-boot:run
```

### 4. Frontend (port 5173)
```
cd frontend
npm install
npm run dev
```

Then open `http://localhost:5173` and click "Check system health" — it calls `backend` → `ai-engine` → `simulator` and shows the status of each.

## Stack

React + TypeScript · Java + Spring Boot · Python (FastAPI) · PostgreSQL (planned) · AWS (progressive adoption, see `docs/19-aws-strategy.md` locally).
