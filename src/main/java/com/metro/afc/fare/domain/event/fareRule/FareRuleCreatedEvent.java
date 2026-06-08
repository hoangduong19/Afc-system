package com.metro.afc.fare.domain.event.fareRule;

import java.util.UUID;

public record FareRuleCreatedEvent(
        UUID fareRuleId,
        String snapshot,
        UUID createdBy
) {}