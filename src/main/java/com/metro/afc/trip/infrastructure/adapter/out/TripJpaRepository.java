package com.metro.afc.trip.infrastructure.adapter.out;

import com.metro.afc.trip.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TripJpaRepository extends JpaRepository<Trip, UUID> {
    boolean existsByExternalTransactionId(UUID externalTransactionId);

    List<Trip> findByOperatorIdAndTapInAtBetween(
            UUID operatorId, Instant from, Instant to);
}