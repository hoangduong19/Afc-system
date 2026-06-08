package com.metro.afc.card.domain.events;

import com.metro.afc.card.domain.model.enums.CardStatus;

import java.util.UUID;

public record CardStatusChangedEvent(
        UUID cardId,
        CardStatus fromStatus,
        CardStatus toStatus,
        String reason,
        UUID changedBy
) {}