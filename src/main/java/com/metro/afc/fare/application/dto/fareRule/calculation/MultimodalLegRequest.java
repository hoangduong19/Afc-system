package com.metro.afc.fare.application.dto.fareRule.calculation;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MultimodalLegRequest(
        @NotNull(message = "From station is required")
        UUID fromStationId,

        @NotNull(message = "To station is required")
        UUID toStationId,

        @NotNull(message = "Mode is required")
        FareMode mode
) {}