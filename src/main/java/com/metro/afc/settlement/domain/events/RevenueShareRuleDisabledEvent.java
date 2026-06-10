package com.metro.afc.settlement.domain.events;

import com.metro.afc.settlement.domain.RevenueShareRule;

import java.util.UUID;

public record RevenueShareRuleDisabledEvent(RevenueShareRule rule, UUID disabledBy) {}
