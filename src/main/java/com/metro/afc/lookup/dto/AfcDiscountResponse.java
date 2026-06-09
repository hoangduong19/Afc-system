package com.metro.afc.lookup.dto;

import com.metro.afc.fare.domain.model.FareDiscount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AfcDiscountResponse(
        UUID id,
        String passengerType,
        String discountType,
        BigDecimal discountValue,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
    public static AfcDiscountResponse from(FareDiscount d) {
        return new AfcDiscountResponse(
                d.getId(),
                d.getPassengerType().name(),
                d.getDiscountValue().getDiscountType().name(),
                d.getDiscountValue().getValue(),
                d.getEffectiveFrom(), d.getEffectiveTo()
        );
    }
}