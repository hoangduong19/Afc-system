package com.metro.afc.devTest.message;

import com.metro.afc.blacklist.domain.Blacklist;
import java.time.Instant;
import java.util.UUID;

public record BlacklistSyncMessage(
        UUID id,
        UUID cardId,
        String reason,
        UUID addedBy,
        Instant addedAt,
        boolean isActive
) {
    public static BlacklistSyncMessage from(Blacklist b) {
        return new BlacklistSyncMessage(
                b.getId(),
                b.getCardId(),
                b.getReason(),
                b.getAddedBy(),
                b.getAddedAt(),
                b.getIsActive()
        );
    }
}