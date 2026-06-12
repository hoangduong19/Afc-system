package com.metro.afc.settlement.domain.events.settlement;

import java.util.UUID;

public record SettlementConfirmedDomainEvent(UUID settlementId) {}