package com.metro.afc.shared.messaging;

import java.time.Instant;
import java.util.UUID;

public record CardStatusMessage(
        UUID cardId,
        String cardUid,
        String fromStatus,
        String toStatus,
        String reason,
        UUID changedBy,
        Instant occurredAt
) {}