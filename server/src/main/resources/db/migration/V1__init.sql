-- Moderated shelters / aid-collection points (acopios).
-- Only administrators write to this table (via authenticated endpoints); the app reads it.
-- This is the anti-fraud guarantee: the public cannot add or edit locations.

CREATE TABLE shelters (
    id             SERIAL PRIMARY KEY,
    name           VARCHAR(200)  NOT NULL,
    type           VARCHAR(40)   NOT NULL,            -- ACOPIO | ALBERGUE | SALUD | AGUA
    address        VARCHAR(300)  NOT NULL,
    latitude       DOUBLE PRECISION NOT NULL,
    longitude      DOUBLE PRECISION NOT NULL,
    accepts        TEXT          NOT NULL DEFAULT '', -- what they receive / provide
    hours          VARCHAR(200),
    contact_phone  VARCHAR(40),
    verified       BOOLEAN       NOT NULL DEFAULT TRUE,
    last_verified  DATE,
    active         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_shelters_active ON shelters (active);

-- Example / illustrative entries. Replace with official UNGRD / municipal data before release.
INSERT INTO shelters (name, type, address, latitude, longitude, accepts, hours, contact_phone, verified, last_verified)
VALUES
 ('Centro de acopio Movistar Arena', 'ACOPIO', 'Diag. 61C #26-36, Bogotá', 4.6486, -74.0778,
  'Agua, alimentos no perecederos, kits de aseo', '8:00–18:00', '123', TRUE, CURRENT_DATE),
 ('Albergue temporal Unidad Deportiva Atanasio Girardot', 'ALBERGUE', 'Cra. 74 #48-10, Medellín', 6.2560, -75.5900,
  'Personas evacuadas; frazadas, colchonetas', '24 h', '144', TRUE, CURRENT_DATE),
 ('Punto de hidratación Plaza de Cayzedo', 'AGUA', 'Cra. 5 #11-00, Cali', 3.4516, -76.5320,
  'Entrega de agua potable', '7:00–19:00', '132', TRUE, CURRENT_DATE);
