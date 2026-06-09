package com.metro.afc.ticket.application.dto;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateSingleTripTicketRequest(
        @NotNull(message = "From station is required")
        UUID fromStationId,

        @NotNull(message = "To station is required")
        UUID toStationId,

        @NotNull(message = "Mode is required")
        FareMode mode,

        PassengerType passengerType
) {}