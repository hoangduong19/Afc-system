package com.metro.afc.fare.domain.model;

import com.metro.afc.fare.domain.event.FareChangeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fare_rule_audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FareRuleAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "fare_rule_id", nullable = false, columnDefinition = "uuid")
    private UUID fareRuleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private FareChangeType changeType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private String oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private String newValue;

    @Column(name = "reason")
    private String reason;

    @Column(name = "changed_by", nullable = false, columnDefinition = "uuid")
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    public static FareRuleAuditLog created(UUID fareRuleId,
                                           String snapshot,
                                           UUID changedBy) {
        FareRuleAuditLog log = new FareRuleAuditLog();
        log.fareRuleId  = fareRuleId;
        log.changeType  = FareChangeType.CREATED;
        log.oldValue    = null;
        log.newValue    = snapshot;
        log.changedBy   = changedBy;
        log.changedAt   = Instant.now();
        return log;
    }

    public static FareRuleAuditLog updated(UUID fareRuleId,
                                           String oldSnapshot,
                                           String newSnapshot,
                                           UUID changedBy) {
        FareRuleAuditLog log = new FareRuleAuditLog();
        log.fareRuleId  = fareRuleId;
        log.changeType  = FareChangeType.UPDATED;
        log.oldValue    = oldSnapshot;
        log.newValue    = newSnapshot;
        log.changedBy   = changedBy;
        log.changedAt   = Instant.now();
        return log;
    }

    public static FareRuleAuditLog disabled(UUID fareRuleId,
                                            String oldSnapshot,
                                            UUID changedBy) {
        FareRuleAuditLog log = new FareRuleAuditLog();
        log.fareRuleId  = fareRuleId;
        log.changeType  = FareChangeType.DISABLED;
        log.oldValue    = oldSnapshot;
        log.newValue    = null;
        log.changedBy   = changedBy;
        log.changedAt   = Instant.now();
        return log;
    }
}