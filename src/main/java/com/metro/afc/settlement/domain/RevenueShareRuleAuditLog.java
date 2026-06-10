package com.metro.afc.settlement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "revenue_share_rule_audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevenueShareRuleAuditLog {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "rule_id", nullable = false, columnDefinition = "uuid")
    private UUID ruleId;

    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private Map<String, Object> oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private Map<String, Object> newValue;

    @Column(name = "changed_by", nullable = false, columnDefinition = "uuid")
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    public static RevenueShareRuleAuditLog of(UUID ruleId, String changeType,
                                              Map<String, Object> oldValue,
                                              Map<String, Object> newValue,
                                              UUID changedBy) {
        RevenueShareRuleAuditLog log = new RevenueShareRuleAuditLog();
        log.id         = UUID.randomUUID();
        log.ruleId     = ruleId;
        log.changeType = changeType;
        log.oldValue   = oldValue;
        log.newValue   = newValue;
        log.changedBy  = changedBy;
        log.changedAt  = Instant.now();
        return log;
    }
}