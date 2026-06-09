package com.metro.afc.lookup.dto;

import com.metro.afc.blacklist.domain.Blacklist;

import java.time.Instant;
import java.util.UUID;

public record AfcBlacklistResponse(
        UUID blacklistId,
        UUID cardId,
        String reason,
        Boolean isActive,
        Instant addedAt,
        Instant removedAt
) {
    public static AfcBlacklistResponse from(Blacklist b) {
        return new AfcBlacklistResponse(
                b.getId(), b.getCardId(), b.getReason(),
                b.getIsActive(), b.getAddedAt(), b.getRemovedAt()
        );
    }
}