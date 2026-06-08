package com.metro.afc.card.application.dto;

import com.metro.afc.card.domain.model.CardStatusHistory;

import java.time.Instant;
import java.util.UUID;

public record CardStatusHistoryResponse(
        UUID id,
        String fromStatus,
        String toStatus,
        String reason,
        UUID changedBy,
        Instant changedAt
) {
    public static CardStatusHistoryResponse from(CardStatusHistory history) {
        return new CardStatusHistoryResponse(
                history.getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getReason(),
                history.getChangedBy(),
                history.getChangedAt()
        );
    }
}