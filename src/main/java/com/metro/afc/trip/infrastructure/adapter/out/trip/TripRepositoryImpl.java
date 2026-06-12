package com.metro.afc.trip.infrastructure.adapter.out.trip;

import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.Trip;
import com.metro.afc.trip.domain.TripSpecification;
import com.metro.afc.trip.domain.enums.trip.TripStatus;
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

    @Override
    public List<Trip> findCompletedTripsInPeriod(Instant from, Instant to) {
        return jpa.findCompletedTripsInPeriod(from, to);
    }

    @Override
    public Page<Trip> findWithFilters(UUID cardId, UUID operatorId,
                                      TripStatus status, Instant from, Instant to,
                                      Pageable pageable) {
        return jpa.findAll(
                TripSpecification.withFilters(
                        cardId, operatorId, status, from, to),
                pageable
        );
    }

    @Override
    public Optional<Trip> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Trip> findInProgressBefore(Instant threshold) {
        return jpa.findInProgressBefore(threshold);
    }

}