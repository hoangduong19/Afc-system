package com.metro.afc.settlement.application.dto.revenueShareRule;

import com.metro.afc.settlement.domain.enums.ShareModel;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record CreateRevenueShareRuleRequest(
        @NotNull(message = "Operator ID is required")
        UUID operatorId,

        @NotNull(message = "Share model is required")
        ShareModel shareModel,

        Map<String, Object> params,

        BigDecimal sharePercentage,

        @NotNull(message = "Effective from is required")
        LocalDate effectiveFrom,

        LocalDate effectiveTo
) {}