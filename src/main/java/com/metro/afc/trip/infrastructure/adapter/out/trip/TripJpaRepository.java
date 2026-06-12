package com.metro.afc.trip.infrastructure.adapter.out.trip;

import com.metro.afc.trip.domain.Trip;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TripJpaRepository
        extends JpaRepository<Trip, UUID>,
        JpaSpecificationExecutor<Trip> {
    boolean existsByExternalTransactionId(UUID externalTransactionId);

    List<Trip> findByOperatorIdAndTapInAtBetween(
            UUID operatorId, Instant from, Instant to);

    @Query("SELECT t FROM Trip t WHERE t.status = 'COMPLETED' " +
            "AND t.tapInAt >= :from AND t.tapInAt < :to")
    List<Trip> findCompletedTripsInPeriod(
            @Param("from") Instant from,
            @Param("to")   Instant to);

    @Query("SELECT t FROM Trip t WHERE t.status = 'IN_PROGRESS' " +
            "AND t.tapInAt < :threshold")
    List<Trip> findInProgressBefore(
            @Param("threshold") Instant threshold);
}