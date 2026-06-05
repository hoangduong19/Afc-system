package com.metro.afc.card.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IssueCardRequest(
        @NotNull(message = "Station ID không được để trống")
        UUID stationId
) {}