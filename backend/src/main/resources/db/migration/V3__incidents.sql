-- Incident/SLA/investigation domain -- see docs/23-incident-lifecycle-and-sla.md,
-- docs/09-ai-engine-architecture.md.

CREATE TABLE incident_categories (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(255) NOT NULL UNIQUE,
    default_severity  VARCHAR(10) NOT NULL
);

CREATE TABLE sla_policies (
    id           BIGSERIAL PRIMARY KEY,
    severity     VARCHAR(10) NOT NULL UNIQUE,
    sla_minutes  INTEGER NOT NULL
);

CREATE TABLE incidents (
    id              BIGSERIAL PRIMARY KEY,
    service_id      BIGINT NOT NULL REFERENCES services(id),
    category_id     BIGINT NOT NULL REFERENCES incident_categories(id),
    severity        VARCHAR(10) NOT NULL,
    status          VARCHAR(30) NOT NULL,
    sla_policy_id   BIGINT NOT NULL REFERENCES sla_policies(id),
    sla_deadline_at TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    resolved_at     TIMESTAMPTZ
);

CREATE INDEX idx_incidents_service_id ON incidents(service_id);
CREATE INDEX idx_incidents_status ON incidents(status);

CREATE TABLE incident_events (
    id           BIGSERIAL PRIMARY KEY,
    incident_id  BIGINT NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    event_type   VARCHAR(50) NOT NULL,
    actor        VARCHAR(255) NOT NULL,
    payload      TEXT,
    created_at   TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_incident_events_incident_id ON incident_events(incident_id);

CREATE TABLE investigations (
    id                 BIGSERIAL PRIMARY KEY,
    incident_id        BIGINT NOT NULL UNIQUE REFERENCES incidents(id) ON DELETE CASCADE,
    status             VARCHAR(20) NOT NULL,
    diagnosis          TEXT,
    evidence_json      TEXT,
    services_examined  VARCHAR(500),
    model              VARCHAR(255),
    provider           VARCHAR(100),
    tier               VARCHAR(20),
    total_latency_ms   INTEGER,
    started_at         TIMESTAMPTZ NOT NULL,
    completed_at       TIMESTAMPTZ
);
