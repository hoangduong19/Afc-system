package com.metro.afc.card.application.dto;

import com.metro.afc.card.domain.model.Card;

import java.time.Instant;
import java.util.UUID;

public record CardResponse(
        UUID id,
        String cardUid,
        String status,
        String type,
        Boolean supportsMetro,
        Boolean supportsBus,
        UUID issuedAtStationId,
        UUID linkedUserId,
        Instant activatedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                card.getCardUid(),
                card.getStatus().name(),
                card.getType().name(),
                card.getSupportsMetro(),
                card.getSupportsBus(),
                card.getIssuedAtStationId(),
                card.getLinkedUserId(),
                card.getActivatedAt(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}