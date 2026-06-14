package com.metro.afc.station.domain.model;

import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "stations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Station {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "route_id", nullable = false, columnDefinition = "uuid")
    private UUID routeId;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "km_marker", nullable = false, precision = 10, scale = 3)
    private BigDecimal kmMarker;

    @Column(name = "station_order", nullable = false)
    private Integer stationOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "external_id")
    private Integer externalId;

    public static Station create(UUID routeId, String code, String name,
                                 BigDecimal kmMarker, Integer stationOrder) {
        Station station    = new Station();
        station.id         = UUID.randomUUID();
        station.routeId    = routeId;
        station.code       = code.trim().toUpperCase();
        station.name       = name.trim();
        station.kmMarker   = kmMarker;
        station.stationOrder = stationOrder;
        return station;
    }

    public void update(String name, BigDecimal kmMarker) {
        this.name     = name.trim();
        this.kmMarker = kmMarker;
    }

    public static void validateKmMarkerOrder(BigDecimal kmMarker,
                                             Optional<Station> prev,
                                             Optional<Station> next) {
        prev.ifPresent(p -> {
            if (kmMarker.compareTo(p.getKmMarker()) <= 0) {
                throw new BusinessRuleException(
                        ErrorCode.STATION_KM_MARKER_INVALID,
                        "km_marker must be greater than previous station: " + p.getKmMarker()
                );
            }
        });

        next.ifPresent(n -> {
            if (kmMarker.compareTo(n.getKmMarker()) >= 0) {
                throw new BusinessRuleException(
                        ErrorCode.STATION_KM_MARKER_INVALID,
                        "km_marker must be less than next station: " + n.getKmMarker()
                );
            }
        });
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}