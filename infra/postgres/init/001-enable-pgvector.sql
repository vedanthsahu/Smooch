-- Runs once, on first container init (docker-entrypoint-initdb.d convention).
-- See docs/adr/ADR-006-milvus.md -- pgvector is the default vector store.
CREATE EXTENSION IF NOT EXISTS vector;
