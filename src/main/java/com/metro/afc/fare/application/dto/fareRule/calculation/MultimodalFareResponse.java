package com.metro.afc.fare.application.dto.fareRule.calculation;

import java.math.BigDecimal;
import java.util.List;

public record MultimodalFareResponse(
        List<MultimodalLegResponse> legs,
        BigDecimal totalFare,
        boolean discountApplied,
        String discountType,
        BigDecimal discountValue,
        BigDecimal finalPrice
) {}