-- Add client_ip column to liveness_sessions for session replay protection
ALTER TABLE liveness_sessions ADD COLUMN client_ip VARCHAR(45);
