package com.metro.afc.station.application.dtos;

import com.metro.afc.station.domain.model.Station;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StationResponse(
        UUID id,
        UUID routeId,
        String code,
        String name,
        BigDecimal kmMarker,
        Integer stationOrder,
        Instant createdAt
) {
    public static StationResponse from(Station station) {
        return new StationResponse(
                station.getId(),
                station.getRouteId(),
                station.getCode(),
                station.getName(),
                station.getKmMarker(),
                station.getStationOrder(),
                station.getCreatedAt()
        );
    }
}
