-- Production/service catalog and per-production authorization --
-- see docs/23-incident-lifecycle-and-sla.md "Ownership model", docs/15-security-and-rbac.md.

CREATE TABLE production_environments (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE services (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    production_id   BIGINT NOT NULL REFERENCES production_environments(id) ON DELETE CASCADE,
    owning_team_id  BIGINT NOT NULL REFERENCES teams(id)
);

CREATE TABLE production_assignments (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    production_id  BIGINT NOT NULL REFERENCES production_environments(id) ON DELETE CASCADE,
    role           VARCHAR(50) NOT NULL,
    UNIQUE (user_id, production_id)
);
