package com.metro.afc.fare.application.dto.fareRule;

import com.metro.afc.fare.domain.model.FareRule;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FareRuleResponse(
        UUID id,
        String code,
        String mode,
        BigDecimal baseFare,
        BigDecimal ratePerKm,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal monthlySinglePrice,
        BigDecimal monthlyMultiPrice,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String status,
        Integer version,
        UUID createdBy,
        Instant createdAt
) {
    public static FareRuleResponse from(FareRule fareRule) {
        return new FareRuleResponse(
                fareRule.getId(),
                fareRule.getCode(),
                fareRule.getMode().name(),
                fareRule.getBaseFare().getAmount(),
                fareRule.getRatePerKm().getAmount(),
                fareRule.getMinPrice().getAmount(),
                fareRule.getMaxPrice().getAmount(),
                fareRule.getMonthlySinglePrice() != null
                        ? fareRule.getMonthlySinglePrice().getAmount() : null,
                fareRule.getMonthlyMultiPrice() != null
                        ? fareRule.getMonthlyMultiPrice().getAmount() : null,
                fareRule.getEffectiveFrom(),
                fareRule.getEffectiveTo(),
                fareRule.getStatus().name(),
                fareRule.getVersion(),
                fareRule.getCreatedBy(),
                fareRule.getCreatedAt()
        );
    }
}