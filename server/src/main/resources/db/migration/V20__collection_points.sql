-- Drop-off / collection points extracted from classified aid posts, stored as a JSON array of
-- {name, address, hours}. Kept as post content (not official map help points). Nullable; existing rows
-- read as an empty list. Added to both the live posts and the classify cache (which memoizes the AI
-- extraction) — the classify prompt version is bumped in the same change so old cache rows aren't reused.
ALTER TABLE resource_posts ADD COLUMN IF NOT EXISTS collection_points TEXT;
ALTER TABLE classify_cache  ADD COLUMN IF NOT EXISTS collection_points TEXT;
