package com.metro.afc.trip.domain;

import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalySeverity;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trip_anomalies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripAnomaly {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "trip_id", columnDefinition = "uuid")
    private UUID tripId;

    @Enumerated(EnumType.STRING)
    @Column(name = "anomaly_type", nullable = false, length = 50)
    private AnomalyType anomalyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnomalySeverity severity;

    @Column(nullable = false)
    private String description;

    @Column(name = "detected_at", nullable = false, updatable = false)
    private Instant detectedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved;

    public static TripAnomaly of(UUID tripId, AnomalyType type,
                                 AnomalySeverity severity, String description) {
        TripAnomaly a  = new TripAnomaly();
        a.id           = UUID.randomUUID();
        a.tripId       = tripId;
        a.anomalyType  = type;
        a.severity     = severity;
        a.description  = description;
        a.detectedAt   = Instant.now();
        a.isResolved   = false;
        return a;
    }

    @PrePersist
    protected void onCreate() { detectedAt = Instant.now(); }
}