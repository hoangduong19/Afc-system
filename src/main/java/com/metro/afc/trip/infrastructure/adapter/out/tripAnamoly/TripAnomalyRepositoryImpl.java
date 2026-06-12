package com.metro.afc.trip.infrastructure.adapter.out.tripAnamoly;

import com.metro.afc.trip.application.port.out.TripAnomalyRepository;
import com.metro.afc.trip.domain.TripAnomaly;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalySeverity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TripAnomalyRepositoryImpl implements TripAnomalyRepository {

    private final TripAnomalyJpaRepository jpa;

    @Override
    public TripAnomaly save(TripAnomaly anomaly) { return jpa.save(anomaly); }

    @Override
    public List<TripAnomaly> findByTripId(UUID tripId) {
        return jpa.findByTripId(tripId);
    }

    @Override
    public List<TripAnomaly> findAllUnresolved() {
        return jpa.findByIsResolvedFalse();
    }

    @Override
    public Optional<TripAnomaly> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Page<TripAnomaly> findAllWithFilters(AnomalySeverity severity,
                                                Boolean isResolved, Pageable pageable) {
        return jpa.findAllWithFilters(severity, isResolved, pageable);
    }

    @Override
    public long countUnresolvedInPeriod(Instant from, Instant to) {
        return jpa.countUnresolvedInPeriod(from, to);
    }
}