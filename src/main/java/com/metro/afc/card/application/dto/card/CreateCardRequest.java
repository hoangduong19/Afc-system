package com.metro.afc.card.application.dto.card;

import com.metro.afc.card.domain.model.enums.CardType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCardRequest(
        @Size(max = 100)
        String cardUid,

        @NotNull(message = "Card type is required")
        CardType type,

        UUID userId,

        @NotNull(message = "supportsMetro is required")
        Boolean supportsMetro,

        @NotNull(message = "supportsBus is required")
        Boolean supportsBus
) {
    public CreateCardRequest {
        if (type == CardType.IDENTIFIED && userId == null) {
            throw new IllegalArgumentException("userId is required for IDENTIFIED card");
        }
        if (type == CardType.ANON && userId != null) {
            throw new IllegalArgumentException("userId must be null for ANON card");
        }
    }
}