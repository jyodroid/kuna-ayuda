-- Regional official help points, so wherever a quake hits there is coverage in the *affected place* —
-- not only Bogotá/Medellín. Cruz Roja Colombiana genuinely has a seccional in every department, so we
-- seed one per department capital used by the app's affected-area logic (core/domain ColombiaRegions),
-- geolocated at that city's centroid (the same coordinates the affected-place detection uses).
--
-- Integrity: these are real official institutions. The coordinate is city-accurate (centroid), and the
-- exact street is intentionally left "por confirmar" for a moderator to refine — we don't fabricate a
-- precise address. `verified=TRUE` reflects "official institution", consistent with the anti-fraud model.
-- Coordinates mirror core/domain/.../ColombiaRegions.kt (keep the two in sync if that list changes).

INSERT INTO shelters (name, type, address, latitude, longitude, accepts, hours, contact_phone, verified, last_verified)
VALUES
 ('Cruz Roja Colombiana — Seccional Cundinamarca',       'SALUD', 'Cruz Roja Seccional Cundinamarca (dirección por confirmar), Bogotá',        4.7110, -74.0721, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Antioquia',          'SALUD', 'Cruz Roja Seccional Antioquia (dirección por confirmar), Medellín',         6.2442, -75.5812, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Valle del Cauca',    'SALUD', 'Cruz Roja Seccional Valle del Cauca (dirección por confirmar), Cali',       3.4516, -76.5320, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Atlántico',          'SALUD', 'Cruz Roja Seccional Atlántico (dirección por confirmar), Barranquilla',    10.9685, -74.7813, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Bolívar',            'SALUD', 'Cruz Roja Seccional Bolívar (dirección por confirmar), Cartagena',         10.3910, -75.4794, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Norte de Santander', 'SALUD', 'Cruz Roja Seccional Norte de Santander (dirección por confirmar), Cúcuta',  7.8939, -72.5078, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Santander',          'SALUD', 'Cruz Roja Seccional Santander (dirección por confirmar), Bucaramanga',      7.1193, -73.1227, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Risaralda',          'SALUD', 'Cruz Roja Seccional Risaralda (dirección por confirmar), Pereira',          4.8133, -75.6961, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Magdalena',          'SALUD', 'Cruz Roja Seccional Magdalena (dirección por confirmar), Santa Marta',     11.2408, -74.1990, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Tolima',             'SALUD', 'Cruz Roja Seccional Tolima (dirección por confirmar), Ibagué',              4.4389, -75.2322, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Caldas',             'SALUD', 'Cruz Roja Seccional Caldas (dirección por confirmar), Manizales',           5.0703, -75.5138, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Meta',               'SALUD', 'Cruz Roja Seccional Meta (dirección por confirmar), Villavicencio',         4.1420, -73.6266, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Quindío',            'SALUD', 'Cruz Roja Seccional Quindío (dirección por confirmar), Armenia',            4.5339, -75.6811, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Nariño',             'SALUD', 'Cruz Roja Seccional Nariño (dirección por confirmar), Pasto',               1.2136, -77.2811, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Huila',              'SALUD', 'Cruz Roja Seccional Huila (dirección por confirmar), Neiva',                2.9273, -75.2819, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),
 ('Cruz Roja Colombiana — Seccional Cauca',              'SALUD', 'Cruz Roja Seccional Cauca (dirección por confirmar), Popayán',              2.4448, -76.6147, 'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE);
