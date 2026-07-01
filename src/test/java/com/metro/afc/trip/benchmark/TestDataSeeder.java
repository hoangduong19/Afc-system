package com.metro.afc.trip.benchmark;


import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class TestDataSeeder {

    private final JdbcTemplate jdbcTemplate;

    public TestDataSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 1. Seed Operators (Có thêm cột mode mặc định là BUS hoặc METRO) */
    public UUID seedOperatorAndRoute() {
        UUID operatorId = UUID.randomUUID();
        UUID routeId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO operators (id, code, name, status, mode, created_at) VALUES (?, 'OP_METRO_01', 'Metro Operator', 'ACTIVE', 'METRO', NOW())",
                operatorId
        );

        jdbcTemplate.update(
                "INSERT INTO routes (id, operator_id, code, name, type, created_at) VALUES (?, ?, 'R_LINE1', 'Line 1', 'METRO', NOW())",
                routeId, operatorId
        );

        return routeId;
    }

    public List<String> seedOperators(int count) {
        List<String> codes = new ArrayList<>(count);
        String sql = "INSERT INTO operators (id, code, name, status, mode, created_at) VALUES (?, ?, ?, 'ACTIVE', ?, NOW())";
        List<Object[]> batchArgs = new ArrayList<>();
        FareMode[] modes = FareMode.values();

        for (int i = 0; i < count; i++) {
            String code = "OP" + i;
            String modeStr = modes[i % modes.length].name();
            // Khớp với ràng buộc CHECK hoặc cấu hình (chỉ nhận METRO/BUS/ANY, fallback về BUS nếu là ANY)
            if ("ANY".equals(modeStr)) modeStr = "BUS";

            batchArgs.add(new Object[]{
                    UUID.randomUUID(), code, "Operator " + i, modeStr
            });
            codes.add(code);
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
        return codes;
    }

    /** 2. Seed Stations */
    public List<String> seedStations(int count, UUID routeId) {
        List<String> codes = new ArrayList<>(count);
        String sql = "INSERT INTO stations (id, route_id, code, name, km_marker, station_order, created_at) VALUES (?, ?, ?, ?, ?, ?, NOW())";
        List<Object[]> batchArgs = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String code = "STA" + String.format("%03d", i);
            batchArgs.add(new Object[]{
                    UUID.randomUUID(), routeId, code, "Station " + i, BigDecimal.valueOf(i * 1.2), i
            });
            codes.add(code);
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
        return codes;
    }

    /** 3. Seed Cards (Đã BỎ hoàn toàn cột created_by không tồn tại) */
    public List<String> seedCards(int count) {
        List<String> uids = new ArrayList<>(count);
        // Khớp 100% với schema: id, card_uid, status, type, supports_metro, supports_bus, linked_user_id, created_at
        String sql = "INSERT INTO cards (id, card_uid, status, type, supports_metro, supports_bus, linked_user_id, created_at, updated_at) " +
                "VALUES (?, ?, 'ACTIVE', 'IDENTIFIED', ?, ?, ?, NOW(), NOW())";

        List<Object[]> batchArgs = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String uid = "CARD" + String.format("%08d", i);
            UUID linkedUser = (i % 10 < 7) ? UUID.randomUUID() : null; // 70% đã liên kết user

            batchArgs.add(new Object[]{
                    UUID.randomUUID(),
                    uid,
                    true, // supports_metro
                    true, // supports_bus
                    linkedUser
            });
            uids.add(uid);
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
        return uids;
    }

    /** 4. Seed Fare Rules (Giữ nguyên cấu trúc bảng fare_rules gốc) */
    public void seedFareRules() {
        // Tạo 1 User ID thật tồn tại để thỏa mãn ràng buộc FK created_by REFERENCES users(id)
        UUID systemUserId = UUID.randomUUID();

        // Chỉ định rõ ràng tất cả các cột bắt buộc, bao gồm cả created_at và updated_at để tránh lỗi NOT-NULL
        jdbcTemplate.update(
                "INSERT INTO users (id, username, email, password_hash, full_name, status, created_at, updated_at) " +
                        "VALUES (?, 'system_bench', 'bench@metro.com', 'hash', 'System Bench', 'ACTIVE', NOW(), NOW())",
                systemUserId
        );

        String sql = "INSERT INTO fare_rules (id, code, mode, base_fare, rate_per_km, min_price, max_price, status, version, effective_from, created_by, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', 1, NOW() - INTERVAL '1 MONTH', ?, NOW())";
        List<Object[]> batchArgs = new ArrayList<>();

        for (FareMode mode : FareMode.values()) {
            batchArgs.add(new Object[]{
                    UUID.randomUUID(),
                    "FR_" + mode.name(),
                    mode.name(),
                    BigDecimal.valueOf(8000),
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(8000),
                    BigDecimal.valueOf(20000),
                    systemUserId
            });
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}