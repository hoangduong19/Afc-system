package com.metro.afc.settlement.application.dto.settlement;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RunSettlementRequest(
        @NotNull @Min(1) @Max(12) Integer month,
        @NotNull @Min(2020)       Integer year
) {}