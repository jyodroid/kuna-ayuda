-- Peer-to-peer mutual-aid board: people REQUEST resources (water, food, medicine…) or OFFER them.
-- Unlike shelters, these are user-submitted. Guardrails: server-side validation + rate limiting,
-- a status for moderation (ACTIVE/CLOSED), and admin removal. The app labels these as unverified.

CREATE TABLE resource_posts (
    id             SERIAL PRIMARY KEY,
    kind           VARCHAR(20)  NOT NULL,            -- REQUEST | OFFER
    resource_type  VARCHAR(30)  NOT NULL,            -- WATER | FOOD | MEDICINE | SHELTER | HYGIENE | OTHER
    region         VARCHAR(120) NOT NULL,
    description    TEXT         NOT NULL DEFAULT '',
    contact_phone  VARCHAR(40)  NOT NULL,
    contact_name   VARCHAR(120),
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | CLOSED
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_board_kind_status ON resource_posts (kind, status);
CREATE INDEX idx_board_region ON resource_posts (region);
