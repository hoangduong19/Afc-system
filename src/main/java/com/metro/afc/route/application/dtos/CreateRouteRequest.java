package com.metro.afc.route.application.dtos;

import com.metro.afc.route.domain.model.enums.RouteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateRouteRequest(
        @NotNull(message = "Operator không được để trống")
        UUID operatorId,

        @NotBlank(message = "Code không được để trống")
        @Size(max = 50)
        String code,

        @NotBlank(message = "Tên không được để trống")
        @Size(max = 255)
        String name,

        @NotNull(message = "Loại tuyến không được để trống")
        RouteType type
) {}