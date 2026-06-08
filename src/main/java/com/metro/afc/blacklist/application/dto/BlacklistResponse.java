package com.metro.afc.blacklist.application.dto;

import com.metro.afc.blacklist.domain.Blacklist;

import java.time.Instant;
import java.util.UUID;

public record BlacklistResponse(
        UUID id,
        UUID cardId,
        String reason,
        UUID addedBy,
        Instant addedAt,
        UUID removedBy,
        Instant removedAt,
        Boolean isActive
) {
    public static BlacklistResponse from(Blacklist b) {
        return new BlacklistResponse(
                b.getId(), b.getCardId(), b.getReason(),
                b.getAddedBy(), b.getAddedAt(),
                b.getRemovedBy(), b.getRemovedAt(),
                b.getIsActive()
        );
    }
}