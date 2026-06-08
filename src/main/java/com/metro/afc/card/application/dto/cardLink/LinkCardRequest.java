package com.metro.afc.card.application.dto.cardLink;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LinkCardRequest(
        @NotNull(message = "User ID is required")
        UUID userId
) {}