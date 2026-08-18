-- Responder lifecycle for SOS/SAFE reports: mark a report attended (SOS) / notified (SAFE) so it
-- leaves the active responder list (soft-archive — reversible, keeps an audit trail). handled_at NULL
-- means still pending/active; a timestamp means archived. handled_by records the moderator (JWT email).

ALTER TABLE sos_reports ADD COLUMN handled_at  TIMESTAMP;
ALTER TABLE sos_reports ADD COLUMN handled_by  VARCHAR(120);

-- The responder view queries by (status, still-pending) and orders by recency; index the pending path.
CREATE INDEX idx_sos_handled ON sos_reports (handled_at, status, created_at DESC);
