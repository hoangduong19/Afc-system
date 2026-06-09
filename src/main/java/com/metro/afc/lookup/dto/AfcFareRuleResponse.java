package com.metro.afc.lookup.dto;

import com.metro.afc.fare.domain.model.FareRule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AfcFareRuleResponse(
        UUID id,
        String code,
        String mode,
        BigDecimal baseFare,
        BigDecimal ratePerKm,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
    public static AfcFareRuleResponse from(FareRule r) {
        return new AfcFareRuleResponse(
                r.getId(), r.getCode(), r.getMode().name(),
                r.getBaseFare().getAmount(), r.getRatePerKm().getAmount(),
                r.getMinPrice().getAmount(), r.getMaxPrice().getAmount(),
                r.getEffectiveFrom(), r.getEffectiveTo()
        );
    }
}