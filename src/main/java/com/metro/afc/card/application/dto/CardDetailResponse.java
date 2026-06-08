package com.metro.afc.card.application.dto;

import com.metro.afc.card.domain.model.Card;
import com.metro.afc.card.domain.model.CardStatusHistory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CardDetailResponse(
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
        Instant updatedAt,
        List<CardStatusHistoryResponse> statusHistory
) {
    public static CardDetailResponse from(Card card,
                                          List<CardStatusHistory> history) {
        return new CardDetailResponse(
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
                card.getUpdatedAt(),
                history.stream()
                        .map(CardStatusHistoryResponse::from)
                        .toList()
        );
    }
}