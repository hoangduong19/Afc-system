package com.metro.afc.station.infrastructure.adapter.out;

import com.metro.afc.station.domain.model.Station;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StationJpaRepository extends JpaRepository<Station, UUID> {

    List<Station> findByRouteIdOrderByStationOrder(UUID routeId);

    boolean existsByCode(String code);

    boolean existsByRouteIdAndStationOrder(UUID routeId, Integer stationOrder);

    @Query("SELECT s FROM Station s WHERE s.routeId = :routeId " +
            "AND s.stationOrder < :stationOrder " +
            "ORDER BY s.stationOrder DESC LIMIT 1")
    Optional<Station> findPreviousStation(
            @Param("routeId") UUID routeId,
            @Param("stationOrder") Integer stationOrder
    );

    @Query("SELECT s FROM Station s WHERE s.routeId = :routeId " +
            "AND s.stationOrder > :stationOrder " +
            "ORDER BY s.stationOrder ASC LIMIT 1")
    Optional<Station> findNextStation(
            @Param("routeId") UUID routeId,
            @Param("stationOrder") Integer stationOrder
    );
}