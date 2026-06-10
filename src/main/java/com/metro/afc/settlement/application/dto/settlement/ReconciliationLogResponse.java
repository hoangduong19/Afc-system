package com.metro.afc.settlement.application.dto.settlement;

import com.metro.afc.settlement.domain.ReconciliationLog;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReconciliationLogResponse(
        UUID id,
        String category,
        BigDecimal discrepancyAmount,
        Integer tripCount,
        String note,
        Instant loggedAt
) {
    public static ReconciliationLogResponse from(ReconciliationLog log) {
        return new ReconciliationLogResponse(
                log.getId(), log.getCategory(),
                log.getDiscrepancyAmount(), log.getTripCount(),
                log.getNote(), log.getLoggedAt()
        );
    }
}