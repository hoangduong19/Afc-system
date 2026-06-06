package com.metro.afc.fare.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateFareRuleRequest(
        @NotNull(message = "Base fare không được để trống")
        @DecimalMin(value = "0.0", message = "Base fare phải >= 0")
        BigDecimal baseFare,

        @NotNull(message = "Rate per km không được để trống")
        @DecimalMin(value = "0.0", message = "Rate per km phải >= 0")
        BigDecimal ratePerKm,

        @NotNull(message = "Min price không được để trống")
        @DecimalMin(value = "0.0", message = "Min price phải >= 0")
        BigDecimal minPrice,

        @NotNull(message = "Max price không được để trống")
        @DecimalMin(value = "0.0", message = "Max price phải >= 0")
        BigDecimal maxPrice,

        @NotNull(message = "Effective from không được để trống")
        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        String reason
) {
    public UpdateFareRuleRequest {
        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Min price không được lớn hơn max price");
        }
        if (effectiveTo != null && effectiveFrom != null
                && effectiveTo.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("Effective to không được trước effective from");
        }
    }
}