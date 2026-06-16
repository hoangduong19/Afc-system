package com.metro.afc.fare.application.dto.fareRule;

import com.metro.afc.fare.domain.model.FareRule;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record FareRuleResponse(
        UUID id,
        String code,
        String mode,
        BigDecimal baseFare,
        BigDecimal ratePerKm,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<PassPriceEntry> passPrices,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String status,
        Integer version,
        UUID createdBy,
        Instant createdAt
) {
    public static FareRuleResponse from(FareRule r) {
        return new FareRuleResponse(
                r.getId(),
                r.getCode(),
                r.getMode().name(),
                r.getBaseFare().getAmount(),
                r.getRatePerKm().getAmount(),
                r.getMinPrice().getAmount(),
                r.getMaxPrice().getAmount(),
                r.getPassPrices().stream()
                        .map(p -> new PassPriceEntry(
                                p.getDurationType(),
                                p.getDurationMonths(),
                                p.getScope(),
                                p.getPrice().getAmount()))
                        .toList(),
                r.getEffectiveFrom(),
                r.getEffectiveTo(),
                r.getStatus().name(),
                r.getVersion(),
                r.getCreatedBy(),
                r.getCreatedAt()
        );
    }
}