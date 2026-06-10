package com.metro.afc.settlement.application.dto.revenueShareRule;

import com.metro.afc.settlement.domain.RevenueShareRule;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record RevenueShareRuleResponse(
        UUID id,
        UUID operatorId,
        String shareModel,
        Map<String, Object> params,
        BigDecimal sharePercentage,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String status,
        Integer version,
        Instant createdAt
) {
    public static RevenueShareRuleResponse from(RevenueShareRule r) {
        return new RevenueShareRuleResponse(
                r.getId(), r.getOperatorId(),
                r.getShareModel().name(), r.getParams(),
                r.getSharePercentage(),
                r.getEffectiveFrom(), r.getEffectiveTo(),
                r.getStatus().name(), r.getVersion(),
                r.getCreatedAt()
        );
    }
}