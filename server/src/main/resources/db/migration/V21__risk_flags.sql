-- Phase B: moderator-facing risk flags extracted by the classify AI (scam / unverified-claim / no-source).
-- A signal only (never an auto-reject). Stored as a JSON array of flag codes, same shape as collection_points.
ALTER TABLE resource_posts ADD COLUMN IF NOT EXISTS risk_flags TEXT;
ALTER TABLE classify_cache ADD COLUMN IF NOT EXISTS risk_flags TEXT;
