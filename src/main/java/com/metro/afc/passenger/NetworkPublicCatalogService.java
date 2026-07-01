package com.metro.afc.passenger;

import com.metro.afc.route.application.dtos.RouteResponse;
import com.metro.afc.route.application.port.out.RouteRepository;
import com.metro.afc.station.application.dtos.StationResponse;
import com.metro.afc.station.application.port.out.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NetworkPublicCatalogService {

    private final StationRepository stationRepository;
    private final RouteRepository routeRepository;

    public List<StationResponse> getAllStations() {
        return stationRepository.findAll().stream()
                .map(StationResponse::from)
                .toList();
    }

    public List<RouteResponse> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(RouteResponse::from)
                .toList();
    }
}