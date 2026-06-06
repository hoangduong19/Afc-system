package com.metro.afc.fare.domain.event;

import java.util.UUID;

public record FareRuleUpdatedEvent(
        UUID newFareRuleId,
        String oldSnapshot,
        String newSnapshot,
        String reason,
        UUID updatedBy
) {}