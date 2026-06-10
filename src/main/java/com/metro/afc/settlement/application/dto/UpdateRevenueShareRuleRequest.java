package com.metro.afc.settlement.application.dto;

import com.metro.afc.settlement.domain.enums.ShareModel;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record UpdateRevenueShareRuleRequest(
        @NotNull ShareModel shareModel,
        Map<String, Object> params,
        BigDecimal sharePercentage,

        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo
) {}