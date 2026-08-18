-- Ingested, normalized humanitarian data from external APIs (see server/upstream/GdacsSource +
-- ReliefWebSource). These are pulled periodically by DisasterIngestionService and UPSERTed by the
-- (source, external_id) natural key, so re-fetching the same event/report updates it in place instead
-- of duplicating. `fetched_at` records when ingestion last saw the row (staleness / "last updated").
--
-- Design notes:
--  * These tables are AUTOMATED sources (events + reports). Help points ("where do I take aid today?")
--    intentionally stay on the moderated-submission path (shelters table) — they change hour by hour
--    and can't come from these feeds.
--  * Timestamps are stored as TIMESTAMP (UTC); the API layer exposes them as epoch millis.

CREATE TABLE disasters (
    id            SERIAL PRIMARY KEY,
    source        VARCHAR(20)  NOT NULL,            -- 'GDACS'
    external_id   VARCHAR(64)  NOT NULL,            -- GDACS eventid
    event_type    VARCHAR(20)  NOT NULL,            -- 'EQ'
    title         VARCHAR(300) NOT NULL,
    description   TEXT,
    country       VARCHAR(120),
    iso3          VARCHAR(3),
    latitude      DOUBLE PRECISION,
    longitude     DOUBLE PRECISION,
    magnitude     DOUBLE PRECISION,                 -- severitydata.severity
    alert_level   VARCHAR(20),                      -- Green / Orange / Red
    severity_text VARCHAR(200),                     -- e.g. "Magnitude 7.4M, Depth:10km"
    event_date    TIMESTAMP,                        -- GDACS fromdate (UTC)
    url           VARCHAR(500),                     -- GDACS report/details link
    fetched_at    TIMESTAMP    NOT NULL,
    CONSTRAINT uq_disasters_source_ext UNIQUE (source, external_id)
);

CREATE INDEX idx_disasters_event_date ON disasters (event_date DESC);

CREATE TABLE disaster_reports (
    id            SERIAL PRIMARY KEY,
    source        VARCHAR(20)  NOT NULL,            -- 'ReliefWeb'
    external_id   VARCHAR(64)  NOT NULL,            -- ReliefWeb node id
    title         VARCHAR(500) NOT NULL,
    body          TEXT,                             -- body-html excerpt
    org_source    VARCHAR(200),                     -- publishing/responding org (source[0].name)
    country       VARCHAR(120),                     -- primary_country.name
    disaster_type VARCHAR(80),                      -- disaster_type[0].name
    url           VARCHAR(500),
    published_at  TIMESTAMP,                        -- date.created
    fetched_at    TIMESTAMP    NOT NULL,
    CONSTRAINT uq_reports_source_ext UNIQUE (source, external_id)
);

CREATE INDEX idx_reports_published_at ON disaster_reports (published_at DESC);
