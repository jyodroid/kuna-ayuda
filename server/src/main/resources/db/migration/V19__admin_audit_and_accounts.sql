-- Oversight console ("monitor the monitors"): an append-only audit trail of every moderator mutation
-- (who / what / before / after / ip), plus an `enabled` flag on admin_users so a rogue moderator can be
-- disabled (not just deleted) — which keeps their audit history intact. Read + revert are super-admin
-- only; the audit table is never edited except to stamp reverted_at/reverted_by when a change is undone.

ALTER TABLE admin_users ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS admin_audit (
    id           SERIAL PRIMARY KEY,
    actor_email  VARCHAR(160) NOT NULL,
    actor_role   VARCHAR(20)  NOT NULL,
    action       VARCHAR(40)  NOT NULL,   -- SHELTER_CREATE, BOARD_APPROVE, SOS_DELETE, LOGIN_SUCCESS, REVERT, ...
    entity_type  VARCHAR(40)  NOT NULL,   -- SHELTER, BOARD_POST, SOS, SEARCH, ADMIN, SESSION
    entity_id    VARCHAR(64),             -- nullable (e.g. login events)
    before_json  TEXT,                    -- entity snapshot before the action (for revert)
    after_json   TEXT,                    -- entity snapshot after the action
    ip           VARCHAR(64),
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    reverted_at  TIMESTAMP,               -- set when this change is undone
    reverted_by  VARCHAR(160)
);

CREATE INDEX IF NOT EXISTS idx_admin_audit_actor   ON admin_audit (actor_email);
CREATE INDEX IF NOT EXISTS idx_admin_audit_action  ON admin_audit (action);
CREATE INDEX IF NOT EXISTS idx_admin_audit_entity  ON admin_audit (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_admin_audit_created ON admin_audit (created_at);
