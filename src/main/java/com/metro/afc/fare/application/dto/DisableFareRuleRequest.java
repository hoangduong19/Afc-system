package com.metro.afc.fare.application.dto;

import jakarta.validation.constraints.NotBlank;

public record DisableFareRuleRequest(
        @NotBlank(message = "Lý do không được để trống")
        String reason
) {}
