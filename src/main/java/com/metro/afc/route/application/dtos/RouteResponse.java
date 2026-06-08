package com.metro.afc.route.application.dtos;

import com.metro.afc.route.domain.model.Route;

import java.time.Instant;
import java.util.UUID;

public record RouteResponse(
        UUID id,
        UUID operatorId,
        String code,
        String name,
        String type,
        Instant createdAt
) {
    public static RouteResponse from(Route route) {
        return new RouteResponse(
                route.getId(),
                route.getOperatorId(),
                route.getCode(),
                route.getName(),
                route.getType().name(),
                route.getCreatedAt()
        );
    }
}