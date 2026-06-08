package com.metro.afc.fare.application.dto.fareRuleDiscount;

import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateFareDiscountRequest(
        @NotNull(message = "Discount type is required")
        DiscountType discountType,

        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.0", message = "Discount value must be >= 0")
        BigDecimal discountValue,

        @NotNull(message = "Effective from is required")
        LocalDate effectiveFrom,

        LocalDate effectiveTo
) {}