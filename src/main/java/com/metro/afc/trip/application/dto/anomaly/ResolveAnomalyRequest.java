package com.metro.afc.trip.application.dto.anomaly;

import jakarta.validation.constraints.NotBlank;

public record ResolveAnomalyRequest(
        @NotBlank String notes
) {}