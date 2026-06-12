package com.metro.afc.trip.infrastructure.adapter.out.tripAnamoly;

import com.metro.afc.trip.domain.TripAnomaly;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalySeverity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TripAnomalyJpaRepository
        extends JpaRepository<TripAnomaly, UUID> {
    List<TripAnomaly> findByTripId(UUID tripId);
    List<TripAnomaly> findByIsResolvedFalse();

    @Query("""
    SELECT a FROM TripAnomaly a
    WHERE (:severity IS NULL OR a.severity = :severity)
    AND (:isResolved IS NULL OR a.isResolved = :isResolved)
    """)
    Page<TripAnomaly> findAllWithFilters(
            @Param("severity") AnomalySeverity severity,
            @Param("isResolved") Boolean isResolved,
            Pageable pageable);

    @Query("""
    SELECT COUNT(a) FROM TripAnomaly a
    JOIN Trip t ON a.tripId = t.id
    WHERE a.isResolved = false
    AND t.tapInAt >= :from
    AND t.tapInAt < :to
    """)
    long countUnresolvedInPeriod(
            @Param("from") Instant from,
            @Param("to")   Instant to);
}