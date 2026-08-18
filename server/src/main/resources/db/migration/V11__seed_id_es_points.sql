-- Official help points for Indonesia (ID) and Spain (ES), following the same anti-fraud model as the
-- Colombian seeds (V6–V8): real, official disaster-response institutions; city-accurate coordinates;
-- the exact street left "por confirmar / to confirm" for a moderator wherever we don't have a
-- verified address. Addresses end with ", <City>" so the client's city grouping works. Coordinates
-- mirror core/domain IndonesiaRegions.kt / SpainRegions.kt (keep them in sync).

-- Indonesia — BNPB (national disaster management), PMI (Indonesian Red Cross), Basarnas (national SAR)
-- plus PMI provincial chapters in quake-prone capitals. Emergency line: 112.
INSERT INTO shelters (name, type, address, latitude, longitude, accepts, hours, contact_phone, verified, last_verified, country)
VALUES
 ('BNPB — Badan Nasional Penanggulangan Bencana',       'OTRO',  'Jl. Pramuka Kav. 38, Jakarta',                                 -6.1889, 106.8650, 'Koordinasi penanggulangan bencana nasional', 'Sen–Jum 08:00–16:00', '112', TRUE, CURRENT_DATE, 'ID'),
 ('PMI — Palang Merah Indonesia (Kantor Pusat)',        'SALUD', 'Jl. Jend. Gatot Subroto Kav. 96, Jakarta',                     -6.2447, 106.8300, 'Bantuan kemanusiaan dan pertolongan pertama',  'Sen–Jum 08:00–16:00', '112', TRUE, CURRENT_DATE, 'ID'),
 ('Basarnas — Badan Nasional Pencarian dan Pertolongan','OTRO',  'Jl. Angkasa Blok B-15 Kav. 2-3, Kemayoran, Jakarta',           -6.1560, 106.8460, 'Pencarian dan pertolongan (SAR)',              '24 horas',            '115', TRUE, CURRENT_DATE, 'ID'),
 ('PMI — Provinsi Sumatera Barat',                      'SALUD', 'PMI Sumatera Barat (alamat belum dikonfirmasi), Padang',        -0.9471, 100.4172, 'Bantuan kemanusiaan dan pertolongan pertama',  'Sen–Jum 08:00–16:00', '112', TRUE, CURRENT_DATE, 'ID'),
 ('PMI — Provinsi Nusa Tenggara Barat',                 'SALUD', 'PMI Nusa Tenggara Barat (alamat belum dikonfirmasi), Mataram',  -8.5833, 116.1167, 'Bantuan kemanusiaan dan pertolongan pertama',  'Sen–Jum 08:00–16:00', '112', TRUE, CURRENT_DATE, 'ID'),
 ('PMI — Provinsi Aceh',                                'SALUD', 'PMI Aceh (alamat belum dikonfirmasi), Banda Aceh',              5.5483,  95.3238, 'Bantuan kemanusiaan dan pertolongan pertama',  'Sen–Jum 08:00–16:00', '112', TRUE, CURRENT_DATE, 'ID');

-- Spain — Cruz Roja Española (HQ + provincial in the seismic south-east), Protección Civil, UME.
-- Emergency line: 112. Cruz Roja: 900 22 11 22.
INSERT INTO shelters (name, type, address, latitude, longitude, accepts, hours, contact_phone, verified, last_verified, country)
VALUES
 ('Cruz Roja Española — Oficina Central',               'SALUD', 'Av. Reina Victoria 26, Madrid',                                40.4470, -3.7038, 'Ayuda humanitaria y primeros auxilios',        'Lun–Vie 8:00–17:00', '900221122', TRUE, CURRENT_DATE, 'ES'),
 ('Dirección Gral. de Protección Civil y Emergencias',  'OTRO',  'C/ Quintiliano 21, Madrid',                                    40.4413, -3.6740, 'Coordinación de emergencias',                  'Lun–Vie 8:00–17:00', '112',       TRUE, CURRENT_DATE, 'ES'),
 ('UME — Unidad Militar de Emergencias',                'OTRO',  'Base Aérea de Torrejón de Ardoz, Madrid',                      40.4876, -3.4590, 'Intervención en emergencias',                  '24 horas',           '112',       TRUE, CURRENT_DATE, 'ES'),
 ('Cruz Roja Española — Granada',                       'SALUD', 'Cruz Roja Granada (dirección por confirmar), Granada',          37.1773, -3.5986, 'Ayuda humanitaria y primeros auxilios',        'Lun–Vie 8:00–17:00', '900221122', TRUE, CURRENT_DATE, 'ES'),
 ('Cruz Roja Española — Almería',                       'SALUD', 'Cruz Roja Almería (dirección por confirmar), Almería',          36.8340, -2.4637, 'Ayuda humanitaria y primeros auxilios',        'Lun–Vie 8:00–17:00', '900221122', TRUE, CURRENT_DATE, 'ES'),
 ('Cruz Roja Española — Murcia',                        'SALUD', 'Cruz Roja Murcia (dirección por confirmar), Murcia',            37.9922, -1.1307, 'Ayuda humanitaria y primeros auxilios',        'Lun–Vie 8:00–17:00', '900221122', TRUE, CURRENT_DATE, 'ES');
