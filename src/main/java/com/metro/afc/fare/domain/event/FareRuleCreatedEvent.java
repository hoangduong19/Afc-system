package com.metro.afc.fare.domain.event;

import java.util.UUID;

public record FareRuleCreatedEvent(
        UUID fareRuleId,
        String snapshot,
        UUID createdBy
) {}