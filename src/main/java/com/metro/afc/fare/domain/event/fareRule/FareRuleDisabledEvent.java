package com.metro.afc.fare.domain.event.fareRule;

import java.util.UUID;

public record FareRuleDisabledEvent(
        UUID fareRuleId,
        String oldSnapshot,
        String reason,
        UUID disabledBy
) {}