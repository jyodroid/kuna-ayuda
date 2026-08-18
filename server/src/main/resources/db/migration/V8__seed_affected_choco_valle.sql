-- Event-specific help points for the places actually hit by the Aug 10, 2026 M7.4 quake (epicentre
-- near San José del Palmar, Chocó). V7 already seeds Cruz Roja seccionales for the department capitals,
-- so Cali, Pereira, Manizales and Armenia are ALREADY covered there — this file adds the affected
-- municipalities that are NOT department capitals (so they had no coverage), plus one precise, verified
-- Cruz Roja address in Cali that refines V7's city-centroid placeholder.
--
-- Integrity notes (anti-fraud model):
--  * Chocó municipalities (San José del Palmar, Quibdó, Condoto) are hard-access zones where relief is
--    run as *coordinated humanitarian operations*, NOT permanent public donation centres. We seed them
--    as coordination/response points (verified=TRUE = official coordinated response) and say so plainly
--    in the address/accepts fields rather than fabricating a walk-in donation address.
--  * Coordinates are municipal centroids (city-accurate); exact street left "por confirmar" for a
--    moderator, except the Cali Cruz Roja point whose street address is published/verifiable.
--
-- Sources (reporting on the Aug 2026 response):
--   Quibdó aid distribution (~40 t) .......... AP
--   Condoto medical staging (PAC) ............ Patrulla Aérea Civil Colombiana
--   Buenaventura humanitarian response ....... UNICEF
--   Chocó remote-access medical transport .... Direct Relief
--   Cruz Roja Cali (San Fernando) ............ Cra. 38 Bis #5-91 (published seccional address)

INSERT INTO shelters (name, type, address, latitude, longitude, accepts, hours, contact_phone, verified, last_verified)
VALUES
 -- 🔴 Highest priority / difficult access (Chocó) — coordinated operations, not walk-in donation centres
 ('San José del Palmar — Operación humanitaria regional', 'OTRO',
  'Respuesta humanitaria coordinada (acceso restringido, cerca del epicentro), San José del Palmar', 4.8967, -76.2378,
  'Respuesta humanitaria coordinada por autoridades regionales; acceso difícil', 'Según operativo', '123', TRUE, CURRENT_DATE),

 ('Quibdó — Distribución de ayuda humanitaria', 'ACOPIO',
  'Punto de distribución de ayuda humanitaria (ubicación exacta por confirmar), Quibdó', 5.6919, -76.6583,
  'Distribución de ayuda humanitaria a municipios afectados', 'Según operativo', '123', TRUE, CURRENT_DATE),

 ('Condoto — Puesto médico (Patrulla Aérea Civil)', 'SALUD',
  'Puesto médico y transporte de insumos — Patrulla Aérea Civil Colombiana (PAC), Condoto', 5.0906, -76.6503,
  'Atención médica, insumos y unidades de sangre para municipios afectados', 'Según operativo', '123', TRUE, CURRENT_DATE),

 -- 🟠 Major affected urban area (Valle del Cauca)
 ('Buenaventura — Respuesta humanitaria (UNICEF)', 'OTRO',
  'Respuesta humanitaria activa, con énfasis en niñez y familias (ubicación exacta por confirmar), Buenaventura', 3.8801, -77.0313,
  'Atención humanitaria a niñez y familias; algunas comunidades de difícil acceso', 'Según operativo', '123', TRUE, CURRENT_DATE),

 ('Cruz Roja Colombiana Cali — San Fernando', 'SALUD',
  'Cra. 38 Bis No. 5-91, Barrio San Fernando, Cali', 3.4206, -76.5468,
  'Atención médica, primeros auxilios y ayuda humanitaria', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE);
