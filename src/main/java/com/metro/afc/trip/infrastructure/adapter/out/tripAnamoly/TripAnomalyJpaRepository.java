package com.metro.afc.trip.infrastructure.adapter.out.tripAnamoly;

import com.metro.afc.trip.domain.TripAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripAnomalyJpaRepository
        extends JpaRepository<TripAnomaly, UUID> {
    List<TripAnomaly> findByTripId(UUID tripId);
    List<TripAnomaly> findByIsResolvedFalse();
}