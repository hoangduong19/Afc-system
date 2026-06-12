package com.metro.afc.trip.application.dto.anomaly;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ResolveAnomalyRequest(
        @NotBlank String notes,
        BigDecimal correctedFare
) {}