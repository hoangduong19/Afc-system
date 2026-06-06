package com.metro.afc.fare.domain.event;

import java.util.UUID;

public record FareRuleDisabledEvent(
        UUID fareRuleId,
        String oldSnapshot,
        String reason,
        UUID disabledBy
) {}