INSERT INTO users (username, email, password_hash, full_name, status)
VALUES (
           'admin',
           'admin@afc.vn',
           '$2a$10$PDjj6hQ88hOiA7YvrNSOQeinFqz4ghkipd3pTdQcYJaXWk1OOe3Mu',
           'System Admin',
           'ACTIVE'
       );

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.code = 'ACC_ADMIN';

-- ═══════════════════════════════════════════════════════════════
-- SEED: OPERATORS
-- ═══════════════════════════════════════════════════════════════

INSERT INTO operators (code, name, status) VALUES
                                               ('HURC',      'Hanoi Urban Railway Company',  'ACTIVE'),
                                               ('TRANSERCO', 'Tổng công ty Vận tải Hà Nội', 'ACTIVE');

-- ═══════════════════════════════════════════════════════════════
-- SEED: ROUTES
-- ═══════════════════════════════════════════════════════════════

INSERT INTO routes (operator_id, code, name, type) VALUES
                                                       ((SELECT id FROM operators WHERE code = 'HURC'),
                                                        'HN_2A',     'Cát Linh - Hà Đông',         'METRO'),
                                                       ((SELECT id FROM operators WHERE code = 'HURC'),
                                                        'HN_3_1',    'Nhổn - Ga Hà Nội',            'METRO'),
                                                       ((SELECT id FROM operators WHERE code = 'TRANSERCO'),
                                                        'HN_BRT_01', 'BRT 01: Yên Nghĩa - Kim Mã',  'BUS'),
                                                       ((SELECT id FROM operators WHERE code = 'TRANSERCO'),
                                                        'HN_BUS_32', 'Buýt 32: Giáp Bát - Nhổn',    'BUS');

-- ═══════════════════════════════════════════════════════════════
-- SEED: STATIONS — Tuyến 2A
-- ═══════════════════════════════════════════════════════════════

INSERT INTO stations (route_id, code, name, km_marker, station_order) VALUES
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_01', 'Cát Linh',     0.000,  1),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_02', 'La Thành',     0.700,  2),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_03', 'Thái Hà',      1.600,  3),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_04', 'Láng',         2.700,  4),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_05', 'Thượng Đình',  3.900,  5),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_06', 'Vành Đai 3',   5.000,  6),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_07', 'Phùng Khoang', 6.400,  7),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_08', 'Văn Quán',     7.500,  8),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_09', 'Hà Đông',      8.800,  9),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_10', 'La Khê',       10.000, 10),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_11', 'Văn Khê',      11.400, 11),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_2A'), 'HN_2A_12', 'Yên Nghĩa',    12.500, 12);

-- ═══════════════════════════════════════════════════════════════
-- SEED: STATIONS — Tuyến 3.1
-- ═══════════════════════════════════════════════════════════════

INSERT INTO stations (route_id, code, name, km_marker, station_order) VALUES
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_01', 'Nhổn',             0.000, 1),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_02', 'Minh Khai',        1.100, 2),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_03', 'Phú Diễn',         2.200, 3),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_04', 'Cầu Diễn',         3.000, 4),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_05', 'Lê Đức Thọ',       4.100, 5),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_06', 'Đại học Quốc Gia', 5.100, 6),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_07', 'Chùa Hà',          6.300, 7),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_3_1'), 'HN_3_08', 'Cầu Giấy',         7.400, 8);

-- ═══════════════════════════════════════════════════════════════
-- SEED: STATIONS — BRT 01
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
-- SEED: STATIONS — Buýt 32
-- ═══════════════════════════════════════════════════════════════

INSERT INTO stations (route_id, code, name, km_marker, station_order) VALUES
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_01', 'Bến xe Giáp Bát',   0.000,  1),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_02', 'Ngã tư Giải Phóng', 1.800,  2),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_03', 'Ga Hà Nội',          3.200,  3),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_04', 'Cầu Giấy',           7.500,  4),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_05', 'Hồ Tùng Mậu',        10.200, 5),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_06', 'Phú Diễn',           13.800, 6),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_07', 'Minh Khai',          15.900, 7),
                                                                          ((SELECT id FROM routes WHERE code = 'HN_BUS_32'), 'BUS32_08', 'Nhổn',               18.200, 8);

-- ═══════════════════════════════════════════════════════════════
-- SEED: FARE RULES (QĐ 3316/2025)
-- ═══════════════════════════════════════════════════════════════

INSERT INTO fare_rules (code, mode, base_fare, rate_per_km,
                        min_price, max_price,
                        effective_from, status, version, created_by)
VALUES
    ('HN_METRO_STANDARD', 'METRO',
     8000, 850, 8000, 30000,
     '2025-07-01', 'ACTIVE', 1,
     (SELECT id FROM users WHERE username = 'admin')),

    ('HN_BUS_STANDARD', 'BUS',
     3000, 450, 3000, 30000,
     '2025-07-01', 'ACTIVE', 1,
     (SELECT id FROM users WHERE username = 'admin'));

-- ═══════════════════════════════════════════════════════════════
-- SEED: FARE PASS PRICES (QĐ 3316/2025)
-- Giá cơ sở = đối tượng không ưu tiên
-- Ưu tiên áp dụng qua fare_discounts (STUDENT/SENIOR/PRIORITY)
-- ═══════════════════════════════════════════════════════════════

INSERT INTO fare_pass_prices (fare_rule_id, duration_type, duration_months, scope, amount)
VALUES
    -- ── METRO ──────────────────────────────────────────────────
    ((SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'), 'DAILY',   NULL, NULL, 40000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'), 'WEEKLY',  NULL, NULL, 160000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'), 'MONTHLY', 1,    NULL, 200000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'), 'MONTHLY', 2,    NULL, 390000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'), 'MONTHLY', 3,    NULL, 590000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'), 'MONTHLY', 4,    NULL, 770000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'), 'MONTHLY', 5,    NULL, 960000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'), 'MONTHLY', 6,    NULL, 1150000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'), 'MONTHLY', 7,    NULL, 1315000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'), 'MONTHLY', 8,    NULL, 1505000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'), 'MONTHLY', 9,    NULL, 1690000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'), 'MONTHLY', 10,   NULL, 1880000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'), 'MONTHLY', 11,   NULL, 2070000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'), 'MONTHLY', 12,   NULL, 2255000),

    -- ── BUS ────────────────────────────────────────────────────
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'DAILY',   NULL, NULL,           30000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'WEEKLY',  NULL, NULL,           120000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 1,    'SINGLE_ROUTE', 140000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 1,    'MULTI_ROUTE',  280000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 2,    'SINGLE_ROUTE', 270000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 2,    'MULTI_ROUTE',  550000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 3,    'SINGLE_ROUTE', 410000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 3,    'MULTI_ROUTE',  820000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 4,    'SINGLE_ROUTE', 535000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 4,    'MULTI_ROUTE',  1075000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 5,    'SINGLE_ROUTE', 670000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 5,    'MULTI_ROUTE',  1345000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 6,    'SINGLE_ROUTE', 805000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 6,    'MULTI_ROUTE',  1615000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 7,    'SINGLE_ROUTE', 920000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 7,    'MULTI_ROUTE',  1840000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 8,    'SINGLE_ROUTE', 1050000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 8,    'MULTI_ROUTE',  2105000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 9,    'SINGLE_ROUTE', 1185000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 9,    'MULTI_ROUTE',  2367000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 10,   'SINGLE_ROUTE', 1315000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 10,   'MULTI_ROUTE',  2630000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 11,   'SINGLE_ROUTE', 1450000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 11,   'MULTI_ROUTE',  2895000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 12,   'SINGLE_ROUTE', 1580000),
    ((SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'), 'MONTHLY', 12,   'MULTI_ROUTE',  3158000);

-- ═══════════════════════════════════════════════════════════════
-- SEED: FARE DISCOUNTS (QĐ 3316/2025)
-- ═══════════════════════════════════════════════════════════════

INSERT INTO fare_discounts (passenger_type, discount_type,
                            discount_value, effective_from, status)
VALUES
    ('STUDENT',  'PERCENT', 50,  '2025-07-01', 'ACTIVE'),
    ('SENIOR',   'PERCENT', 100, '2025-07-01', 'ACTIVE'),
    ('PRIORITY', 'PERCENT', 100, '2025-07-01', 'ACTIVE');

-- ═══════════════════════════════════════════════════════════════
-- SEED: REVENUE SHARE RULES
-- ═══════════════════════════════════════════════════════════════

INSERT INTO revenue_share_rules (operator_id, share_model, share_percentage,
                                 effective_from, status, version, created_by)
VALUES
    ((SELECT id FROM operators WHERE code = 'HURC'),
     'KM_BASED', 65.00, '2025-07-01', 'ACTIVE', 1,
     (SELECT id FROM users WHERE username = 'admin')),

    ((SELECT id FROM operators WHERE code = 'TRANSERCO'),
     'KM_BASED', 35.00, '2025-07-01', 'ACTIVE', 1,
     (SELECT id FROM users WHERE username = 'admin'));

-- ═══════════════════════════════════════════════════════════════
-- SEED: INTEROP CONFIGS
-- ═══════════════════════════════════════════════════════════════

INSERT INTO interop_configs (from_operator_id, to_operator_id,
                             transfer_discount, enabled)
VALUES
    ((SELECT id FROM operators WHERE code = 'HURC'),
     (SELECT id FROM operators WHERE code = 'TRANSERCO'),
     0, true),

    ((SELECT id FROM operators WHERE code = 'TRANSERCO'),
     (SELECT id FROM operators WHERE code = 'HURC'),
     0, true);

-- ═══════════════════════════════════════════════════════════════
-- SEED: CARDS
-- ═══════════════════════════════════════════════════════════════

INSERT INTO cards (card_uid, status, type, supports_metro,
                   supports_bus, activated_at)
VALUES
    ('CARD-ANON-001', 'ACTIVE',    'ANON',       true, true,  CURRENT_TIMESTAMP),
    ('CARD-ANON-002', 'ACTIVE',    'ANON',       true, false, CURRENT_TIMESTAMP),
    ('CARD-ANON-003', 'SUSPENDED', 'ANON',       true, true,  CURRENT_TIMESTAMP),
    ('CARD-ID-001',   'ACTIVE',    'IDENTIFIED', true, true,  CURRENT_TIMESTAMP);

UPDATE cards
SET linked_user_id = (SELECT id FROM users WHERE username = 'admin'),
    linked_at      = CURRENT_TIMESTAMP
WHERE card_uid = 'CARD-ID-001';

-- ═══════════════════════════════════════════════════════════════
-- SEED: BLACKLIST
-- ═══════════════════════════════════════════════════════════════

INSERT INTO blacklist (card_id, reason, added_by, is_active)
VALUES (
           (SELECT id FROM cards WHERE card_uid = 'CARD-ANON-003'),
           'Seed: thẻ tạm khóa để test blacklist',
           (SELECT id FROM users WHERE username = 'admin'),
           true
       );

-- ═══════════════════════════════════════════════════════════════
-- SEED: TICKETS
-- ═══════════════════════════════════════════════════════════════

-- Single trip (Cát Linh → Văn Quán, METRO) — 7.5 km → 8000 + 7.5×850 = 14375
INSERT INTO tickets (card_id, user_id, type, price,
                     fare_rule_id, discount_id,
                     from_station_id, to_station_id,
                     mode, valid_from, valid_to, status)
VALUES (
           NULL,
           (SELECT id FROM users WHERE username = 'admin'),
           'SINGLE_TRIP', 14375.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           NULL,
           (SELECT id FROM stations WHERE code = 'HN_2A_01'),
           (SELECT id FROM stations WHERE code = 'HN_2A_08'),
           'METRO',
           CURRENT_DATE, CURRENT_DATE + INTERVAL '1 day',
           'ACTIVE'
       );

-- Single trip STUDENT 50% → 7187.5đ
INSERT INTO tickets (card_id, user_id, type, price,
                     fare_rule_id, discount_id,
                     from_station_id, to_station_id,
                     mode, valid_from, valid_to, status)
VALUES (
           NULL,
           (SELECT id FROM users WHERE username = 'admin'),
           'SINGLE_TRIP', 7187.50,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           (SELECT id FROM fare_discounts WHERE passenger_type = 'STUDENT'),
           (SELECT id FROM stations WHERE code = 'HN_2A_01'),
           (SELECT id FROM stations WHERE code = 'HN_2A_08'),
           'METRO',
           CURRENT_DATE, CURRENT_DATE + INTERVAL '1 day',
           'ACTIVE'
       );

-- Single trip đã USED
INSERT INTO tickets (card_id, user_id, type, price,
                     fare_rule_id,
                     from_station_id, to_station_id,
                     mode, valid_from, valid_to, status, used_at)
VALUES (
           NULL,
           (SELECT id FROM users WHERE username = 'admin'),
           'SINGLE_TRIP', 14375.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           (SELECT id FROM stations WHERE code = 'HN_2A_01'),
           (SELECT id FROM stations WHERE code = 'HN_2A_08'),
           'METRO',
           CURRENT_DATE - INTERVAL '1 day', CURRENT_DATE,
           'USED',
           CURRENT_TIMESTAMP - INTERVAL '2 hours'
       );

-- METRO MONTHLY 1 tháng → 200k (link vào CARD-ID-001)
INSERT INTO tickets (card_id, user_id, type, price,
                     fare_rule_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM cards WHERE card_uid = 'CARD-ID-001'),
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 200000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           'METRO',
           DATE_TRUNC('month', CURRENT_DATE),
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '30 days',
           'ACTIVE'
       );

-- METRO MONTHLY 1 tháng STUDENT 50% → 100k
INSERT INTO tickets (user_id, type, price, fare_rule_id, discount_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 100000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           (SELECT id FROM fare_discounts WHERE passenger_type = 'STUDENT'),
           'METRO',
           DATE_TRUNC('month', CURRENT_DATE),
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '30 days',
           'ACTIVE'
       );

-- METRO MONTHLY 1 tháng SENIOR 100% → 0đ
INSERT INTO tickets (user_id, type, price, fare_rule_id, discount_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 0.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           (SELECT id FROM fare_discounts WHERE passenger_type = 'SENIOR'),
           'METRO',
           DATE_TRUNC('month', CURRENT_DATE),
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '30 days',
           'ACTIVE'
       );

-- METRO WEEKLY → 160k
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 160000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           'METRO',
           CURRENT_DATE,
           CURRENT_DATE + INTERVAL '7 days',
           'ACTIVE'
       );

-- BUS SINGLE_ROUTE MONTHLY 1 tháng → 140k
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status, scope)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 140000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           'BUS',
           DATE_TRUNC('month', CURRENT_DATE),
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '30 days',
           'ACTIVE',
           'SINGLE_ROUTE'
       );

-- BUS SINGLE_ROUTE MONTHLY 1 tháng STUDENT 50% → 70k
INSERT INTO tickets (user_id, type, price, fare_rule_id, discount_id,
                     mode, valid_from, valid_to, status, scope)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 70000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           (SELECT id FROM fare_discounts WHERE passenger_type = 'STUDENT'),
           'BUS',
           DATE_TRUNC('month', CURRENT_DATE),
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '30 days',
           'ACTIVE',
           'SINGLE_ROUTE'
       );

-- BUS MULTI_ROUTE MONTHLY 1 tháng → 280k
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status, scope)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 280000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           'BUS',
           DATE_TRUNC('month', CURRENT_DATE),
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '30 days',
           'ACTIVE',
           'MULTI_ROUTE'
       );

-- BUS MULTI_ROUTE MONTHLY 1 tháng STUDENT 50% → 140k
INSERT INTO tickets (user_id, type, price, fare_rule_id, discount_id,
                     mode, valid_from, valid_to, status, scope)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 140000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           (SELECT id FROM fare_discounts WHERE passenger_type = 'STUDENT'),
           'BUS',
           DATE_TRUNC('month', CURRENT_DATE),
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '30 days',
           'ACTIVE',
           'MULTI_ROUTE'
       );

-- METRO EXPIRED (tháng trước)
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 200000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           'METRO',
           DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month',
           DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 day',
           'EXPIRED'
       );

-- BUS SINGLE_ROUTE tháng sau (chưa có hiệu lực)
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status, scope)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 140000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           'BUS',
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month',
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '2 month' - INTERVAL '1 day',
           'ACTIVE',
           'SINGLE_ROUTE'
       );

-- ═══════════════════════════════════════════════════════════════
-- SETTLEMENT TEST SEED — June 2026
-- Expected: totalExpected = 15,000 + 200,000 + 140,000 + 280,000 = 635,000đ
-- ═══════════════════════════════════════════════════════════════

-- ── POOL 1: Vé lượt WALLET → direct 100% HURC ─────────────────
INSERT INTO trips (
    card_id, operator_id,
    tap_in_station_id, tap_in_gate_id, tap_in_at,
    distance_km, fare_amount,
    payment_method, ticket_type_used, transport_mode, status
) VALUES (
             (SELECT id FROM cards    WHERE card_uid = 'CARD-ANON-001'),
             (SELECT id FROM operators WHERE code    = 'HURC'),
             (SELECT id FROM stations  WHERE code    = 'HN_2A_01'),
             'GATE_IN_01', CURRENT_TIMESTAMP - INTERVAL '5 days',
             8.5, 15000.00,
             'WALLET', 'SINGLE_TRIP', 'METRO', 'COMPLETED'
         );

-- ── POOL 2a: Vé tháng METRO → direct 100% HURC ────────────────
INSERT INTO trips (
    card_id, operator_id, ticket_id,
    tap_in_station_id, tap_in_gate_id, tap_in_at,
    distance_km,
    payment_method, ticket_type_used, transport_mode, status
) VALUES (
             (SELECT id FROM cards    WHERE card_uid = 'CARD-ID-001'),
             (SELECT id FROM operators WHERE code    = 'HURC'),
             (SELECT id FROM tickets   WHERE type = 'MONTHLY_PASS' AND mode = 'METRO'
                                         AND price = 200000 LIMIT 1),
         (SELECT id FROM stations  WHERE code = 'HN_3_01'),
    'GATE_IN_02', CURRENT_TIMESTAMP - INTERVAL '4 days',
    5.0,
    'TICKET', 'MONTHLY_PASS', 'METRO', 'COMPLETED'
    );

-- ── POOL 2b: Vé tháng BUS SINGLE_ROUTE → direct 100% TRANSERCO ─
INSERT INTO trips (
    operator_id, ticket_id,
    tap_in_station_id, tap_in_gate_id, tap_in_at,
    distance_km,
    payment_method, ticket_type_used, transport_mode, status
) VALUES (
             (SELECT id FROM operators WHERE code = 'TRANSERCO'),
             (SELECT id FROM tickets   WHERE type = 'MONTHLY_PASS' AND scope = 'SINGLE_ROUTE'
                                         AND price = 140000 LIMIT 1),
         (SELECT id FROM stations  WHERE code = 'BUS32_01'),
    'GATE_IN_03', CURRENT_TIMESTAMP - INTERVAL '3 days',
    10.2,
    'TICKET', 'MONTHLY_PASS', 'BUS', 'COMPLETED'
    );

-- ── POOL 3: Vé tháng BUS MULTI_ROUTE → allocateProportional ────
-- weight(TRANSERCO) = 3000×2 + 450×22.0 = 15,900
-- weight(HURC)      = 8000×1 + 850×6.0  = 13,100
-- totalWeight = 29,000
-- TRANSERCO → 15900/29000 × 280,000 ≈ 153,517đ
-- HURC      → 13100/29000 × 280,000 ≈ 126,483đ

INSERT INTO trips (
    operator_id, ticket_id,
    tap_in_station_id, tap_in_gate_id, tap_in_at,
    distance_km,
    payment_method, ticket_type_used, transport_mode, status
) VALUES
-- Trip 3-A: TRANSERCO, BUS, 14 km
(
    (SELECT id FROM operators WHERE code = 'TRANSERCO'),
    (SELECT id FROM tickets   WHERE type = 'MONTHLY_PASS' AND scope = 'MULTI_ROUTE'
                                AND price = 280000 LIMIT 1),
(SELECT id FROM stations  WHERE code = 'BRT01_01'),
    'GATE_IN_04', CURRENT_TIMESTAMP - INTERVAL '2 days',
    14.0,
    'TICKET', 'MONTHLY_PASS', 'BUS', 'COMPLETED'
    ),
-- Trip 3-B: TRANSERCO, BUS, 8 km
    (
    (SELECT id FROM operators WHERE code = 'TRANSERCO'),
    (SELECT id FROM tickets   WHERE type = 'MONTHLY_PASS' AND scope = 'MULTI_ROUTE'
    AND price = 280000 LIMIT 1),
    (SELECT id FROM stations  WHERE code = 'BUS32_05'),
    'GATE_IN_05', CURRENT_TIMESTAMP - INTERVAL '1 days',
    8.0,
    'TICKET', 'MONTHLY_PASS', 'BUS', 'COMPLETED'
    ),
-- Trip 3-C: HURC, METRO, 6 km
    (
    (SELECT id FROM operators WHERE code = 'HURC'),
    (SELECT id FROM tickets   WHERE type = 'MONTHLY_PASS' AND scope = 'MULTI_ROUTE'
    AND price = 280000 LIMIT 1),
    (SELECT id FROM stations  WHERE code = 'HN_2A_01'),
    'GATE_IN_06', CURRENT_TIMESTAMP - INTERVAL '2 days 12 hours',
    6.0,
    'TICKET', 'MONTHLY_PASS', 'METRO', 'COMPLETED'
    );

-- ═══════════════════════════════════════════════════════════════
-- EXPECTED CompanyShare sau settlement:
--   HURC      = 15,000 + 200,000 + 126,483 = 341,483đ
--   TRANSERCO =          140,000 + 153,517  = 293,517đ
--   totalActual = 635,000đ  →  reconciliationStatus = MATCH
-- ═══════════════════════════════════════════════════════════════