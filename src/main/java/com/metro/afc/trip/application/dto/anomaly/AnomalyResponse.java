package com.metro.afc.trip.application.dto.anomaly;

import com.metro.afc.trip.domain.TripAnomaly;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AnomalyResponse(
        UUID id,
        UUID tripId,
        String anomalyType,
        String severity,
        String description,
        Boolean isResolved,
        BigDecimal correctedFare,
        Instant detectedAt,
        Instant resolvedAt,
        String resolveNotes
) {
    public static AnomalyResponse from(TripAnomaly a) {
        return new AnomalyResponse(
                a.getId(), a.getTripId(),
                a.getAnomalyType().name(),
                a.getSeverity().name(),
                a.getDescription(),
                a.getIsResolved(),
                a.getCorrectedFare(),
                a.getDetectedAt(),
                a.getResolvedAt(),
                a.getResolveNotes()
        );
    }
}