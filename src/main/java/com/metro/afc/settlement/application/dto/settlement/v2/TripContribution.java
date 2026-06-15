package com.metro.afc.settlement.application.dto.settlement.v2;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;

import java.math.BigDecimal;
import java.util.UUID;

public record TripContribution(
        UUID operatorId,
        FareMode mode,
        int tripCount,
        BigDecimal totalKm
) {}