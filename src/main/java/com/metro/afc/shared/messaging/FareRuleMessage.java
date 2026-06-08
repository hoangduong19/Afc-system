package com.metro.afc.shared.messaging;

import java.time.Instant;
import java.util.UUID;

public record FareRuleMessage(
        UUID fareRuleId,
        String code,
        String changeType,
        String reason,
        UUID changedBy,
        Instant occurredAt
) {}