package com.metro.afc.passenger.controller;

import com.metro.afc.passenger.NetworkPublicCatalogService;
import com.metro.afc.route.application.dtos.RouteResponse;
import com.metro.afc.station.application.dtos.StationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/passenger")
@RequiredArgsConstructor
public class NetworkCatalogController {

    private final NetworkPublicCatalogService networkPublicCatalogService;

    @GetMapping("/stations")
    public ResponseEntity<List<StationResponse>> getStations() {
        return ResponseEntity.ok(networkPublicCatalogService.getAllStations());
    }

    @GetMapping("/routes")
    public ResponseEntity<List<RouteResponse>> getRoutes() {
        return ResponseEntity.ok(networkPublicCatalogService.getAllRoutes());
    }
}