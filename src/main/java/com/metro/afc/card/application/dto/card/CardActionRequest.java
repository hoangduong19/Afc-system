package com.metro.afc.card.application.dto.card;

import jakarta.validation.constraints.NotBlank;

public record CardActionRequest(
        @NotBlank(message = "Reason is required")
        String reason
) {}