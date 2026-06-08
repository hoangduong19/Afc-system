package com.metro.afc.card.application.dto.card;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IssueCardRequest(
        @NotNull(message = "Station ID is required")
        UUID stationId
) {}