-- "I'm safe" check-ins become a public reassurance list, so they need a name to show publicly.
-- (The country column already exists from V10__multi_country.sql, default 'CO'.)
ALTER TABLE sos_reports ADD COLUMN display_name VARCHAR(120);

-- Supports the public per-country safe list: SAFE rows with a name, newest first.
CREATE INDEX IF NOT EXISTS idx_sos_country_status_created ON sos_reports (country, status, created_at);
