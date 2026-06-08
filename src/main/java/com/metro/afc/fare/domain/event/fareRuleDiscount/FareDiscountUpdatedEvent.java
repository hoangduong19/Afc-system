package com.metro.afc.fare.domain.event.fareRuleDiscount;

import java.util.UUID;

public record FareDiscountUpdatedEvent(UUID discountId, String oldSnapshot, String newSnapshot, UUID updatedBy) {}
