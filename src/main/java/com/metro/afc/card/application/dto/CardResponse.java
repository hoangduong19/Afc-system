package com.metro.afc.card.application.dto;

import com.metro.afc.card.domain.model.Card;

import java.time.Instant;
import java.util.UUID;

public record CardResponse(
        UUID id,
        String cardUid,
        String status,
        String type,
        boolean supportsMetro,
        boolean supportsBus,
        UUID issuedAtStationId,
        UUID linkedUserId,
        Instant activatedAt,
        Instant linkedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                card.getCardUid(),
                card.getStatus().name(),
                card.getType().name(),
                card.isSupportsMetro(),
                card.isSupportsBus(),
                card.getIssuedAtStationId(),
                card.getLinkedUserId(),
                card.getActivatedAt(),
                card.getLinkedAt(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}