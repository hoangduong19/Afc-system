package com.metro.afc.trip.infrastructure.adapter.out.trip;

import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.Trip;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TripRepositoryImpl implements TripRepository {

    private final TripJpaRepository jpa;

    @Override
    public Trip save(Trip trip) { return jpa.save(trip); }

    @Override
    public boolean existsByExternalTransactionId(UUID id) {
        return jpa.existsByExternalTransactionId(id);
    }

    @Override
    public List<Trip> findByOperatorIdAndTapInAtBetween(
            UUID operatorId, Instant from, Instant to) {
        return jpa.findByOperatorIdAndTapInAtBetween(operatorId, from, to);
    }
}