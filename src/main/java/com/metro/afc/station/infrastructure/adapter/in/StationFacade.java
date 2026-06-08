package com.metro.afc.station.infrastructure.adapter.in;

import com.metro.afc.station.application.dtos.CreateStationRequest;
import com.metro.afc.station.application.dtos.StationResponse;
import com.metro.afc.station.application.dtos.UpdateStationRequest;
import com.metro.afc.station.application.port.in.StationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StationFacade {

    private final StationUseCase stationUseCase;

    public StationResponse create(CreateStationRequest request) {
        return StationResponse.from(stationUseCase.create(
                request.routeId(), request.code(), request.name(),
                request.kmMarker(), request.stationOrder()
        ));
    }

    public StationResponse update(UUID id, UpdateStationRequest request) {
        return StationResponse.from(
                stationUseCase.update(id, request.name(), request.kmMarker())
        );
    }

    public void delete(UUID id) {
        stationUseCase.delete(id);
    }

    public StationResponse findById(UUID id) {
        return StationResponse.from(stationUseCase.findById(id));
    }

    public List<StationResponse> findAll() {
        return stationUseCase.findAll().stream()
                .map(StationResponse::from).toList();
    }

    public List<StationResponse> findByRouteId(UUID routeId) {
        return stationUseCase.findByRouteId(routeId).stream()
                .map(StationResponse::from).toList();
    }
}