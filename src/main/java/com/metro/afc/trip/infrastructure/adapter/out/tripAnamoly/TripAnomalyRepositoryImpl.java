package com.metro.afc.trip.infrastructure.adapter.out.tripAnamoly;

import com.metro.afc.trip.application.port.out.TripAnomalyRepository;
import com.metro.afc.trip.domain.TripAnomaly;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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
}