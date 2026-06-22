package com.metro.afc.passenger.dto;

import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DiscountResponse(
        PassengerType passengerType,
        String discountType,
        BigDecimal discountValue,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {}