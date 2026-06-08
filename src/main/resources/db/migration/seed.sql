-- ═══════════════════════════════════════════════════════════════
-- SEED: OPERATORS
-- ═══════════════════════════════════════════════════════════════

INSERT INTO operators (code, name, status) VALUES
                                               ('HURC',      'Hanoi Urban Railway Company',        'ACTIVE'),
                                               ('TRANSERCO', 'Tổng công ty Vận tải Hà Nội',       'ACTIVE');

-- ═══════════════════════════════════════════════════════════════
-- SEED: ROUTES
-- ═══════════════════════════════════════════════════════════════

INSERT INTO routes (operator_id, code, name, type) VALUES
                                                       ((SELECT id FROM operators WHERE code = 'HURC'),
                                                        'HN_2A',    'Cát Linh - Hà Đông',   'METRO'),

                                                       ((SELECT id FROM operators WHERE code = 'HURC'),
                                                        'HN_3_1',   'Nhổn - Ga Hà Nội',     'METRO'),

                                                       ((SELECT id FROM operators WHERE code = 'TRANSERCO'),
                                                        'HN_BRT_01','BRT 01: Yên Nghĩa - Kim Mã', 'BUS'),

                                                       ((SELECT id FROM operators WHERE code = 'TRANSERCO'),
                                                        'HN_BUS_32','Buýt 32: Bến xe Giáp Bát - Nhổn', 'BUS');

-- ═══════════════════════════════════════════════════════════════
-- SEED: STATIONS — Tuyến 2A (km_marker từ QĐ 3316/2025 Phụ lục III)
-- Công thức: km = (fare_ga1_to_gaN - 8000) / 850
-- ═══════════════════════════════════════════════════════════════

INSERT INTO stations (route_id, code, name, km_marker, station_order) VALUES
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_01', 'Cát Linh',     0.000,  1),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_02', 'La Thành',     0.700,  2),  -- (8595-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_03', 'Thái Hà',      1.600,  3),  -- (9360-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_04', 'Láng',         2.700,  4),  -- (10295-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_05', 'Thượng Đình',  3.900,  5),  -- (11315-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_06', 'Vành Đai 3',   5.000,  6),  -- (12250-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_07', 'Phùng Khoang', 6.400,  7),  -- (13440-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_08', 'Văn Quán',     7.500,  8),  -- (14375-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_09', 'Hà Đông',      8.800,  9),  -- (15480-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_10', 'La Khê',       10.000, 10), -- (16500-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_11', 'Văn Khê',      11.400, 11), -- (17690-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_12', 'Yên Nghĩa',    12.500, 12); -- (18625-8000)/850

-- ═══════════════════════════════════════════════════════════════
-- SEED: STATIONS — Tuyến 3.1 (km_marker từ QĐ 3316/2025 Phụ lục III)
-- Công thức: km = (fare_ga1_to_gaN - 8000) / 850
-- ═══════════════════════════════════════════════════════════════

INSERT INTO stations (route_id, code, name, km_marker, station_order) VALUES
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_01', 'Nhổn',                0.000, 1),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_02', 'Minh Khai',           1.100, 2),  -- (8935-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_03', 'Phú Diễn',            2.200, 3),  -- (9870-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_04', 'Cầu Diễn',            3.000, 4),  -- (10550-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_05', 'Lê Đức Thọ',          4.100, 5),  -- (11485-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_06', 'Đại học Quốc Gia',    5.100, 6),  -- (12335-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_07', 'Chùa Hà',             6.300, 7),  -- (13355-8000)/850
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_08', 'Cầu Giấy',            7.400, 8);  -- (14290-8000)/850

-- ═══════════════════════════════════════════════════════════════
-- SEED: STATIONS — BRT 01 (kết nối metro 2A, km_marker ước tính)
-- ═══════════════════════════════════════════════════════════════

INSERT INTO stations (route_id, code, name, km_marker, station_order) VALUES
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BRT_01'), 'BRT01_01', 'Bến xe Yên Nghĩa', 0.000,  1),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BRT_01'), 'BRT01_02', 'Văn Khê',           1.200,  2),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BRT_01'), 'BRT01_03', 'La Khê',            2.100,  3),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BRT_01'), 'BRT01_04', 'Hà Đông',           3.500,  4),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BRT_01'), 'BRT01_05', 'Vành Đai 3',        5.800,  5),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BRT_01'), 'BRT01_06', 'Thượng Đình',       7.200,  6),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BRT_01'), 'BRT01_07', 'Láng Hạ',           9.100,  7),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BRT_01'), 'BRT01_08', 'Giảng Võ',          11.200, 8),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BRT_01'), 'BRT01_09', 'Cát Linh',          12.400, 9),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BRT_01'), 'BRT01_10', 'Kim Mã',            14.000, 10);

-- ═══════════════════════════════════════════════════════════════
-- SEED: STATIONS — Buýt 32 (kết nối metro 3.1 ga Nhổn, km_marker ước tính)
-- ═══════════════════════════════════════════════════════════════

INSERT INTO stations (route_id, code, name, km_marker, station_order) VALUES
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_01', 'Bến xe Giáp Bát',  0.000,  1),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_02', 'Ngã tư Giải Phóng',1.800,  2),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_03', 'Ga Hà Nội',         3.200,  3),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_04', 'Cầu Giấy',          7.500,  4),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_05', 'Hồ Tùng Mậu',       10.200, 5),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_06', 'Phú Diễn',           13.800, 6),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_07', 'Minh Khai',          15.900, 7),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_08', 'Nhổn',               18.200, 8);