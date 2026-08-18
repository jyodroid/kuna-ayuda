-- Device-gated "claim / resolve" for board posts (#4). Regular users are anonymous, so ownership is
-- device-local: the server issues a random owner_secret when a post is created (manual posts only —
-- classified/PENDING posts have no device owner) and returns it once to the creator, who stores it
-- locally. Resolving a post requires presenting that secret. Nullable: classified posts and legacy
-- rows have none.
ALTER TABLE resource_posts ADD COLUMN owner_secret VARCHAR(64);
