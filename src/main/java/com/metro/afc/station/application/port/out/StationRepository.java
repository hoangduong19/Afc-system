package com.metro.afc.station.application.port.out;

import com.metro.afc.station.domain.model.Station;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StationRepository {
    Optional<Station> findById(UUID id);
    List<Station> findByRouteId(UUID routeId);
    List<Station> findAll();
    boolean existsByCode(String code);
    boolean existsByRouteIdAndStationOrder(UUID routeId, Integer stationOrder);
    Optional<Station> findMaxKmMarkerByRouteId(UUID routeId);
    Optional<Station> findPreviousStation(UUID routeId, Integer stationOrder);
    Optional<Station> findNextStation(UUID routeId, Integer stationOrder);
    Station save(Station station);
    void deleteById(UUID id);
}