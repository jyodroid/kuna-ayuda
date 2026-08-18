-- Lost & Found / reunification board (pets + people) with photos.
--
-- `photos` stores uploaded images as bytea — durable without external object storage (Heroku's
-- filesystem is ephemeral). Fine for MVP volume; S3/GCS is the scale path. `search_reports` are
-- community-submitted and published directly (no moderation, like the aid board); a moderator can
-- close abusive entries. `state` = LOST|FOUND, `status` = ACTIVE|CLOSED lifecycle.

CREATE TABLE photos (
    id           SERIAL PRIMARY KEY,
    content_type VARCHAR(40) NOT NULL,   -- image/jpeg | image/png | image/webp
    data         BYTEA       NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE search_reports (
    id            SERIAL PRIMARY KEY,
    subject       VARCHAR(10)  NOT NULL,                    -- PET | PERSON
    state         VARCHAR(10)  NOT NULL,                    -- LOST | FOUND
    title         VARCHAR(160) NOT NULL,                    -- pet name / person name or alias
    description   TEXT         NOT NULL DEFAULT '',
    last_seen     VARCHAR(160) NOT NULL,                    -- region / municipality
    notes         TEXT,
    contact_phone VARCHAR(40)  NOT NULL,
    contact_name  VARCHAR(120),
    photo_id      INTEGER      REFERENCES photos(id) ON DELETE SET NULL,
    country       VARCHAR(2)   NOT NULL DEFAULT 'CO',
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',   -- ACTIVE | CLOSED
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_search_reports_filter ON search_reports (country, subject, status);
