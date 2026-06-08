package com.metro.afc.station.application.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateStationRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255)
        String name,

        @NotNull(message = "km_marker is required")
        @DecimalMin(value = "0.0", message = "km_marker must be >= 0")
        BigDecimal kmMarker
) {}