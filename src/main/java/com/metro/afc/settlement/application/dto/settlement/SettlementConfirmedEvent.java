package com.metro.afc.settlement.application.dto.settlement;

import com.metro.afc.shared.messaging.CompanyShareMessage;

import java.util.List;
import java.util.UUID;

public record SettlementConfirmedEvent(
        UUID settlementId,
        String period,
        List<CompanyShareMessage> shares
) {}