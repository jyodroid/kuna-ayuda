-- Multi-country support. The app now serves Colombia (CO), Indonesia (ID) and Spain (ES). Every
-- moderated/user-generated row gets a `country` ISO-3166 alpha-2 code so the client can request a
-- single country's data (`GET /api/shelters?country=ID`, `GET /api/board?country=ES`).
--
-- All existing rows are Colombian, so we backfill 'CO' via the column default. SOS keeps a country
-- column for future scoping but the responder view stays global (emergencies aren't filtered).

ALTER TABLE shelters       ADD COLUMN country VARCHAR(2) NOT NULL DEFAULT 'CO';
ALTER TABLE resource_posts ADD COLUMN country VARCHAR(2) NOT NULL DEFAULT 'CO';
ALTER TABLE sos_reports    ADD COLUMN country VARCHAR(2) NOT NULL DEFAULT 'CO';

CREATE INDEX idx_shelters_country       ON shelters (country);
CREATE INDEX idx_resource_posts_country ON resource_posts (country);
