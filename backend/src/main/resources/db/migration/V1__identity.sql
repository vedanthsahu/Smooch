-- Identity/RBAC domain -- see docs/15-security-and-rbac.md, docs/08-data-architecture.md.

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    google_sub    VARCHAR(255) UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    display_name  VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL
);

CREATE TABLE roles (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE permissions (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE teams (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id  BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permissions (
    role_id        BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id  BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE team_memberships (
    user_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    team_id  BIGINT NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, team_id)
);
