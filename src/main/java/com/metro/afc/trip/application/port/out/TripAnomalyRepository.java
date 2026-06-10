package com.metro.afc.trip.application.port.out;

import com.metro.afc.trip.domain.TripAnomaly;

import java.util.List;
import java.util.UUID;

public interface TripAnomalyRepository {
    TripAnomaly save(TripAnomaly anomaly);
    List<TripAnomaly> findByTripId(UUID tripId);
    List<TripAnomaly> findAllUnresolved();
}