package com.metro.afc.fare.application.dto.fareRuleDiscount;

import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.DiscountType;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateFareDiscountRequest(
        @NotNull PassengerType passengerType,
        @NotNull DiscountType discountType,
        @NotNull @DecimalMin("0.0") BigDecimal discountValue,
        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo
) {}
