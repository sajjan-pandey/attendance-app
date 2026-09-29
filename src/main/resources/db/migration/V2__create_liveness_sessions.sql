CREATE TABLE IF NOT EXISTS liveness_sessions (
    session_id VARCHAR(36) PRIMARY KEY,
    user_code VARCHAR(80) NOT NULL,
    sequence TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_liveness_sessions_created_at
    ON liveness_sessions (created_at);
