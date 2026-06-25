TRUNCATE TABLE
    reconciliation_logs,
    company_shares,
    settlements,
    trip_anomalies,
    tap_events,
    trips,
    tickets,
    blacklist,
    card_link_history,
    card_status_history,
    cards,
    fare_discount_audit_log,
    fare_discounts,
    fare_rule_audit_log,
    fare_pass_prices,
    fare_rules,
    revenue_share_rule_audit_log,
    revenue_share_rules,
    stations,
    routes,
    operators,
    refresh_tokens,
    user_roles,
    users,
    role_permissions,
    roles,
    permissions
CASCADE;

-- ═══════════════════════════════════════════════════════════════
-- SEED: PERMISSIONS
-- ═══════════════════════════════════════════════════════════════
INSERT INTO permissions (code, name, resource_group) VALUES
                                                         ('USER_READ',          'Xem danh sách người dùng',          'USER'),
                                                         ('USER_CREATE',        'Tạo người dùng mới',                'USER'),
                                                         ('USER_UPDATE',        'Cập nhật thông tin người dùng',     'USER'),
                                                         ('USER_DEACTIVATE',    'Vô hiệu hóa người dùng',            'USER'),
                                                         ('CARD_READ',          'Xem thông tin thẻ',                 'CARD'),
                                                         ('CARD_ISSUE',         'Phát thẻ tại trạm',                 'CARD'),
                                                         ('CARD_ACTIVATE',      'Kích hoạt thẻ',                     'CARD'),
                                                         ('CARD_SUSPEND',       'Tạm khóa thẻ',                      'CARD'),
                                                         ('CARD_REVOKE',        'Thu hồi thẻ',                       'CARD'),
                                                         ('CARD_BLACKLIST',     'Thêm thẻ vào blacklist',            'CARD'),
                                                         ('CARD_LINK',          'Liên kết thẻ với tài khoản',        'CARD'),
                                                         ('FARE_READ',          'Xem cấu hình giá vé',               'FARE'),
                                                         ('FARE_CREATE',        'Tạo quy tắc giá vé',                'FARE'),
                                                         ('FARE_UPDATE',        'Cập nhật quy tắc giá vé',           'FARE'),
                                                         ('FARE_DISABLE',       'Vô hiệu hóa quy tắc giá vé',        'FARE'),
                                                         ('DISCOUNT_READ',      'Xem cấu hình giảm giá',             'DISCOUNT'),
                                                         ('DISCOUNT_CREATE',    'Tạo chính sách giảm giá',           'DISCOUNT'),
                                                         ('DISCOUNT_UPDATE',    'Cập nhật chính sách giảm giá',      'DISCOUNT'),
                                                         ('TRIP_READ',          'Xem tất cả chuyến đi',              'TRIP'),
                                                         ('TRIP_READ_OWN',      'Xem chuyến đi của bản thân',        'TRIP'),
                                                         ('TAP_PROCESS',        'Xử lý tap in / tap out',            'TRIP'),
                                                         ('WALLET_READ',        'Xem tất cả ví',                     'WALLET'),
                                                         ('WALLET_READ_OWN',    'Xem ví của bản thân',               'WALLET'),
                                                         ('WALLET_DEPOSIT',     'Nạp tiền vào ví',                   'WALLET'),
                                                         ('TICKET_READ',        'Xem tất cả vé',                     'TICKET'),
                                                         ('TICKET_READ_OWN',    'Xem vé của bản thân',               'TICKET'),
                                                         ('TICKET_PURCHASE',    'Mua vé',                            'TICKET'),
                                                         ('SHIFT_READ',         'Xem ca làm việc',                   'SHIFT'),
                                                         ('SHIFT_CREATE',       'Tạo ca làm việc',                   'SHIFT'),
                                                         ('SHIFT_UPDATE',       'Cập nhật ca làm việc',              'SHIFT'),
                                                         ('SETTLEMENT_READ',    'Xem báo cáo quyết toán',            'SETTLEMENT'),
                                                         ('SETTLEMENT_RUN',     'Chạy quyết toán',                   'SETTLEMENT'),
                                                         ('SETTLEMENT_CONFIRM', 'Xác nhận quyết toán',               'SETTLEMENT'),
                                                         ('OPERATOR_READ',      'Xem thông tin đơn vị vận hành',     'OPERATOR'),
                                                         ('OPERATOR_CREATE',    'Tạo đơn vị vận hành',               'OPERATOR'),
                                                         ('OPERATOR_UPDATE',    'Cập nhật đơn vị vận hành',          'OPERATOR'),
                                                         ('REPORT_READ',        'Xem báo cáo tổng hợp',              'REPORT');

-- ═══════════════════════════════════════════════════════════════
-- SEED: ROLES
-- ═══════════════════════════════════════════════════════════════
INSERT INTO roles (code, name, description) VALUES
                                                ('ACC_ADMIN',     'Account Admin', 'Quản trị toàn bộ hệ thống'),
                                                ('STATION_STAFF', 'Station Staff', 'Nhân viên tại trạm'),
                                                ('PASSENGER',     'Passenger',     'Hành khách');

-- ═══════════════════════════════════════════════════════════════
-- SEED: ROLE_PERMISSIONS
-- ═══════════════════════════════════════════════════════════════
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ACC_ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'STATION_STAFF'
  AND p.code IN (
                 'CARD_READ','CARD_ISSUE','CARD_ACTIVATE','CARD_SUSPEND','CARD_BLACKLIST','CARD_LINK',
                 'TAP_PROCESS','TRIP_READ','WALLET_READ','TICKET_READ',
                 'SHIFT_READ','SHIFT_UPDATE','OPERATOR_READ'
    );

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'PASSENGER'
  AND p.code IN (
                 'TRIP_READ_OWN','WALLET_READ_OWN','WALLET_DEPOSIT',
                 'TICKET_READ_OWN','TICKET_PURCHASE','CARD_READ'
    );

-- ═══════════════════════════════════════════════════════════════
-- SEED: USERS
-- ═══════════════════════════════════════════════════════════════
INSERT INTO users (username, email, password_hash, full_name, status)
VALUES (
           'admin', 'admin@afc.vn',
           '$2a$10$PDjj6hQ88hOiA7YvrNSOQeinFqz4ghkipd3pTdQcYJaXWk1OOe3Mu',
           'System Admin', 'ACTIVE'
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
                                                       ((SELECT id FROM operators WHERE code = 'HURC'),      'HN_2A',     'Cát Linh - Hà Đông',         'METRO'),
                                                       ((SELECT id FROM operators WHERE code = 'HURC'),      'HN_3_1',    'Nhổn - Ga Hà Nội',            'METRO'),
                                                       ((SELECT id FROM operators WHERE code = 'TRANSERCO'), 'HN_BRT_01', 'BRT 01: Yên Nghĩa - Kim Mã',  'BUS'),
                                                       ((SELECT id FROM operators WHERE code = 'TRANSERCO'), 'HN_BUS_32', 'Buýt 32: Giáp Bát - Nhổn',    'BUS');

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
-- SEED: FARE RULES
-- ═══════════════════════════════════════════════════════════════
INSERT INTO fare_rules (code, mode, base_fare, rate_per_km,
                        min_price, max_price,
                        effective_from, status, version, created_by)
VALUES
    ('HN_METRO_STANDARD', 'METRO', 8000, 850, 8000, 30000,
     '2025-07-01', 'ACTIVE', 1, (SELECT id FROM users WHERE username = 'admin')),
    ('HN_BUS_STANDARD',   'BUS',   3000, 450, 3000, 30000,
     '2025-07-01', 'ACTIVE', 1, (SELECT id FROM users WHERE username = 'admin'));

-- ═══════════════════════════════════════════════════════════════
-- SEED: FARE PASS PRICES
-- ═══════════════════════════════════════════════════════════════
INSERT INTO fare_pass_prices (fare_rule_id, duration_type, duration_months, scope, amount)
VALUES
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
-- SEED: FARE DISCOUNTS
-- ═══════════════════════════════════════════════════════════════
INSERT INTO fare_discounts (passenger_type, discount_type, discount_value, effective_from, status)
VALUES
    ('STUDENT',  'PERCENT', 50, '2025-07-01', 'ACTIVE'),
    ('PRIORITY', 'PERCENT', 50, '2025-07-01', 'ACTIVE');

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
-- SEED: CARDS
-- ═══════════════════════════════════════════════════════════════
INSERT INTO cards (card_uid, status, type, supports_metro, supports_bus, activated_at)
VALUES
    ('CARD-ID-001',  'ACTIVE',    'IDENTIFIED', true, true,  CURRENT_TIMESTAMP),
    ('VMS-A3F20C91', 'ACTIVE',    'IDENTIFIED', true, true,  CURRENT_TIMESTAMP - INTERVAL '10 days'),
    ('VMS-B7E4D2F0', 'SUSPENDED', 'IDENTIFIED', true, true,  CURRENT_TIMESTAMP - INTERVAL '5 days'),
    ('VMS-C1A9E830', 'REVOKED',   'IDENTIFIED', true, false, CURRENT_TIMESTAMP - INTERVAL '20 days');

UPDATE cards
SET linked_user_id = (SELECT id FROM users WHERE username = 'admin'),
    linked_at      = CURRENT_TIMESTAMP
WHERE card_uid IN ('CARD-ID-001', 'VMS-A3F20C91', 'VMS-B7E4D2F0');

-- ═══════════════════════════════════════════════════════════════
-- SEED: BLACKLIST
-- ═══════════════════════════════════════════════════════════════
INSERT INTO blacklist (card_id, reason, added_by, added_at, is_active)
VALUES (
           (SELECT id FROM cards WHERE card_uid = 'VMS-B7E4D2F0'),
           'Báo cáo mất thẻ từ chủ thẻ',
           (SELECT id FROM users WHERE username = 'admin'),
           CURRENT_TIMESTAMP - INTERVAL '2 days',
           true
       );

-- ═══════════════════════════════════════════════════════════════
-- SEED: TICKETS
-- ═══════════════════════════════════════════════════════════════

-- ── JUNE 2026 SETTLEMENT — 4 tickets có trip ───────────────────
-- totalExpected = 14375 + 200000 + 140000 + 280000 = 634375đ → MATCH

-- Pool 1: SINGLE_TRIP 14375đ
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     from_station_id, to_station_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'SINGLE_TRIP', 14375.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           (SELECT id FROM stations WHERE code = 'HN_2A_01'),
           (SELECT id FROM stations WHERE code = 'HN_2A_08'),
           'METRO',
           CURRENT_DATE, CURRENT_DATE + INTERVAL '1 day',
           'ACTIVE'
       );

-- Pool 2a: METRO MONTHLY 200k → CARD-ID-001
INSERT INTO tickets (card_id, user_id, type, price, fare_rule_id,
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

-- Pool 2b: BUS SINGLE_ROUTE 140k
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status, scope)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 140000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           'BUS',
           DATE_TRUNC('month', CURRENT_DATE),
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '30 days',
           'ACTIVE', 'SINGLE_ROUTE'
       );

-- Pool 3: BUS MULTI_ROUTE 280k
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status, scope)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 280000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           'BUS',
           DATE_TRUNC('month', CURRENT_DATE),
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '30 days',
           'ACTIVE', 'MULTI_ROUTE'
       );

-- ── DISPLAY TICKETS — tháng 7, không có trip, không ảnh hưởng settlement ──

-- VMS-A3F20C91 METRO MONTHLY 200k
INSERT INTO tickets (card_id, user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM cards WHERE card_uid = 'VMS-A3F20C91'),
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 200000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           'METRO',
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month',
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month' + INTERVAL '30 days',
           'ACTIVE'
       );

-- SINGLE_TRIP STUDENT 7187.5đ
INSERT INTO tickets (user_id, type, price, fare_rule_id, discount_id,
                     from_station_id, to_station_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'SINGLE_TRIP', 7187.50,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           (SELECT id FROM fare_discounts WHERE passenger_type = 'STUDENT'),
           (SELECT id FROM stations WHERE code = 'HN_2A_01'),
           (SELECT id FROM stations WHERE code = 'HN_2A_08'),
           'METRO',
           CURRENT_DATE + INTERVAL '32 days',
           CURRENT_DATE + INTERVAL '33 days',
           'ACTIVE'
       );

-- SINGLE_TRIP USED — đặt trong tháng 5 tránh bị đếm vào June
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     from_station_id, to_station_id,
                     mode, valid_from, valid_to, status, used_at)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'SINGLE_TRIP', 14375.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           (SELECT id FROM stations WHERE code = 'HN_2A_01'),
           (SELECT id FROM stations WHERE code = 'HN_2A_08'),
           'METRO',
           '2026-05-20', '2026-05-21',
           'USED',
           CURRENT_TIMESTAMP - INTERVAL '35 days'
       );

-- METRO MONTHLY STUDENT 100k
INSERT INTO tickets (user_id, type, price, fare_rule_id, discount_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 100000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           (SELECT id FROM fare_discounts WHERE passenger_type = 'STUDENT'),
           'METRO',
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month',
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month' + INTERVAL '30 days',
           'ACTIVE'
       );

-- METRO WEEKLY 160k
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 160000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           'METRO',
           CURRENT_DATE + INTERVAL '32 days',
           CURRENT_DATE + INTERVAL '39 days',
           'ACTIVE'
       );

-- BUS SINGLE_ROUTE STUDENT 70k
INSERT INTO tickets (user_id, type, price, fare_rule_id, discount_id,
                     mode, valid_from, valid_to, status, scope)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 70000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           (SELECT id FROM fare_discounts WHERE passenger_type = 'STUDENT'),
           'BUS',
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month',
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month' + INTERVAL '30 days',
           'ACTIVE', 'SINGLE_ROUTE'
       );

-- BUS MULTI_ROUTE STUDENT 140k
INSERT INTO tickets (user_id, type, price, fare_rule_id, discount_id,
                     mode, valid_from, valid_to, status, scope)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 140000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           (SELECT id FROM fare_discounts WHERE passenger_type = 'STUDENT'),
           'BUS',
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month',
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month' + INTERVAL '30 days',
           'ACTIVE', 'MULTI_ROUTE'
       );

-- METRO EXPIRED tháng trước
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

-- BUS SINGLE_ROUTE tháng 8 (chưa hiệu lực)
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status, scope)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 140000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           'BUS',
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '2 months',
           DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '3 months' - INTERVAL '1 day',
           'ACTIVE', 'SINGLE_ROUTE'
       );

-- ── MAY 2026 — MISMATCH ─────────────────────────────────────────
-- totalExpected = 200000 + 15000 + 140000 + 280000 = 635000đ
-- METRO MONTHLY operator_id=NULL → 200000đ không allocate
-- totalActual = 435000đ, diff = 200000đ → MISMATCH

INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 200000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           'METRO', '2026-05-01', '2026-05-31', 'ACTIVE'
       );

INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     from_station_id, to_station_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'SINGLE_TRIP', 15000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           (SELECT id FROM stations WHERE code = 'HN_2A_01'),
           (SELECT id FROM stations WHERE code = 'HN_2A_09'),
           'METRO', '2026-05-15', '2026-05-16', 'ACTIVE'
       );

INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status, scope)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 140000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           'BUS', '2026-05-01', '2026-05-31', 'ACTIVE', 'SINGLE_ROUTE'
       );

INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status, scope)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 280000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           'BUS', '2026-05-01', '2026-05-31', 'ACTIVE', 'MULTI_ROUTE'
       );

-- ── APRIL 2026 — WARNING ────────────────────────────────────────
-- totalExpected = 14376 + 200000 + 140000 + 280000 = 634376đ
-- Pool 1 fare_amount=14375 (lệch 1đ)
-- totalActual = 634375đ, diff = 1đ < 10000đ → WARNING

INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     from_station_id, to_station_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'SINGLE_TRIP', 14376.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           (SELECT id FROM stations WHERE code = 'HN_2A_01'),
           (SELECT id FROM stations WHERE code = 'HN_2A_08'),
           'METRO', '2026-04-15', '2026-04-16', 'ACTIVE'
       );

INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 200000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           'METRO', '2026-04-01', '2026-04-30', 'ACTIVE'
       );

INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status, scope)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 140000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           'BUS', '2026-04-01', '2026-04-30', 'ACTIVE', 'SINGLE_ROUTE'
       );

INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     mode, valid_from, valid_to, status, scope)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'MONTHLY_PASS', 280000.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           'BUS', '2026-04-01', '2026-04-30', 'ACTIVE', 'MULTI_ROUTE'
       );

-- ═══════════════════════════════════════════════════════════════
-- SEED: TRIPS
-- ═══════════════════════════════════════════════════════════════

-- ── JUNE 2026 — MATCH ──────────────────────────────────────────

-- Pool 1
INSERT INTO trips (operator_id, ticket_id,
                   tap_in_station_id, tap_in_at,
                   tap_out_station_id, tap_out_at,
                   distance_km, fare_amount, ticket_type_used, transport_mode)
VALUES (
           (SELECT id FROM operators WHERE code = 'HURC'),
           (SELECT id FROM tickets WHERE type = 'SINGLE_TRIP' AND price = 14375
                                     AND status = 'ACTIVE' AND valid_from = CURRENT_DATE LIMIT 1),
       (SELECT id FROM stations WHERE code = 'HN_2A_01'),
    TIMESTAMP '2026-06-20 08:00:00',
       (SELECT id FROM stations WHERE code = 'HN_2A_08'),
    TIMESTAMP '2026-06-20 08:15:00',
    7.5, 14375.00, 'SINGLE_TRIP', 'METRO'
    );

-- Pool 2a
INSERT INTO trips (card_id, operator_id, ticket_id,
                   tap_in_station_id, tap_in_at,
                   tap_out_station_id, tap_out_at,
                   distance_km, ticket_type_used, transport_mode)
VALUES (
           (SELECT id FROM cards WHERE card_uid = 'CARD-ID-001'),
           (SELECT id FROM operators WHERE code = 'HURC'),
           (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND mode = 'METRO'
                                     AND card_id = (SELECT id FROM cards WHERE card_uid = 'CARD-ID-001') LIMIT 1),
       (SELECT id FROM stations WHERE code = 'HN_3_01'),
    TIMESTAMP '2026-06-18 09:00:00',
       (SELECT id FROM stations WHERE code = 'HN_3_06'),
    TIMESTAMP '2026-06-18 09:10:00',
    5.0, 'MONTHLY_PASS', 'METRO'
    );

-- Pool 2b
INSERT INTO trips (operator_id, ticket_id,
                   tap_in_station_id, tap_in_at,
                   tap_out_station_id, tap_out_at,
                   distance_km, ticket_type_used, transport_mode)
VALUES (
           (SELECT id FROM operators WHERE code = 'TRANSERCO'),
           (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND scope = 'SINGLE_ROUTE'
                                     AND price = 140000 AND valid_from = DATE_TRUNC('month', CURRENT_DATE) LIMIT 1),
       (SELECT id FROM stations WHERE code = 'BUS32_01'),
    TIMESTAMP '2026-06-15 07:30:00',
       (SELECT id FROM stations WHERE code = 'BUS32_05'),
    TIMESTAMP '2026-06-15 07:55:00',
    10.2, 'MONTHLY_PASS', 'BUS'
    );

-- Pool 3
INSERT INTO trips (operator_id, ticket_id,
                   tap_in_station_id, tap_in_at,
                   tap_out_station_id, tap_out_at,
                   distance_km, ticket_type_used, transport_mode)
VALUES
    (
        (SELECT id FROM operators WHERE code = 'TRANSERCO'),
        (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND scope = 'MULTI_ROUTE'
                                  AND price = 280000 AND valid_from = DATE_TRUNC('month', CURRENT_DATE) LIMIT 1),
    (SELECT id FROM stations WHERE code = 'BRT01_01'),
    TIMESTAMP '2026-06-10 08:00:00',
    (SELECT id FROM stations WHERE code = 'BRT01_10'),
    TIMESTAMP '2026-06-10 08:30:00',
    14.0, 'MONTHLY_PASS', 'BUS'
    ),
(
    (SELECT id FROM operators WHERE code = 'TRANSERCO'),
    (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND scope = 'MULTI_ROUTE'
        AND price = 280000 AND valid_from = DATE_TRUNC('month', CURRENT_DATE) LIMIT 1),
    (SELECT id FROM stations WHERE code = 'BUS32_05'),
    TIMESTAMP '2026-06-12 17:00:00',
    (SELECT id FROM stations WHERE code = 'BUS32_08'),
    TIMESTAMP '2026-06-12 17:15:00',
    8.0, 'MONTHLY_PASS', 'BUS'
),
(
    (SELECT id FROM operators WHERE code = 'HURC'),
    (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND scope = 'MULTI_ROUTE'
        AND price = 280000 AND valid_from = DATE_TRUNC('month', CURRENT_DATE) LIMIT 1),
    (SELECT id FROM stations WHERE code = 'HN_2A_01'),
    TIMESTAMP '2026-06-11 09:00:00',
    (SELECT id FROM stations WHERE code = 'HN_2A_07'),
    TIMESTAMP '2026-06-11 09:12:00',
    6.0, 'MONTHLY_PASS', 'METRO'
);

-- ── MAY 2026 — MISMATCH ─────────────────────────────────────────

-- Pool 2a METRO MONTHLY — operator_id NULL
INSERT INTO trips (ticket_id,
                   tap_in_station_id, tap_in_at,
                   tap_out_station_id, tap_out_at,
                   distance_km, ticket_type_used, transport_mode)
VALUES (
           (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND mode = 'METRO'
                                     AND valid_from = '2026-05-01' LIMIT 1),
       (SELECT id FROM stations WHERE code = 'HN_3_01'),
    TIMESTAMP '2026-05-15 08:00:00',
       (SELECT id FROM stations WHERE code = 'HN_3_06'),
    TIMESTAMP '2026-05-15 08:10:00',
    5.0, 'MONTHLY_PASS', 'METRO'
    );

-- Pool 1
INSERT INTO trips (operator_id, ticket_id,
                   tap_in_station_id, tap_in_at,
                   tap_out_station_id, tap_out_at,
                   distance_km, fare_amount, ticket_type_used, transport_mode)
VALUES (
           (SELECT id FROM operators WHERE code = 'HURC'),
           (SELECT id FROM tickets WHERE type = 'SINGLE_TRIP' AND price = 15000
                                     AND valid_from = '2026-05-15' LIMIT 1),
       (SELECT id FROM stations WHERE code = 'HN_2A_01'),
    TIMESTAMP '2026-05-15 09:00:00',
       (SELECT id FROM stations WHERE code = 'HN_2A_09'),
    TIMESTAMP '2026-05-15 09:18:00',
    8.8, 15000.00, 'SINGLE_TRIP', 'METRO'
    );

-- Pool 2b
INSERT INTO trips (operator_id, ticket_id,
                   tap_in_station_id, tap_in_at,
                   tap_out_station_id, tap_out_at,
                   distance_km, ticket_type_used, transport_mode)
VALUES (
           (SELECT id FROM operators WHERE code = 'TRANSERCO'),
           (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND scope = 'SINGLE_ROUTE'
                                     AND valid_from = '2026-05-01' LIMIT 1),
       (SELECT id FROM stations WHERE code = 'BUS32_01'),
    TIMESTAMP '2026-05-16 07:30:00',
       (SELECT id FROM stations WHERE code = 'BUS32_05'),
    TIMESTAMP '2026-05-16 07:55:00',
    10.2, 'MONTHLY_PASS', 'BUS'
    );

-- Pool 3
INSERT INTO trips (operator_id, ticket_id,
                   tap_in_station_id, tap_in_at,
                   tap_out_station_id, tap_out_at,
                   distance_km, ticket_type_used, transport_mode)
VALUES
    (
        (SELECT id FROM operators WHERE code = 'TRANSERCO'),
        (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND scope = 'MULTI_ROUTE'
                                  AND valid_from = '2026-05-01' LIMIT 1),
    (SELECT id FROM stations WHERE code = 'BRT01_01'),
    TIMESTAMP '2026-05-10 08:00:00',
    (SELECT id FROM stations WHERE code = 'BRT01_10'),
    TIMESTAMP '2026-05-10 08:30:00',
    14.0, 'MONTHLY_PASS', 'BUS'
    ),
(
    (SELECT id FROM operators WHERE code = 'TRANSERCO'),
    (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND scope = 'MULTI_ROUTE'
        AND valid_from = '2026-05-01' LIMIT 1),
    (SELECT id FROM stations WHERE code = 'BUS32_05'),
    TIMESTAMP '2026-05-12 17:00:00',
    (SELECT id FROM stations WHERE code = 'BUS32_08'),
    TIMESTAMP '2026-05-12 17:15:00',
    8.0, 'MONTHLY_PASS', 'BUS'
),
(
    (SELECT id FROM operators WHERE code = 'HURC'),
    (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND scope = 'MULTI_ROUTE'
        AND valid_from = '2026-05-01' LIMIT 1),
    (SELECT id FROM stations WHERE code = 'HN_2A_01'),
    TIMESTAMP '2026-05-11 09:00:00',
    (SELECT id FROM stations WHERE code = 'HN_2A_07'),
    TIMESTAMP '2026-05-11 09:12:00',
    6.0, 'MONTHLY_PASS', 'METRO'
);

-- ── APRIL 2026 — WARNING ─────────────────────────────────────────

-- Pool 1: fare_amount lệch 1đ
INSERT INTO trips (operator_id, ticket_id,
                   tap_in_station_id, tap_in_at,
                   tap_out_station_id, tap_out_at,
                   distance_km, fare_amount, ticket_type_used, transport_mode)
VALUES (
           (SELECT id FROM operators WHERE code = 'HURC'),
           (SELECT id FROM tickets WHERE type = 'SINGLE_TRIP' AND price = 14376
                                     AND valid_from = '2026-04-15' LIMIT 1),
       (SELECT id FROM stations WHERE code = 'HN_2A_01'),
    TIMESTAMP '2026-04-15 08:00:00',
       (SELECT id FROM stations WHERE code = 'HN_2A_08'),
    TIMESTAMP '2026-04-15 08:15:00',
    7.5, 14375.00, 'SINGLE_TRIP', 'METRO'
    );

-- Pool 2a
INSERT INTO trips (operator_id, ticket_id,
                   tap_in_station_id, tap_in_at,
                   tap_out_station_id, tap_out_at,
                   distance_km, ticket_type_used, transport_mode)
VALUES (
           (SELECT id FROM operators WHERE code = 'HURC'),
           (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND mode = 'METRO'
                                     AND valid_from = '2026-04-01' LIMIT 1),
       (SELECT id FROM stations WHERE code = 'HN_3_05'),
    TIMESTAMP '2026-04-15 09:00:00',
       (SELECT id FROM stations WHERE code = 'HN_3_08'),
    TIMESTAMP '2026-04-15 09:08:00',
    4.1, 'MONTHLY_PASS', 'METRO'
    );

-- Pool 2b
INSERT INTO trips (operator_id, ticket_id,
                   tap_in_station_id, tap_in_at,
                   tap_out_station_id, tap_out_at,
                   distance_km, ticket_type_used, transport_mode)
VALUES (
           (SELECT id FROM operators WHERE code = 'TRANSERCO'),
           (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND scope = 'SINGLE_ROUTE'
                                     AND valid_from = '2026-04-01' LIMIT 1),
       (SELECT id FROM stations WHERE code = 'BUS32_01'),
    TIMESTAMP '2026-04-16 07:30:00',
       (SELECT id FROM stations WHERE code = 'BUS32_05'),
    TIMESTAMP '2026-04-16 07:55:00',
    10.2, 'MONTHLY_PASS', 'BUS'
    );

-- Pool 3
INSERT INTO trips (operator_id, ticket_id,
                   tap_in_station_id, tap_in_at,
                   tap_out_station_id, tap_out_at,
                   distance_km, ticket_type_used, transport_mode)
VALUES
    (
        (SELECT id FROM operators WHERE code = 'TRANSERCO'),
        (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND scope = 'MULTI_ROUTE'
                                  AND valid_from = '2026-04-01' LIMIT 1),
    (SELECT id FROM stations WHERE code = 'BRT01_01'),
    TIMESTAMP '2026-04-10 08:00:00',
    (SELECT id FROM stations WHERE code = 'BRT01_10'),
    TIMESTAMP '2026-04-10 08:30:00',
    14.0, 'MONTHLY_PASS', 'BUS'
    ),
(
    (SELECT id FROM operators WHERE code = 'TRANSERCO'),
    (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND scope = 'MULTI_ROUTE'
        AND valid_from = '2026-04-01' LIMIT 1),
    (SELECT id FROM stations WHERE code = 'BUS32_05'),
    TIMESTAMP '2026-04-12 17:00:00',
    (SELECT id FROM stations WHERE code = 'BUS32_08'),
    TIMESTAMP '2026-04-12 17:15:00',
    8.0, 'MONTHLY_PASS', 'BUS'
),
(
    (SELECT id FROM operators WHERE code = 'HURC'),
    (SELECT id FROM tickets WHERE type = 'MONTHLY_PASS' AND scope = 'MULTI_ROUTE'
        AND valid_from = '2026-04-01' LIMIT 1),
    (SELECT id FROM stations WHERE code = 'HN_2A_01'),
    TIMESTAMP '2026-04-11 09:00:00',
    (SELECT id FROM stations WHERE code = 'HN_2A_07'),
    TIMESTAMP '2026-04-11 09:12:00',
    6.0, 'MONTHLY_PASS', 'METRO'
);
-- ═══════════════════════════════════════════════════════════════
-- SEED: ANOMALY TICKETS — tháng 3/2026, không ảnh hưởng settlement
-- ═══════════════════════════════════════════════════════════════

-- INCOMPLETE_TRIP ticket
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     from_station_id, to_station_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'SINGLE_TRIP', 14375.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           (SELECT id FROM stations WHERE code = 'HN_2A_03'),
           (SELECT id FROM stations WHERE code = 'HN_2A_08'),
           'METRO', '2026-03-20', '2026-03-21', 'ACTIVE'
       );

-- FARE_MISMATCH ticket
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     from_station_id, to_station_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'SINGLE_TRIP', 14375.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD'),
           (SELECT id FROM stations WHERE code = 'HN_2A_01'),
           (SELECT id FROM stations WHERE code = 'HN_2A_08'),
           'METRO', '2026-03-21', '2026-03-22', 'ACTIVE'
       );

-- DUPLICATE_TRANSACTION ticket
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     from_station_id, to_station_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           'SINGLE_TRIP', 6375.00,
           (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD'),
           (SELECT id FROM stations WHERE code = 'BUS32_01'),
           (SELECT id FROM stations WHERE code = 'BUS32_04'),
           'BUS', '2026-03-19', '2026-03-20', 'ACTIVE'
       );

-- ═══════════════════════════════════════════════════════════════
-- SEED: ANOMALY TRIPS — tháng 3/2026
-- ═══════════════════════════════════════════════════════════════

-- ═══════════════════════════════════════════════════════════════
-- SEED: ANOMALY TICKETS — tháng 3/2026, không ảnh hưởng settlement
-- ═══════════════════════════════════════════════════════════════

-- INCOMPLETE_TRIP ticket
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     from_station_id, to_station_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
    'SINGLE_TRIP', 14375.00,
       (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD' LIMIT 1),
       (SELECT id FROM stations WHERE code = 'HN_2A_03' LIMIT 1),
       (SELECT id FROM stations WHERE code = 'HN_2A_08' LIMIT 1),
    'METRO', '2026-03-20', '2026-03-21', 'ACTIVE'
    );

-- FARE_MISMATCH ticket
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     from_station_id, to_station_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
    'SINGLE_TRIP', 14375.00,
       (SELECT id FROM fare_rules WHERE code = 'HN_METRO_STANDARD' LIMIT 1),
       (SELECT id FROM stations WHERE code = 'HN_2A_01' LIMIT 1),
       (SELECT id FROM stations WHERE code = 'HN_2A_08' LIMIT 1),
    'METRO', '2026-03-21', '2026-03-22', 'ACTIVE'
    );

-- DUPLICATE_TRANSACTION ticket
INSERT INTO tickets (user_id, type, price, fare_rule_id,
                     from_station_id, to_station_id,
                     mode, valid_from, valid_to, status)
VALUES (
           (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
    'SINGLE_TRIP', 6375.00,
       (SELECT id FROM fare_rules WHERE code = 'HN_BUS_STANDARD' LIMIT 1),
       (SELECT id FROM stations WHERE code = 'BUS32_01' LIMIT 1),
       (SELECT id FROM stations WHERE code = 'BUS32_04' LIMIT 1),
    'BUS', '2026-03-19', '2026-03-20', 'ACTIVE'
    );

-- ═══════════════════════════════════════════════════════════════
-- SEED: ANOMALY TRIPS — tháng 3/2026
-- ═══════════════════════════════════════════════════════════════

-- INCOMPLETE_TRIP — không có tap_out
INSERT INTO trips (operator_id, ticket_id,
                   tap_in_station_id, tap_in_at,
                   distance_km, fare_amount, ticket_type_used, transport_mode)
VALUES (
           (SELECT id FROM operators WHERE code = 'HURC' LIMIT 1),
       (SELECT id FROM tickets WHERE valid_from = '2026-03-20'
           AND from_station_id = (SELECT id FROM stations WHERE code = 'HN_2A_03' LIMIT 1) LIMIT 1),
       (SELECT id FROM stations WHERE code = 'HN_2A_03' LIMIT 1),
    TIMESTAMP '2026-03-20 07:45:00',
    0, 0, 'SINGLE_TRIP', 'METRO'
    );

-- FARE_MISMATCH — Cấp 4 gửi 12000đ, đúng phải là 14375đ (lệch 16.5%)
INSERT INTO trips (operator_id, ticket_id,
                   tap_in_station_id, tap_in_at,
                   tap_out_station_id, tap_out_at,
                   distance_km, fare_amount, ticket_type_used, transport_mode)
VALUES (
           (SELECT id FROM operators WHERE code = 'HURC' LIMIT 1),
       (SELECT id FROM tickets WHERE valid_from = '2026-03-21'
           AND from_station_id = (SELECT id FROM stations WHERE code = 'HN_2A_01' LIMIT 1) LIMIT 1),
       (SELECT id FROM stations WHERE code = 'HN_2A_01' LIMIT 1),
    TIMESTAMP '2026-03-21 09:00:00',
       (SELECT id FROM stations WHERE code = 'HN_2A_08' LIMIT 1),
    TIMESTAMP '2026-03-21 09:15:00',
    7.5, 12000.00, 'SINGLE_TRIP', 'METRO'
    );

-- DUPLICATE_TRANSACTION — giao dịch bị gửi trùng
INSERT INTO trips (operator_id, ticket_id,
                   tap_in_station_id, tap_in_at,
                   tap_out_station_id, tap_out_at,
                   distance_km, fare_amount, ticket_type_used, transport_mode,
                   external_transaction_id)
VALUES (
           (SELECT id FROM operators WHERE code = 'TRANSERCO' LIMIT 1),
       (SELECT id FROM tickets WHERE valid_from = '2026-03-19'
           AND from_station_id = (SELECT id FROM stations WHERE code = 'BUS32_01' LIMIT 1) LIMIT 1),
       (SELECT id FROM stations WHERE code = 'BUS32_01' LIMIT 1),
    TIMESTAMP '2026-03-19 07:00:00',
       (SELECT id FROM stations WHERE code = 'BUS32_04' LIMIT 1),
    TIMESTAMP '2026-03-19 07:20:00',
    7.5, 6375.00, 'SINGLE_TRIP', 'BUS',
    'a1b2c3d4-0000-0000-0000-000000000001'
    );

-- ═══════════════════════════════════════════════════════════════
-- SEED: TRIP ANOMALIES
-- ═══════════════════════════════════════════════════════════════

INSERT INTO trip_anomalies (trip_id, anomaly_type, severity, description, detected_at, is_resolved)
VALUES (
           (SELECT id FROM trips WHERE tap_in_at = TIMESTAMP '2026-03-20 07:45:00' LIMIT 1),
    'INCOMPLETE_TRIP', 'WARNING',
    'Hành khách quẹt vào tại HN_2A_03 lúc 07:45 nhưng không quẹt ra — chuyến đi không hoàn thành',
    TIMESTAMP '2026-03-20 09:45:00',
    false
    );

INSERT INTO trip_anomalies (trip_id, anomaly_type, severity, description, detected_at, is_resolved, corrected_fare)
VALUES (
           (SELECT id FROM trips WHERE tap_in_at = TIMESTAMP '2026-03-21 09:00:00' LIMIT 1),
    'FARE_MISMATCH', 'WARNING',
    'Giá vé Cấp 4 gửi 12,000đ lệch so với Cấp 5 tính 14,375đ (lệch 16.5% vượt ngưỡng 5%)',
    TIMESTAMP '2026-03-21 09:15:00',
    false,
    14375.00
    );

INSERT INTO trip_anomalies (trip_id, anomaly_type, severity, description, detected_at, is_resolved)
VALUES (
           (SELECT id FROM trips WHERE external_transaction_id = 'a1b2c3d4-0000-0000-0000-000000000001' LIMIT 1),
    'DUPLICATE_TRANSACTION', 'ERROR',
    'Giao dịch a1b2c3d4-0000-0000-0000-000000000001 bị gửi trùng lặp từ Cấp 4 — đã bỏ qua lần thứ 2',
    TIMESTAMP '2026-03-19 07:00:01',
    false
    );