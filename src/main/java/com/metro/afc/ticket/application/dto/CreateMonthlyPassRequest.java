package com.metro.afc.ticket.application.dto;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateMonthlyPassRequest(
        @NotNull(message = "Mode is required")
        FareMode mode,

        PassengerType passengerType,

        @NotNull(message = "Valid from is required")
        LocalDate validFrom,

        @Min(value = 1, message = "Duration must be >= 1 day")
        int durationDays
) {}