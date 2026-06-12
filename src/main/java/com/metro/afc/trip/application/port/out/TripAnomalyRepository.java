package com.metro.afc.trip.application.port.out;

import com.metro.afc.trip.domain.TripAnomaly;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalySeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripAnomalyRepository {
    TripAnomaly save(TripAnomaly anomaly);
    List<TripAnomaly> findByTripId(UUID tripId);
    List<TripAnomaly> findAllUnresolved();
    Page<TripAnomaly> findAllWithFilters(AnomalySeverity severity,
                                         Boolean isResolved, Pageable pageable);
    Optional<TripAnomaly> findById(UUID id);
    long countUnresolvedInPeriod(Instant from, Instant to);
}