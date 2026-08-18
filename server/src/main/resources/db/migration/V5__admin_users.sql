-- Moderator accounts. This is the ONLY table that grants a JWT: regular app users (SOS, board
-- posts, quakes, shelters) stay fully anonymous — they never authenticate. Only moderators log in,
-- and login is the sole path to an ADMIN token, which the admin-only board routes (/pending,
-- /approve, DELETE) already require via requireAdmin(). Passwords are bcrypt-hashed (jbcrypt);
-- the plaintext is never stored. Seed the first moderator with ADMIN_EMAIL/ADMIN_PASSWORD env vars.

CREATE TABLE admin_users (
    id            SERIAL PRIMARY KEY,
    email         VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,   -- bcrypt hash (60 chars); never the plaintext
    role          VARCHAR(20)  NOT NULL DEFAULT 'ADMIN',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);
