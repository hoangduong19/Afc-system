package com.metro.afc.fare.application.dto.fareRule;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateFareRuleRequest(
        @NotBlank(message = "Code is required")
        @Size(max = 50, message = "Code must be at most 50 characters")
        String code,

        @NotNull(message = "Mode is required")
        FareMode mode,

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

        LocalDate effectiveTo
) {
    public CreateFareRuleRequest {
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