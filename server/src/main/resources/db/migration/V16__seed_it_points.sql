-- Official help points for Italy (IT), following the same anti-fraud model as the Colombian (V6–V8),
-- Indonesian and Spanish (V11) seeds: real, official disaster-response institutions; city-accurate
-- coordinates; the exact street left "indirizzo da confermare" for a moderator wherever we don't have
-- a verified address. Addresses end with ", <City>" so the client's city grouping works. Coordinates
-- mirror core/domain ItalyRegions.kt (keep them in sync).

-- Italy — Croce Rossa Italiana (CRI, national HQ + committees in the seismic Apennine/southern cities),
-- Dipartimento della Protezione Civile, Vigili del Fuoco. Single emergency line: 112 (NUE).
INSERT INTO shelters (name, type, address, latitude, longitude, accepts, hours, contact_phone, verified, last_verified, country)
VALUES
 ('Croce Rossa Italiana — Sede Nazionale',             'SALUD', 'Via Bernardino Ramazzini 31, Roma',                    41.9028, 12.4964, 'Assistenza umanitaria e primo soccorso',       'Lun–Ven 9:00–17:00', '112', TRUE, CURRENT_DATE, 'IT'),
 ('Dipartimento della Protezione Civile',             'OTRO',  'Via Ulpiano 11, Roma',                                 41.9050, 12.4720, 'Coordinamento delle emergenze',                'Lun–Ven 9:00–17:00', '112', TRUE, CURRENT_DATE, 'IT'),
 ('Vigili del Fuoco — Comando Nazionale',             'OTRO',  'Piazza Scilla 2, Roma',                                41.9280, 12.5170, 'Soccorso tecnico urgente',                     '24 ore',             '115', TRUE, CURRENT_DATE, 'IT'),
 ('Croce Rossa Italiana — Comitato de L''Aquila',     'SALUD', 'CRI L''Aquila (indirizzo da confermare), L''Aquila',   42.3498, 13.3995, 'Assistenza umanitaria e primo soccorso',       'Lun–Ven 9:00–17:00', '112', TRUE, CURRENT_DATE, 'IT'),
 ('Croce Rossa Italiana — Comitato di Norcia',        'SALUD', 'CRI Norcia (indirizzo da confermare), Norcia',         42.7924, 13.0964, 'Assistenza umanitaria e primo soccorso',       'Lun–Ven 9:00–17:00', '112', TRUE, CURRENT_DATE, 'IT'),
 ('Croce Rossa Italiana — Comitato di Amatrice',      'SALUD', 'CRI Amatrice (indirizzo da confermare), Amatrice',     42.6296, 13.2896, 'Assistenza umanitaria e primo soccorso',       'Lun–Ven 9:00–17:00', '112', TRUE, CURRENT_DATE, 'IT'),
 ('Croce Rossa Italiana — Comitato di Messina',       'SALUD', 'CRI Messina (indirizzo da confermare), Messina',       38.1938, 15.5540, 'Assistenza umanitaria e primo soccorso',       'Lun–Ven 9:00–17:00', '112', TRUE, CURRENT_DATE, 'IT'),
 ('Croce Rossa Italiana — Comitato di Catania',       'SALUD', 'CRI Catania (indirizzo da confermare), Catania',       37.5079, 15.0830, 'Assistenza umanitaria e primo soccorso',       'Lun–Ven 9:00–17:00', '112', TRUE, CURRENT_DATE, 'IT'),
 ('Croce Rossa Italiana — Comitato di Udine',         'SALUD', 'CRI Udine (indirizzo da confermare), Udine',           46.0711, 13.2346, 'Assistenza umanitaria e primo soccorso',       'Lun–Ven 9:00–17:00', '112', TRUE, CURRENT_DATE, 'IT');
