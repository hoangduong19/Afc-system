package com.metro.afc.shared.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record CompanyShareMessage(
        UUID operatorId,
        String operatorCode,
        BigDecimal allocatedAmount,
        BigDecimal totalKm,
        Integer totalTrips,
        BigDecimal kmRatio
) {}