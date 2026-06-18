package com.metro.afc.station.infrastructure.messaging;

import com.metro.afc.route.application.port.in.SyncRouteUseCase;
import com.metro.afc.route.infrastructure.messaging.RouteSyncMessage;
import com.metro.afc.shared.infrastructure.config.RabbitMQConfig;
import com.metro.afc.station.application.port.in.SyncStationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StationRouteSyncListener {

    private final SyncStationUseCase syncStationUseCase;
    private final SyncRouteUseCase syncRouteUseCase;

    @RabbitListener(queues = RabbitMQConfig.STATION_SYNC_QUEUE)
    public void onStationSynced(StationSyncMessage message) {
        syncStationUseCase.sync(message);
    }

    @RabbitListener(queues = RabbitMQConfig.ROUTE_SYNC_QUEUE)
    public void onRouteSynced(RouteSyncMessage message) {
        syncRouteUseCase.sync(message);
    }
}