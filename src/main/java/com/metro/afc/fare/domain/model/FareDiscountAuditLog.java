package com.metro.afc.fare.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fare_discount_audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FareDiscountAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "discount_id", nullable = false, columnDefinition = "uuid")
    private UUID discountId;

    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private String oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private String newValue;

    @Column(name = "changed_by", nullable = false, columnDefinition = "uuid")
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public static FareDiscountAuditLog of(UUID discountId, String changeType,
                                          String oldValue, String newValue,
                                          UUID changedBy) {
        FareDiscountAuditLog log = new FareDiscountAuditLog();
        log.discountId = discountId;
        log.changeType = changeType;
        log.oldValue   = oldValue;
        log.newValue   = newValue;
        log.changedBy  = changedBy;
        log.changedAt  = Instant.now();
        return log;
    }
}