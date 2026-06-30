package com.metro.afc.station.application;

import com.metro.afc.route.application.port.out.RouteRepository;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.station.application.port.in.SyncStationUseCase;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import com.metro.afc.station.infrastructure.messaging.StationSyncMessage;
import com.metro.afc.trip.domain.events.StationCatalogChangedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncStationService implements SyncStationUseCase {

    private final StationRepository stationRepository;
    private final RouteRepository routeRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void sync(StationSyncMessage message) {
        var route = routeRepository.findByCode(message.routeCode())
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.ROUTE_NOT_FOUND));

        stationRepository.findByCode(message.stationCode())
                .ifPresentOrElse(
                        existing -> existing.update(message.stationName(), message.distance()),
                        () -> stationRepository.save(Station.create(
                                route.getId(),
                                message.stationCode(),
                                message.stationName(),
                                message.distance(),
                                message.stationOrder()
                        ))
                );

        eventPublisher.publishEvent(new StationCatalogChangedEvent());
    }
}