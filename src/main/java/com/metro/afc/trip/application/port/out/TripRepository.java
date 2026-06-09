package com.metro.afc.trip.application.port.out;

import com.metro.afc.trip.domain.Trip;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TripRepository {
    Trip save(Trip trip);
    boolean existsByExternalTransactionId(UUID externalTransactionId);
    List<Trip> findByOperatorIdAndTapInAtBetween(
            UUID operatorId, Instant from, Instant to);
}