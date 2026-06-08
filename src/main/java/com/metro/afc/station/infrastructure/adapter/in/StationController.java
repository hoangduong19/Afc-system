package com.metro.afc.station.infrastructure.adapter.in;

import com.metro.afc.station.application.dtos.CreateStationRequest;
import com.metro.afc.station.application.dtos.StationResponse;
import com.metro.afc.station.application.dtos.UpdateStationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationFacade stationFacade;

    @PostMapping
    @PreAuthorize("hasAuthority('OPERATOR_CREATE')")
    public ResponseEntity<StationResponse> create(
            @Valid @RequestBody CreateStationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stationFacade.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATOR_UPDATE')")
    public ResponseEntity<StationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStationRequest request) {
        return ResponseEntity.ok(stationFacade.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATOR_UPDATE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        stationFacade.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATOR_READ')")
    public ResponseEntity<StationResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(stationFacade.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('OPERATOR_READ')")
    public ResponseEntity<List<StationResponse>> findAll() {
        return ResponseEntity.ok(stationFacade.findAll());
    }

    @GetMapping("/route/{routeId}")
    @PreAuthorize("hasAuthority('OPERATOR_READ')")
    public ResponseEntity<List<StationResponse>> findByRouteId(
            @PathVariable UUID routeId) {
        return ResponseEntity.ok(stationFacade.findByRouteId(routeId));
    }
}
