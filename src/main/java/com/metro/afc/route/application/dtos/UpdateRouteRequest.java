package com.metro.afc.route.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRouteRequest(
        @NotBlank(message = "Tên không được để trống")
        @Size(max = 255)
        String name
) {}