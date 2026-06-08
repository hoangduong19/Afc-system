package com.metro.afc.shared.messaging;

import java.time.Instant;
import java.util.UUID;

public record BlacklistMessage(
        UUID blacklistId,
        UUID cardId,
        String action,       // ADDED / REMOVED
        String reason,
        UUID performedBy,
        Instant occurredAt
) {}