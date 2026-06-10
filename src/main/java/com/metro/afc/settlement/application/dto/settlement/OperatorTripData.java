package com.metro.afc.settlement.application.dto.settlement;

import java.math.BigDecimal;
import java.util.UUID;

public record OperatorTripData(
        UUID operatorId,
        BigDecimal totalKm,
        int tripCount
) {}