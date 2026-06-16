package com.metro.afc.fare.application.dto.fareRule;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UpdateFareRuleRequest(
        @NotNull @DecimalMin("0.0") BigDecimal baseFare,
        @NotNull @DecimalMin("0.0") BigDecimal ratePerKm,
        @NotNull @DecimalMin("0.0") BigDecimal minPrice,
        @NotNull @DecimalMin("0.0") BigDecimal maxPrice,

        @NotNull @NotEmpty @Valid
        List<PassPriceEntry> passPrices,

        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String reason
) {
    public UpdateFareRuleRequest {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)
            throw new IllegalArgumentException("minPrice must not be greater than maxPrice");

        if (effectiveTo != null && effectiveFrom != null && effectiveTo.isBefore(effectiveFrom))
            throw new IllegalArgumentException("effectiveTo must not be before effectiveFrom");

        if (passPrices != null) {
            long distinctKeys = passPrices.stream()
                    .map(p -> p.durationType() + "|" + p.durationMonths() + "|" + p.scope())
                    .distinct().count();
            if (distinctKeys != passPrices.size())
                throw new IllegalArgumentException("Duplicate pass price entry");
        }
    }
}