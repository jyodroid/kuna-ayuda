-- Geolocated SOS reports and "I'm safe" check-ins.
-- status = SOS (needs help, ideally with coordinates) or SAFE (person is OK).
-- Public submission (someone in danger), rate-limited; a responder view lists active SOS.

CREATE TABLE sos_reports (
    id             SERIAL PRIMARY KEY,
    status         VARCHAR(10)      NOT NULL,        -- SOS | SAFE
    latitude       DOUBLE PRECISION,
    longitude      DOUBLE PRECISION,
    region         VARCHAR(120),
    message        TEXT,
    contact_phone  VARCHAR(40),
    created_at     TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sos_status_created ON sos_reports (status, created_at DESC);
