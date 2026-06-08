package com.metro.afc.blacklist.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddToBlacklistRequest(
        @NotNull(message = "Card ID is required")
        UUID cardId,

        @NotBlank(message = "Reason is required")
        String reason
) {}