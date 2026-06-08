package com.metro.afc.fare.domain.event.fareRuleDiscount;

import java.util.UUID;

public record FareDiscountCreatedEvent(UUID discountId, String snapshot, UUID createdBy) {}
