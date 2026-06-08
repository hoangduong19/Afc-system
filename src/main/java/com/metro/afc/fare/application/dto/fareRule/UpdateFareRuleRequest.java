package com.metro.afc.fare.application.dto.fareRule;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateFareRuleRequest(
        @NotNull(message = "Base fare is required")
        @DecimalMin(value = "0.0", message = "Base fare must be >= 0")
        BigDecimal baseFare,

        @NotNull(message = "Rate per km is required")
        @DecimalMin(value = "0.0", message = "Rate per km must be >= 0")
        BigDecimal ratePerKm,

        @NotNull(message = "Min price is required")
        @DecimalMin(value = "0.0", message = "Min price must be >= 0")
        BigDecimal minPrice,

        @NotNull(message = "Max price is required")
        @DecimalMin(value = "0.0", message = "Max price must be >= 0")
        BigDecimal maxPrice,

        @NotNull(message = "Effective from is required")
        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        String reason
) {
    public UpdateFareRuleRequest {
        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Min price must not be greater than max price");
        }
        if (effectiveTo != null && effectiveFrom != null
                && effectiveTo.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("Effective to must not be before effective from");
        }
    }
}