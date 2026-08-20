-- Official help points for Peru (PE), following the same anti-fraud model as the Colombian (V6–V8)
-- and ID/ES/IT (V11/V16) seeds: real, official disaster-response institutions; city-accurate
-- coordinates; the exact street left "por confirmar" for a moderator wherever we don't have a
-- verified address. Addresses end with ", <City>" so the client's city grouping works. Coordinates
-- mirror core/domain PeruRegions.kt (keep them in sync). Emergency lines: 105 (Policía), 116
-- (Bomberos), 106 (SAMU), 115 (INDECI / Defensa Civil).

-- National institutions: INDECI (national civil defence), Cruz Roja Peruana (national HQ), CGBVP
-- (national fire brigade command), all in Lima.
INSERT INTO shelters (name, type, address, latitude, longitude, accepts, hours, contact_phone, verified, last_verified, country)
VALUES
 ('INDECI — Instituto Nacional de Defensa Civil (Sede Central)', 'OTRO',  'Calle Ricardo Angulo 694, Urb. Corpac, San Isidro, Lima', -12.0951, -77.0270, 'Coordinación nacional de gestión del riesgo de desastres', 'Lun–Vie 08:30–16:30', '115', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Sede Central',                            'SALUD', 'Av. Arequipa 1285, Lince, Lima',                          -12.0850, -77.0360, 'Ayuda humanitaria y primeros auxilios',                  'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('CGBVP — Comandancia General del Cuerpo de Bomberos',          'OTRO',  'Av. Salaverry, Jesús María, Lima',                       -12.0730, -77.0470, 'Emergencias, incendios y rescate',                       '24 horas',            '116', TRUE, CURRENT_DATE, 'PE');

-- Regional Cruz Roja Peruana / Defensa Civil in the seismic departmental capitals. Coordinates match
-- PeruRegions.kt; the street is left "por confirmar" for a moderator to verify.
INSERT INTO shelters (name, type, address, latitude, longitude, accepts, hours, contact_phone, verified, last_verified, country)
VALUES
 ('Cruz Roja Peruana — Filial Arequipa',   'SALUD', 'Cruz Roja (dirección por confirmar), Arequipa',   -16.4090, -71.5375, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Trujillo',   'SALUD', 'Cruz Roja (dirección por confirmar), Trujillo',   -8.1116,  -79.0288, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Chiclayo',   'SALUD', 'Cruz Roja (dirección por confirmar), Chiclayo',   -6.7714,  -79.8409, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Piura',      'SALUD', 'Cruz Roja (dirección por confirmar), Piura',      -5.1945,  -80.6328, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Cusco',      'SALUD', 'Cruz Roja (dirección por confirmar), Cusco',      -13.5319, -71.9675, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Ica',        'SALUD', 'Cruz Roja (dirección por confirmar), Ica',        -14.0678, -75.7286, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Pisco',      'SALUD', 'Cruz Roja (dirección por confirmar), Pisco',      -13.7100, -76.2036, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Huaraz',     'SALUD', 'Cruz Roja (dirección por confirmar), Huaraz',     -9.5278,  -77.5278, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Chimbote',   'SALUD', 'Cruz Roja (dirección por confirmar), Chimbote',   -9.0745,  -78.5936, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Tacna',      'SALUD', 'Cruz Roja (dirección por confirmar), Tacna',      -18.0066, -70.2463, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Moquegua',   'SALUD', 'Cruz Roja (dirección por confirmar), Moquegua',   -17.1934, -70.9350, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Ayacucho',   'SALUD', 'Cruz Roja (dirección por confirmar), Ayacucho',   -13.1588, -74.2232, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Huancayo',   'SALUD', 'Cruz Roja (dirección por confirmar), Huancayo',   -12.0686, -75.2103, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Cajamarca',  'SALUD', 'Cruz Roja (dirección por confirmar), Cajamarca',  -7.1638,  -78.5003, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Iquitos',    'SALUD', 'Cruz Roja (dirección por confirmar), Iquitos',    -3.7491,  -73.2538, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE'),
 ('Cruz Roja Peruana — Filial Puno',       'SALUD', 'Cruz Roja (dirección por confirmar), Puno',       -15.8402, -70.0219, 'Ayuda humanitaria y primeros auxilios', 'Lun–Vie 08:00–17:00', '105', TRUE, CURRENT_DATE, 'PE');
