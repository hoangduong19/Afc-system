package com.metro.afc.station.infrastructure.adapter.out;

import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StationRepositoryImpl implements StationRepository {

    private final StationJpaRepository jpa;

    @Override
    public Optional<Station> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Station> findByRouteId(UUID routeId) {
        return jpa.findByRouteIdOrderByStationOrder(routeId);
    }

    @Override
    public List<Station> findAll() {
        return jpa.findAll();
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsByCode(code);
    }

    @Override
    public boolean existsByRouteIdAndStationOrder(UUID routeId, Integer stationOrder) {
        return jpa.existsByRouteIdAndStationOrder(routeId, stationOrder);
    }

    @Override
    public Optional<Station> findMaxKmMarkerByRouteId(UUID routeId) {
        return jpa.findPreviousStation(routeId, Integer.MAX_VALUE);
    }

    @Override
    public Optional<Station> findPreviousStation(UUID routeId, Integer stationOrder) {
        return jpa.findPreviousStation(routeId, stationOrder);
    }

    @Override
    public Optional<Station> findNextStation(UUID routeId, Integer stationOrder) {
        return jpa.findNextStation(routeId, stationOrder);
    }

    @Override
    public Station save(Station station) {
        return jpa.save(station);
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}