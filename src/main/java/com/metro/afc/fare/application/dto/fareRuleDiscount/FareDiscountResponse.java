package com.metro.afc.fare.application.dto.fareRuleDiscount;

import com.metro.afc.fare.domain.model.FareDiscount;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FareDiscountResponse(
        UUID id,
        String passengerType,
        String discountType,
        BigDecimal discountValue,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String status,
        Integer version,
        Instant createdAt
) {
    public static FareDiscountResponse from(FareDiscount d) {
        return new FareDiscountResponse(
                d.getId(),
                d.getPassengerType().name(),
                d.getDiscountValue().getDiscountType().name(),
                d.getDiscountValue().getValue(),
                d.getEffectiveFrom(),
                d.getEffectiveTo(),
                d.getStatus().name(),
                d.getVersion(),
                d.getCreatedAt()
        );
    }
}