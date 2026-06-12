package com.metro.afc.trip.application.port.in;

import com.metro.afc.trip.domain.TripAnomaly;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalySeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface TripAnomalyUseCase {
    Page<TripAnomaly> findAll(AnomalySeverity severity,
                              Boolean isResolved, Pageable pageable);
    TripAnomaly resolve(UUID id, String notes, BigDecimal correctedFare);
}
