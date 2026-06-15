package com.metro.afc.settlement.application.dto.settlement;

import java.math.BigDecimal;
import java.util.UUID;

public record CompanyShareResponse(
        UUID operatorId,
        String operatorCode,
        BigDecimal totalKm,
        Integer totalTrips,
        BigDecimal expectedRevenue,
        BigDecimal shareAmount,
        BigDecimal directShare,
        BigDecimal proportionalShare,
        BigDecimal roundingAdjustment
) {}