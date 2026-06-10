package com.metro.afc.settlement.application.dto.settlement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SettlementResponse(
        UUID settlementId,
        String period,
        String status,
        BigDecimal totalExpected,
        BigDecimal totalActual,
        BigDecimal diffAmount,
        String reconciliationStatus,
        BigDecimal toleranceThreshold,
        List<CompanyShareResponse> companyShares,
        UUID ranBy,
        Instant ranAt,
        Instant confirmedAt
) {}