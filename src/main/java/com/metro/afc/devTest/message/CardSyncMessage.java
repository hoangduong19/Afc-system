package com.metro.afc.devTest.message;

import com.metro.afc.card.domain.model.Card;

import java.time.Instant;
import java.util.UUID;

public record CardSyncMessage(
        UUID id,
        String cardUid,
        String status,
        String type,
        Boolean supportsMetro,
        Boolean supportsBus,
        UUID issuedAtStationId,
        UUID linkedUserId,
        Instant activatedAt,
        Instant linkedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static CardSyncMessage from(Card card) {
        return new CardSyncMessage(
                card.getId(), card.getCardUid(),
                card.getStatus().name(), card.getType().name(),
                card.getSupportsMetro(), card.getSupportsBus(),
                card.getIssuedAtStationId(), card.getLinkedUserId(),
                card.getActivatedAt(), card.getLinkedAt(),
                card.getCreatedAt(), card.getUpdatedAt()
        );
    }
}