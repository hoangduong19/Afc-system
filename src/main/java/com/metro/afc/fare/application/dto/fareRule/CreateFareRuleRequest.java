package com.metro.afc.fare.application.dto.fareRule;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.ticket.domain.enums.PassScope;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateFareRuleRequest(
        @NotBlank @Size(max = 50)
        String code,

        @NotNull
        FareMode mode,

        @NotNull @DecimalMin("0.0") BigDecimal baseFare,
        @NotNull @DecimalMin("0.0") BigDecimal ratePerKm,
        @NotNull @DecimalMin("0.0") BigDecimal minPrice,
        @NotNull @DecimalMin("0.0") BigDecimal maxPrice,

        @NotNull @NotEmpty @Valid
        List<PassPriceEntry> passPrices,

        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
    public CreateFareRuleRequest {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)
            throw new IllegalArgumentException("minPrice must not be greater than maxPrice");

        if (effectiveTo != null && effectiveFrom != null && effectiveTo.isBefore(effectiveFrom))
            throw new IllegalArgumentException("effectiveTo must not be before effectiveFrom");

        if (passPrices != null) {
            // Không duplicate key (durationType, durationMonths, scope)
            long distinctKeys = passPrices.stream()
                    .map(p -> p.durationType() + "|" + p.durationMonths() + "|" + p.scope())
                    .distinct().count();
            if (distinctKeys != passPrices.size())
                throw new IllegalArgumentException("Duplicate pass price entry");

            // BUS phải có MULTI_ROUTE; METRO/ANY không được có MULTI_ROUTE
            if (mode == FareMode.BUS) {
                boolean hasMulti = passPrices.stream()
                        .anyMatch(p -> p.scope() == PassScope.MULTI_ROUTE);
                if (!hasMulti)
                    throw new IllegalArgumentException("BUS mode requires MULTI_ROUTE pass price");
            } else {
                boolean hasScope = passPrices.stream()
                        .anyMatch(p -> p.scope() != null);
                if (hasScope)
                    throw new IllegalArgumentException("Only BUS mode can have scope in pass prices");
            }
        }
    }
}