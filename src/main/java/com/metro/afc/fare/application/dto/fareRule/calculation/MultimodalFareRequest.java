package com.metro.afc.fare.application.dto.fareRule.calculation;

import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MultimodalFareRequest(
        @NotNull(message = "Legs are required")
        @Size(min = 2, message = "At least 2 legs required for multimodal")
        List<MultimodalLegRequest> legs,

        PassengerType passengerType
) {}