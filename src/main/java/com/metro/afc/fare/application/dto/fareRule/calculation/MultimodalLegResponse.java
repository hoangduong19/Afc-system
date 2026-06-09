package com.metro.afc.fare.application.dto.fareRule.calculation;

import java.math.BigDecimal;

public record MultimodalLegResponse(
        String fromStationCode,
        String toStationCode,
        String mode,
        BigDecimal distanceKm,
        BigDecimal fare
) {}
