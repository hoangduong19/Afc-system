package com.metro.afc.trip.infrastructure.adapter.out.trip;

import com.metro.afc.trip.domain.Trip;
import com.metro.afc.trip.domain.enums.trip.TripStatus;
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
public interface TripJpaRepository extends JpaRepository<Trip, UUID> {
    boolean existsByExternalTransactionId(UUID externalTransactionId);

    List<Trip> findByOperatorIdAndTapInAtBetween(
            UUID operatorId, Instant from, Instant to);

    @Query("SELECT t FROM Trip t WHERE t.status = 'COMPLETED' " +
            "AND t.tapInAt >= :from AND t.tapInAt < :to")
    List<Trip> findCompletedTripsInPeriod(
            @Param("from") Instant from,
            @Param("to")   Instant to);

    @Query("""
    SELECT t FROM Trip t
    WHERE (:cardId IS NULL OR t.cardId = :cardId)
    AND (:operatorId IS NULL OR t.operatorId = :operatorId)
    AND (:status IS NULL OR t.status = :status)
    AND (:from IS NULL OR t.tapInAt >= :from)
    AND (:to IS NULL OR t.tapInAt <= :to)
    """)
    Page<Trip> findWithFilters(
            @Param("cardId")     UUID cardId,
            @Param("operatorId") UUID operatorId,
            @Param("status") TripStatus status,
            @Param("from")       Instant from,
            @Param("to")         Instant to,
            Pageable pageable);
}