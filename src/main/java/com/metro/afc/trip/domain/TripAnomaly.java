package com.metro.afc.trip.domain;

import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalySeverity;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalyType;
import com.metro.afc.trip.domain.events.FareCorrectedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trip_anomalies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripAnomaly extends AbstractAggregateRoot<TripAnomaly> {

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

    @Column(name = "resolve_notes")
    private String resolveNotes;

    @Column(name = "corrected_fare", precision = 15, scale = 2)
    private BigDecimal correctedFare;

    public static TripAnomaly create(UUID tripId,
                                     AnomalyType anomalyType,
                                     AnomalySeverity severity,
                                     String description) {
        TripAnomaly a  = new TripAnomaly();
        a.id           = UUID.randomUUID();
        a.tripId       = tripId;
        a.anomalyType  = anomalyType;
        a.severity     = severity;
        a.description  = description;
        a.isResolved   = false;
        return a;
    }

    public void resolve(String notes, BigDecimal correctedFare) {
        if (Boolean.TRUE.equals(this.isResolved))
            throw new BusinessRuleException(
                    ErrorCode.ANOMALY_ALREADY_RESOLVED);
        this.isResolved    = true;
        this.resolvedAt    = Instant.now();
        this.resolveNotes  = notes;
        this.correctedFare = correctedFare;

        if (correctedFare != null
                && this.anomalyType == AnomalyType.FARE_MISMATCH) {
            registerEvent(new FareCorrectedEvent(
                    this.tripId, correctedFare));
        }
    }

    @PrePersist
    protected void onCreate() { detectedAt = Instant.now(); }
}