-- Replace the V1 illustrative placeholders with real, official institutions sourced from their own
-- public portals (see below). Rationale: Colombia has no public registry of *permanent* acopios/
-- albergues — those are activated per-event on temporary sites. The stable, verifiable "official
-- points" are the emergency-management and humanitarian institutions that coordinate the response;
-- event-specific acopios then arrive through the admin moderation flow. Coordinates are geocoded from
-- the official published addresses (block-accurate); the addresses themselves are authoritative.
--
-- Sources:
--   UNGRD           https://portal.gestiondelriesgo.gov.co  (Av. Calle 26 No. 92-32, Ed. Gold 4)
--   IDIGER          https://www.idiger.gov.co               (Diagonal 47 No. 77A-09)
--   Cruz Roja Col.  https://www.cruzrojacolombiana.org      (Cra. 68 No. 68B-31, sede nacional)
--   Cruz Roja Bogotá https://www.cruzrojabogota.org.co      (Cra. 23 No. 73-19, San Felipe)
--   Defensa Civil   https://www.defensacivil.gov.co         (Calle 52 No. 14-67, Dirección General)
--   DAGRD Medellín  https://www.medellin.gov.co/es/dagrd    (Calle 44 No. 52-165, CAM La Alpujarra)

DELETE FROM shelters WHERE name IN (
    'Centro de acopio Movistar Arena',
    'Albergue temporal Unidad Deportiva Atanasio Girardot',
    'Punto de hidratación Plaza de Cayzedo'
);

INSERT INTO shelters (name, type, address, latitude, longitude, accepts, hours, contact_phone, verified, last_verified)
VALUES
 ('UNGRD — Unidad Nacional para la Gestión del Riesgo de Desastres', 'OTRO',
  'Av. Calle 26 No. 92-32, Ed. Gold 4, Bogotá', 4.6690, -74.1200,
  'Coordinación nacional de emergencias e información oficial', 'Lun–Vie 8:00–17:00', '123', TRUE, CURRENT_DATE),

 ('IDIGER — Instituto Distrital de Gestión de Riesgos y Cambio Climático', 'OTRO',
  'Diagonal 47 No. 77A-09, Bogotá', 4.6602, -74.1178,
  'Gestión del riesgo en Bogotá; información y coordinación de emergencias', 'Lun–Vie 7:30–16:30', '123', TRUE, CURRENT_DATE),

 ('Cruz Roja Colombiana — Sede Nacional', 'SALUD',
  'Cra. 68 No. 68B-31, Bogotá', 4.6626, -74.0846,
  'Atención humanitaria, primeros auxilios y reencuentro familiar', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),

 ('Cruz Roja — Seccional Cundinamarca y Bogotá', 'SALUD',
  'Cra. 23 No. 73-19, Barrio San Felipe, Bogotá', 4.6668, -74.0664,
  'Atención humanitaria y primeros auxilios', 'Lun–Vie 8:00–17:00', '132', TRUE, CURRENT_DATE),

 ('Defensa Civil Colombiana — Dirección General', 'OTRO',
  'Calle 52 No. 14-67, Bogotá', 4.6376, -74.0648,
  'Búsqueda, rescate y atención de desastres', 'Lun–Vie 8:00–17:00', '144', TRUE, CURRENT_DATE),

 ('DAGRD — Gestión del Riesgo de Desastres (CAM La Alpujarra)', 'OTRO',
  'Calle 44 No. 52-165, Medellín', 6.2447, -75.5730,
  'Coordinación de emergencias en Medellín (línea 123)', 'Lun–Vie 7:30–17:00', '123', TRUE, CURRENT_DATE);
