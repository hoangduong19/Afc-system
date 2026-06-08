package com.metro.afc.fare.application.dto.fareRule;

import jakarta.validation.constraints.NotBlank;

public record DisableFareRuleRequest(
        @NotBlank(message = "Reason cannot be null")
        String reason
) {}
