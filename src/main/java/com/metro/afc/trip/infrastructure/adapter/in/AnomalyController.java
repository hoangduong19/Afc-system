package com.metro.afc.trip.infrastructure.adapter.in;

import com.metro.afc.trip.application.dto.anomaly.AnomalyResponse;
import com.metro.afc.trip.application.dto.anomaly.ResolveAnomalyRequest;
import com.metro.afc.trip.application.port.in.TripAnomalyUseCase;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalySeverity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyController {

    private final TripAnomalyUseCase anomalyUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIP_READ')")
    public ResponseEntity<Page<AnomalyResponse>> findAll(
            @RequestParam(required = false) AnomalySeverity severity,
            @RequestParam(required = false) Boolean isResolved,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("detectedAt").descending());
        return ResponseEntity.ok(
                anomalyUseCase.findAll(severity, isResolved, pageable)
                        .map(AnomalyResponse::from));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('TRIP_READ')")
    public ResponseEntity<AnomalyResponse> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveAnomalyRequest request) {
        return ResponseEntity.ok(
                AnomalyResponse.from(
                        anomalyUseCase.resolve(id, request.notes())));
    }
}