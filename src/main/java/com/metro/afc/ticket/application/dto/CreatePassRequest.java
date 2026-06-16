package com.metro.afc.ticket.application.dto;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRule.PassDurationType;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import com.metro.afc.ticket.domain.enums.PassScope;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreatePassRequest(
        @NotNull(message = "Mode is required")
        FareMode mode,

        PassScope scope,

        PassengerType passengerType,

        @NotNull(message = "Valid from is required")
        LocalDate validFrom,

        @NotNull(message = "Duration type is required")
        PassDurationType durationType,

        @Min(value = 1) @Max(value = 12)
        Integer durationMonths
) {
        public CreatePassRequest {
                if (durationType == PassDurationType.MONTHLY && durationMonths == null)
                        throw new IllegalArgumentException("durationMonths is required for MONTHLY pass");
                if (durationType != PassDurationType.MONTHLY && durationMonths != null)
                        throw new IllegalArgumentException("durationMonths only applies to MONTHLY pass");
        }
}