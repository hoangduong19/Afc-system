package com.metro.afc.card.application.dto.card;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCardRequest(
        @Size(max = 100)
        String cardUid,

        UUID userId,

        @NotNull(message = "supportsMetro is required")
        Boolean supportsMetro,

        @NotNull(message = "supportsBus is required")
        Boolean supportsBus
) {
}