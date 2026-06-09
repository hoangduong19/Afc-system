package com.metro.afc.passenger.dto;

import com.metro.afc.card.domain.model.Card;

import java.time.Instant;
import java.util.UUID;

public record PassengerCardResponse(
        UUID cardId,
        String cardUid,
        String status,
        String type,
        Instant activatedAt,
        Instant linkedAt
) {
    public static PassengerCardResponse from(Card c) {
        return new PassengerCardResponse(
                c.getId(), c.getCardUid(),
                c.getStatus().name(), c.getType().name(),
                c.getActivatedAt(), c.getLinkedAt()
        );
    }
}