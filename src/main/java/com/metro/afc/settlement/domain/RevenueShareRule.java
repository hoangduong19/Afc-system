package com.metro.afc.settlement.domain;

import com.metro.afc.settlement.domain.enums.RuleStatus;
import com.metro.afc.settlement.domain.enums.ShareModel;
import com.metro.afc.settlement.domain.events.revenueShareRule.RevenueShareRuleCreatedEvent;
import com.metro.afc.settlement.domain.events.revenueShareRule.RevenueShareRuleDisabledEvent;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "revenue_share_rules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevenueShareRule extends AbstractAggregateRoot<RevenueShareRule> {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "operator_id", nullable = false, columnDefinition = "uuid")
    private UUID operatorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "share_model", nullable = false, length = 30)
    private ShareModel shareModel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> params;

    @Column(name = "share_percentage", precision = 5, scale = 2)
    private BigDecimal sharePercentage;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RuleStatus status;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "created_by", nullable = false, columnDefinition = "uuid")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ── Factory ──────────────────────────────────────────────────

    public static RevenueShareRule create(UUID operatorId, ShareModel shareModel,
                                          Map<String, Object> params,
                                          BigDecimal sharePercentage,
                                          LocalDate effectiveFrom, LocalDate effectiveTo,
                                          UUID createdBy) {
        validate(shareModel, sharePercentage);

        RevenueShareRule r = new RevenueShareRule();
        r.id               = UUID.randomUUID();
        r.operatorId       = operatorId;
        r.shareModel       = shareModel;
        r.params           = params;
        r.sharePercentage  = sharePercentage;
        r.effectiveFrom    = effectiveFrom;
        r.effectiveTo      = effectiveTo;
        r.status           = RuleStatus.ACTIVE;
        r.version          = 1;
        r.createdBy        = createdBy;
        r.registerEvent(new RevenueShareRuleCreatedEvent(r));
        return r;
    }

    // ── Domain behavior ──────────────────────────────────────────

    public RevenueShareRule createNewVersion(ShareModel shareModel,
                                             Map<String, Object> params,
                                             BigDecimal sharePercentage,
                                             LocalDate effectiveFrom,
                                             LocalDate effectiveTo,
                                             UUID updatedBy) {
        validate(shareModel, sharePercentage);

        // Disable version hiện tại
        this.status      = RuleStatus.INACTIVE;
        this.effectiveTo = effectiveFrom.minusDays(1);
        this.registerEvent(new RevenueShareRuleDisabledEvent(this, updatedBy));

        // Tạo version mới
        RevenueShareRule newVersion = new RevenueShareRule();
        newVersion.id              = UUID.randomUUID();
        newVersion.operatorId      = this.operatorId;
        newVersion.shareModel      = shareModel;
        newVersion.params          = params;
        newVersion.sharePercentage = sharePercentage;
        newVersion.effectiveFrom   = effectiveFrom;
        newVersion.effectiveTo     = effectiveTo;
        newVersion.status          = RuleStatus.ACTIVE;
        newVersion.version         = this.version + 1;
        newVersion.createdBy       = updatedBy;
        newVersion.registerEvent(new RevenueShareRuleCreatedEvent(newVersion));
        return newVersion;
    }

    public void disable(UUID disabledBy) {
        if (this.status == RuleStatus.INACTIVE)
            throw new BusinessRuleException(
                    ErrorCode.REVENUE_SHARE_RULE_ALREADY_INACTIVE,
                    "Rule is already inactive"
            );
        this.status = RuleStatus.INACTIVE;
        this.registerEvent(new RevenueShareRuleDisabledEvent(this, disabledBy));
    }

    // ── Validation ───────────────────────────────────────────────

    private static void validate(ShareModel shareModel, BigDecimal sharePercentage) {
        if (shareModel == ShareModel.KM_BASED || shareModel == ShareModel.TRIP_BASED) {
            if (sharePercentage == null)
                throw new BusinessRuleException(
                        ErrorCode.REVENUE_SHARE_RULE_INVALID,
                        "sharePercentage is required for " + shareModel
                );
        }
    }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }
}