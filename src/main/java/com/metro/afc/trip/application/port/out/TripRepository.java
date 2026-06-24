package com.metro.afc.trip.application.port.out;

import com.metro.afc.trip.domain.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository {
    Trip save(Trip trip);
    boolean existsByExternalTransactionId(UUID externalTransactionId);
    List<Trip> findByOperatorIdAndTapInAtBetween(
            UUID operatorId, Instant from, Instant to);
    List<Trip> findByTicketIdIn(List<UUID> ticketIds);
    List<Trip> findCompletedTripsInPeriod(Instant from, Instant to);
    Page<Trip> findWithFilters(UUID cardId, UUID operatorId,
                               Instant from, Instant to, Pageable pageable);
    Optional<Trip> findById(UUID id);
    List<Trip> findInProgressBefore(Instant threshold);
}