package com.metro.afc.fare.application.dto.fareRule;

import com.metro.afc.fare.domain.model.enums.fareRule.PassDurationType;
import com.metro.afc.ticket.domain.enums.PassScope;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PassPriceEntry(
        @NotNull PassDurationType durationType,
        Integer durationMonths, // null cho DAILY/WEEKLY
        PassScope scope,        // null cho METRO/ANY
        @NotNull @DecimalMin("0.0") BigDecimal amount
) {}