package com.metro.afc.trip.application;

import com.metro.afc.station.domain.model.Station;
import com.metro.afc.station.infrastructure.adapter.out.StationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExternalIdMapper {

    private final StationJpaRepository stationJpaRepository;

    public String toStationCode(Integer externalId) {
        if (externalId == null) return null;
        return stationJpaRepository
                .findByExternalId(externalId)
                .map(Station::getCode)
                .orElse(null);
    }

    public Integer toExternalId(UUID stationUUID) {
        if (stationUUID == null) return null;
        return stationJpaRepository
                .findById(stationUUID)
                .map(Station::getExternalId)
                .orElse(null);
    }
}