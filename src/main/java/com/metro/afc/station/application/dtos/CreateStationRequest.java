package com.metro.afc.station.application.dtos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateStationRequest(
        @NotNull(message = "Route ID is required")
        UUID routeId,

        @NotBlank(message = "Code is required")
        @Size(max = 50)
        String code,

        @NotBlank(message = "Name is required")
        @Size(max = 255)
        String name,

        @NotNull(message = "km_marker is required")
        @DecimalMin(value = "0.0", message = "km_marker must be >= 0")
        BigDecimal kmMarker,

        @NotNull(message = "Station order is required")
        @Min(value = 1, message = "Station order must be >= 1")
        Integer stationOrder
) {}