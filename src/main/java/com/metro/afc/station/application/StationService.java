package com.metro.afc.station.application;

import com.metro.afc.route.application.port.out.RouteRepository;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.station.application.port.in.StationUseCase;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StationService implements StationUseCase {

    private final StationRepository stationRepository;
    private final RouteRepository routeRepository;

    @Override
    @Transactional
    public Station create(UUID routeId, String code, String name,
                          BigDecimal kmMarker, Integer stationOrder) {

        if (!routeRepository.existsById(routeId)) {
            throw new NotFoundException(ErrorCode.ROUTE_NOT_FOUND);
        }
        if (stationRepository.existsByCode(code.trim().toUpperCase())) {
            throw new ConflictException(ErrorCode.STATION_ALREADY_EXISTS);
        }
        if (stationRepository.existsByRouteIdAndStationOrder(routeId, stationOrder)) {
            throw new ConflictException(ErrorCode.STATION_ORDER_DUPLICATE);
        }

        Optional<Station> prev = stationRepository.findPreviousStation(routeId, stationOrder);
        Optional<Station> next = stationRepository.findNextStation(routeId, stationOrder);
        Station.validateKmMarkerOrder(kmMarker, prev, next);

        return stationRepository.save(
                Station.create(routeId, code, name, kmMarker, stationOrder)
        );
    }

    @Override
    @Transactional
    public Station update(UUID id, String name, BigDecimal kmMarker) {
        Station station = findOrThrow(id);

        Optional<Station> prev = stationRepository.findPreviousStation(
                station.getRouteId(), station.getStationOrder()
        );
        Optional<Station> next = stationRepository.findNextStation(
                station.getRouteId(), station.getStationOrder()
        );
        Station.validateKmMarkerOrder(kmMarker, prev, next);

        station.update(name, kmMarker);
        return stationRepository.save(station);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        findOrThrow(id);
        stationRepository.deleteById(id);
    }

    @Override
    public Station findById(UUID id) {
        return findOrThrow(id);
    }

    @Override
    public List<Station> findAll() {
        return stationRepository.findAll();
    }

    @Override
    public List<Station> findByRouteId(UUID routeId) {
        return stationRepository.findByRouteId(routeId);
    }

    private Station findOrThrow(UUID id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.STATION_NOT_FOUND));
    }
}