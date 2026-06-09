package com.metro.afc.fare.application.dto.fareRule.calculation;

import java.math.BigDecimal;

public record SingleTripFareResponse(
        String fromStationCode,
        String toStationCode,
        BigDecimal distanceKm,
        BigDecimal baseFare,
        BigDecimal calculatedFare,
        boolean discountApplied,
        String discountType,
        BigDecimal discountValue,
        BigDecimal finalPrice
) {}