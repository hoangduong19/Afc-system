package com.metro.afc.fare.domain.event.fareRuleDiscount;

import java.util.UUID;

public record FareDiscountDisabledEvent(UUID discountId, String oldSnapshot, UUID disabledBy) {}
