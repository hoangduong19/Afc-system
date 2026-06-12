-- ═══════════════════════════════════════════════════════════════
-- AFC SYSTEM - PostgreSQL Schema (RBAC Updated)
-- ═══════════════════════════════════════════════════════════════

DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ═══════════════════════════════════════════════════════════════
-- 1. USER & AUTH (RBAC)
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE permissions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    resource_group  VARCHAR(50)  NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role_permissions (
    role_id         UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id   UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username        VARCHAR(100) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255),
    phone           VARCHAR(20),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_users_email ON users(email);

CREATE TABLE user_roles (
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID REFERENCES users(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);

-- ═══════════════════════════════════════════════════════════════
-- 2. ORGANIZATION
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE operators (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_operator_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE routes (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    operator_id UUID NOT NULL REFERENCES operators(id),
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_route_type CHECK (type IN ('METRO', 'BUS'))
);

CREATE INDEX idx_routes_operator ON routes(operator_id);

CREATE TABLE stations (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    route_id        UUID NOT NULL REFERENCES routes(id),
    code            VARCHAR(50)  NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    km_marker       DECIMAL(10, 3) NOT NULL,
    station_order   INTEGER NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (route_id, station_order)
);

CREATE INDEX idx_stations_route ON stations(route_id);

-- ═══════════════════════════════════════════════════════════════
-- 3. STAFF & SHIFT
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE staff_assignments (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id),
    route_id    UUID REFERENCES routes(id),
    station_id  UUID REFERENCES stations(id),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_assignment_target CHECK (
        (route_id IS NOT NULL AND station_id IS NULL) OR
        (route_id IS NULL AND station_id IS NOT NULL)
    )
);

CREATE INDEX idx_staff_assignment_user    ON staff_assignments(user_id);
CREATE INDEX idx_staff_assignment_station ON staff_assignments(station_id);

CREATE TABLE shifts (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id),
    station_id  UUID NOT NULL REFERENCES stations(id),
    start_time  TIMESTAMP NOT NULL,
    end_time    TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_shift_time CHECK (end_time > start_time)
);

CREATE INDEX idx_shifts_user    ON shifts(user_id);
CREATE INDEX idx_shifts_station ON shifts(station_id);
CREATE INDEX idx_shifts_time    ON shifts(start_time, end_time);

-- ═══════════════════════════════════════════════════════════════
-- 4. CARD
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE cards (
    id                   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    card_uid             VARCHAR(100) NOT NULL UNIQUE,
    status               VARCHAR(20)  NOT NULL DEFAULT 'CREATED',
    type                 VARCHAR(20)  NOT NULL DEFAULT 'ANON',
    supports_metro       BOOLEAN NOT NULL DEFAULT TRUE,
    supports_bus         BOOLEAN NOT NULL DEFAULT TRUE,
    issued_at_station_id UUID REFERENCES stations(id),
    linked_user_id       UUID REFERENCES users(id),
    activated_at         TIMESTAMP,
    linked_at            TIMESTAMP,
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_card_status CHECK (status IN ('CREATED','ISSUED','ACTIVE','SUSPENDED','REVOKED','DISPOSED')),
    CONSTRAINT chk_card_type   CHECK (type   IN ('ANON','IDENTIFIED'))
);

CREATE INDEX idx_cards_uid    ON cards(card_uid);
CREATE INDEX idx_cards_user   ON cards(linked_user_id);
CREATE INDEX idx_cards_status ON cards(status);

CREATE TABLE card_status_history (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    card_id     UUID NOT NULL REFERENCES cards(id),
    from_status VARCHAR(20),
    to_status   VARCHAR(20) NOT NULL,
    reason      TEXT,
    changed_by  UUID REFERENCES users(id),
    changed_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_card_history_card ON card_status_history(card_id);

CREATE TABLE card_link_history (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    card_id         UUID NOT NULL REFERENCES cards(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    action          VARCHAR(20) NOT NULL,
    merged_balance  DECIMAL(15, 2),
    performed_by    UUID REFERENCES users(id),
    performed_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_link_action CHECK (action IN ('LINK','UNLINK'))
);

CREATE INDEX idx_card_link_card ON card_link_history(card_id);
CREATE INDEX idx_card_link_user ON card_link_history(user_id);

CREATE TABLE blacklist (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    card_id     UUID NOT NULL REFERENCES cards(id),
    reason      TEXT NOT NULL,
    added_by    UUID NOT NULL REFERENCES users(id),
    added_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    removed_by  UUID REFERENCES users(id),
    removed_at  TIMESTAMP,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_blacklist_card   ON blacklist(card_id);
CREATE INDEX idx_blacklist_active ON blacklist(card_id, is_active);

-- ═══════════════════════════════════════════════════════════════
-- 5. FARE
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE fare_rules (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code            VARCHAR(50) NOT NULL,
    mode            VARCHAR(20) NOT NULL,
    base_fare       DECIMAL(15, 2) NOT NULL,
    rate_per_km     DECIMAL(15, 2) NOT NULL,
    min_price       DECIMAL(15, 2) NOT NULL,
    max_price       DECIMAL(15, 2) NOT NULL,
    effective_from  DATE NOT NULL,
    effective_to    DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version         INTEGER NOT NULL DEFAULT 1,
    created_by      UUID NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_fare_mode   CHECK (mode   IN ('METRO','BUS','ANY')),
    CONSTRAINT chk_fare_status CHECK (status IN ('ACTIVE','INACTIVE')),
    CONSTRAINT chk_fare_price  CHECK (min_price <= max_price AND base_fare >= 0)
);

CREATE INDEX idx_fare_rules_effective ON fare_rules(effective_from, effective_to);
CREATE INDEX idx_fare_rules_status    ON fare_rules(status);

CREATE TABLE fare_rule_audit_log (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    fare_rule_id UUID NOT NULL REFERENCES fare_rules(id),
    change_type  VARCHAR(20) NOT NULL,
    old_value    JSONB,
    new_value    JSONB,
    reason       TEXT,
    changed_by   UUID NOT NULL REFERENCES users(id),
    changed_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_fare_change_type CHECK (change_type IN ('CREATED','UPDATED','DISABLED'))
);

CREATE INDEX idx_fare_audit_rule ON fare_rule_audit_log(fare_rule_id);

CREATE TABLE fare_discounts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    passenger_type  VARCHAR(50) NOT NULL,
    discount_type   VARCHAR(20) NOT NULL,
    discount_value  DECIMAL(10, 2) NOT NULL,
    effective_from  DATE NOT NULL,
    effective_to    DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_passenger_type CHECK (passenger_type IN ('STUDENT','SENIOR','PRIORITY')),
    CONSTRAINT chk_discount_type   CHECK (discount_type  IN ('PERCENT','FIXED'))
);

CREATE TABLE fare_discount_audit_log (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    discount_id  UUID NOT NULL REFERENCES fare_discounts(id),
    change_type  VARCHAR(20) NOT NULL,
    old_value    JSONB,
    new_value    JSONB,
    changed_by   UUID NOT NULL REFERENCES users(id),
    changed_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_discount_audit ON fare_discount_audit_log(discount_id);

CREATE TABLE interop_configs (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    from_operator_id  UUID NOT NULL REFERENCES operators(id),
    to_operator_id    UUID NOT NULL REFERENCES operators(id),
    transfer_discount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    enabled           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_interop_different CHECK (from_operator_id <> to_operator_id),
    UNIQUE (from_operator_id, to_operator_id)
);

-- ═══════════════════════════════════════════════════════════════
-- 6. WALLET
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE wallets (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID REFERENCES users(id),
    card_id     UUID REFERENCES cards(id),
    owner_type  VARCHAR(20) NOT NULL,
    balance     DECIMAL(15, 2) NOT NULL DEFAULT 0,
    debt_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_owner_type CHECK (owner_type IN ('ANON','ACCOUNT')),
    CONSTRAINT chk_wallet_owner_xor CHECK (
        (user_id IS NOT NULL AND card_id IS NULL) OR
        (user_id IS NULL AND card_id IS NOT NULL)
    )
);

CREATE UNIQUE INDEX idx_wallet_user ON wallets(user_id) WHERE user_id IS NOT NULL;
CREATE UNIQUE INDEX idx_wallet_card ON wallets(card_id) WHERE card_id IS NOT NULL;

CREATE TABLE wallet_transactions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet_id       UUID NOT NULL REFERENCES wallets(id),
    type            VARCHAR(20) NOT NULL,
    amount          DECIMAL(15, 2) NOT NULL,
    balance_before  DECIMAL(15, 2) NOT NULL,
    balance_after   DECIMAL(15, 2) NOT NULL,
    reference_type  VARCHAR(30),
    reference_id    UUID,
    reason          VARCHAR(100),
    note            TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_tx_type CHECK (type IN ('DEPOSIT','DEDUCTION','TRANSFER_IN','TRANSFER_OUT','REFUND','MERGE'))
);

CREATE INDEX idx_wallet_tx_wallet  ON wallet_transactions(wallet_id);
CREATE INDEX idx_wallet_tx_ref     ON wallet_transactions(reference_type, reference_id);
CREATE INDEX idx_wallet_tx_created ON wallet_transactions(created_at);

-- ═══════════════════════════════════════════════════════════════
-- 7. TICKET
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE tickets (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    card_id          UUID REFERENCES cards(id),
    user_id          UUID REFERENCES users(id),
    type             VARCHAR(20) NOT NULL,
    price            DECIMAL(15, 2) NOT NULL,
    fare_rule_id     UUID REFERENCES fare_rules(id),
    discount_id      UUID REFERENCES fare_discounts(id),
    from_station_id  UUID REFERENCES stations(id),
    to_station_id    UUID REFERENCES stations(id),
    mode             VARCHAR(20),
    valid_from       DATE NOT NULL,
    valid_to         DATE NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'UNUSED',
    purchased_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at          TIMESTAMP,
    CONSTRAINT chk_ticket_type     CHECK (type   IN ('SINGLE_TRIP','MONTHLY_PASS')),
    CONSTRAINT chk_ticket_status   CHECK (status IN ('UNUSED','ACTIVE','USED','EXPIRED')),
    CONSTRAINT chk_ticket_validity CHECK (valid_to >= valid_from),
    CONSTRAINT chk_ticket_monthly_user CHECK (
        (type = 'MONTHLY_PASS' AND user_id IS NOT NULL) OR
        (type = 'SINGLE_TRIP')
    )
);

CREATE INDEX idx_tickets_card     ON tickets(card_id);
CREATE INDEX idx_tickets_user     ON tickets(user_id);
CREATE INDEX idx_tickets_status   ON tickets(status);
CREATE INDEX idx_tickets_validity ON tickets(valid_from, valid_to);

-- ═══════════════════════════════════════════════════════════════
-- 8. TRIP & TAP
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE trips (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    card_id               UUID NOT NULL REFERENCES cards(id),
    operator_id           UUID REFERENCES operators(id),
    tap_in_station_id     UUID NOT NULL REFERENCES stations(id),
    tap_in_gate_id        VARCHAR(50) NOT NULL,
    tap_in_at             TIMESTAMP NOT NULL,
    tap_out_station_id    UUID REFERENCES stations(id),
    tap_out_gate_id       VARCHAR(50),
    tap_out_at            TIMESTAMP,
    distance_km           DECIMAL(10, 3),
    fare_amount           DECIMAL(15, 2),
    payment_method        VARCHAR(20),
    ticket_type_used      VARCHAR(20),
    ticket_id             UUID REFERENCES tickets(id),
    wallet_transaction_id UUID REFERENCES wallet_transactions(id),
    status                VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    debt_amount           DECIMAL(15, 2),
    created_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    external_transaction_id UUID UNIQUE,
    CONSTRAINT chk_trip_status     CHECK (status         IN ('IN_PROGRESS','COMPLETED','DEBT')),
    CONSTRAINT chk_payment_method  CHECK (payment_method IN ('TICKET','WALLET','PREPAID'))
);

CREATE INDEX idx_trips_card    ON trips(card_id);
CREATE INDEX idx_trips_operator ON trips(operator_id);
CREATE INDEX idx_trips_status  ON trips(status);
CREATE INDEX idx_trips_tap_in  ON trips(tap_in_at);
CREATE INDEX idx_trips_tap_out ON trips(tap_out_at);

CREATE TABLE tap_events (
    id                        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id                   UUID NOT NULL REFERENCES trips(id),
    event_type                VARCHAR(20) NOT NULL,
    station_id                UUID NOT NULL REFERENCES stations(id),
    gate_id                   VARCHAR(50) NOT NULL,
    layer1_exists             BOOLEAN NOT NULL,
    layer2_active             BOOLEAN NOT NULL,
    layer3_not_blacklisted    BOOLEAN NOT NULL,
    layer4_has_fund_or_ticket BOOLEAN NOT NULL,
    layer5_no_duplicate       BOOLEAN NOT NULL,
    failed_reason             TEXT,
    tapped_at                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_tap_type CHECK (event_type IN ('TAP_IN','TAP_OUT'))
);

CREATE INDEX idx_tap_events_trip    ON tap_events(trip_id);
CREATE INDEX idx_tap_events_station ON tap_events(station_id);
CREATE INDEX idx_tap_events_time    ON tap_events(tapped_at);

CREATE TABLE trip_anomalies (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id      UUID REFERENCES trips(id),
    anomaly_type VARCHAR(50)  NOT NULL,
    severity     VARCHAR(20)  NOT NULL,
    description  TEXT         NOT NULL,
    detected_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at  TIMESTAMP,
    is_resolved  BOOLEAN      NOT NULL DEFAULT FALSE
);
-- ═══════════════════════════════════════════════════════════════
-- 9. SETTLEMENT & RECONCILIATION
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE revenue_share_rules (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    operator_id      UUID NOT NULL REFERENCES operators(id),
    share_model      VARCHAR(30) NOT NULL,
    params           JSONB,
    share_percentage DECIMAL(5, 2),
    effective_from   DATE NOT NULL,
    effective_to     DATE,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_share_model CHECK (share_model IN ('KM_BASED','FIXED','TRIP_BASED'))
);

CREATE INDEX idx_revenue_rule_operator ON revenue_share_rules(operator_id);

CREATE TABLE revenue_share_rule_audit_log (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    rule_id     UUID NOT NULL REFERENCES revenue_share_rules(id),
    change_type VARCHAR(20) NOT NULL,
    old_value   JSONB,
    new_value   JSONB,
    changed_by  UUID NOT NULL REFERENCES users(id),
    changed_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE settlements (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    period                VARCHAR(7) NOT NULL UNIQUE,
    status                VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    total_expected        DECIMAL(15, 2),
    total_actual          DECIMAL(15, 2),
    diff_amount           DECIMAL(15, 2),
    reconciliation_status VARCHAR(20),
    tolerance_threshold   DECIMAL(15, 2) DEFAULT 100,
    ran_by                UUID NOT NULL REFERENCES users(id),
    ran_at                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at          TIMESTAMP,
    CONSTRAINT chk_settlement_status      CHECK (status                IN ('DRAFT','CONFIRMED')),
    CONSTRAINT chk_reconciliation_status  CHECK (reconciliation_status IN ('MATCH','WARNING','MISMATCH')),
    CONSTRAINT chk_period_format          CHECK (period ~ '^[0-9]{4}-(0[1-9]|1[0-2])$')
);

CREATE INDEX idx_settlements_period ON settlements(period);

CREATE TABLE company_shares (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    settlement_id       UUID NOT NULL REFERENCES settlements(id),
    operator_id         UUID NOT NULL REFERENCES operators(id),
    total_km            DECIMAL(15, 3) NOT NULL DEFAULT 0,
    total_trips         INTEGER NOT NULL DEFAULT 0,
    expected_revenue    DECIMAL(15, 2) NOT NULL DEFAULT 0,
    share_amount        DECIMAL(15, 2) NOT NULL DEFAULT 0,
    rounding_adjustment DECIMAL(15, 2) DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (settlement_id, operator_id)
);

CREATE INDEX idx_company_share_settlement ON company_shares(settlement_id);

CREATE TABLE reconciliation_logs (
    id                 UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    settlement_id      UUID NOT NULL REFERENCES settlements(id),
    category           VARCHAR(50) NOT NULL,
    discrepancy_amount DECIMAL(15, 2) NOT NULL,
    trip_count         INTEGER NOT NULL DEFAULT 0,
    note               TEXT,
    detail             JSONB,
    logged_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recon_log_settlement ON reconciliation_logs(settlement_id);

-- ═══════════════════════════════════════════════════════════════
-- 10. AUDIT LOG
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE audit_log (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type  VARCHAR(100) NOT NULL,
    entity_id    UUID NOT NULL,
    action       VARCHAR(50)  NOT NULL,
    before_value JSONB,
    after_value  JSONB,
    ip_address   VARCHAR(45),
    performed_by UUID REFERENCES users(id),
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_user   ON audit_log(performed_by);
CREATE INDEX idx_audit_time   ON audit_log(performed_at);

-- ═══════════════════════════════════════════════════════════════
-- TRIGGERS
-- ═══════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at   BEFORE UPDATE ON users   FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_cards_updated_at   BEFORE UPDATE ON cards   FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_wallets_updated_at BEFORE UPDATE ON wallets FOR EACH ROW EXECUTE FUNCTION update_updated_at();

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
    ('ACC_ADMIN',     'Account Admin',  'Quản trị toàn bộ hệ thống'),
    ('STATION_STAFF', 'Station Staff',  'Nhân viên tại trạm'),
    ('PASSENGER',     'Passenger',      'Hành khách');

-- ═══════════════════════════════════════════════════════════════
-- SEED: ROLE_PERMISSIONS
-- ═══════════════════════════════════════════════════════════════

-- ACC_ADMIN: tất cả permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'ACC_ADMIN';

-- STATION_STAFF
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'STATION_STAFF'
  AND p.code IN (
    'CARD_READ', 'CARD_ISSUE', 'CARD_ACTIVATE',
    'CARD_SUSPEND', 'CARD_BLACKLIST', 'CARD_LINK',
    'TAP_PROCESS',
    'TRIP_READ',
    'WALLET_READ',
    'TICKET_READ',
    'SHIFT_READ', 'SHIFT_UPDATE',
    'OPERATOR_READ'
);

-- PASSENGER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'PASSENGER'
  AND p.code IN (
    'TRIP_READ_OWN',
    'WALLET_READ_OWN', 'WALLET_DEPOSIT',
    'TICKET_READ_OWN', 'TICKET_PURCHASE',
    'CARD_READ'
);
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE revenue_share_rules
    ADD COLUMN created_by UUID REFERENCES users(id),
    ADD COLUMN version    INTEGER NOT NULL DEFAULT 1;

ALTER TABLE tickets
    ADD COLUMN scope VARCHAR(30);

ALTER TABLE tickets
    ADD CONSTRAINT chk_ticket_scope
        CHECK (scope IS NULL OR scope IN ('SINGLE_ROUTE', 'MULTI_ROUTE'));

ALTER TABLE tickets
    ADD CONSTRAINT chk_ticket_mode_scope
        CHECK (
            (mode = 'BUS' AND scope IS NOT NULL)
                OR
            (mode <> 'BUS' AND scope IS NULL)
            );
ALTER TABLE trips ALTER COLUMN card_id DROP NOT NULL;
ALTER TABLE trip_anomalies ADD COLUMN resolve_notes TEXT;

ALTER TABLE trip_anomalies
    ADD COLUMN corrected_fare DECIMAL(15, 2);