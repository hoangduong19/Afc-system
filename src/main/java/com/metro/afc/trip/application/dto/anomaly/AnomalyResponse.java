package com.metro.afc.trip.application.dto.anomaly;

import com.metro.afc.trip.domain.TripAnomaly;

import java.time.Instant;
import java.util.UUID;

public record AnomalyResponse(
        UUID id,
        UUID tripId,
        String anomalyType,
        String severity,
        String description,
        Boolean isResolved,
        Instant detectedAt,
        Instant resolvedAt
) {
    public static AnomalyResponse from(TripAnomaly a) {
        return new AnomalyResponse(
                a.getId(), a.getTripId(),
                a.getAnomalyType().name(),
                a.getSeverity().name(),
                a.getDescription(),
                a.getIsResolved(),
                a.getDetectedAt(), a.getResolvedAt()
        );
    }
}