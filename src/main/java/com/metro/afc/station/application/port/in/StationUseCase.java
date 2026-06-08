package com.metro.afc.station.application.port.in;

import com.metro.afc.station.domain.model.Station;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface StationUseCase {
    Station create(UUID routeId, String code, String name,
                   BigDecimal kmMarker, Integer stationOrder);
    Station update(UUID id, String name, BigDecimal kmMarker);
    void delete(UUID id);
    Station findById(UUID id);
    List<Station> findAll();
    List<Station> findByRouteId(UUID routeId);
}