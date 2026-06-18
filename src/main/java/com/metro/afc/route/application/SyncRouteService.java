package com.metro.afc.route.application;

import com.metro.afc.route.application.port.in.SyncRouteUseCase;
import com.metro.afc.route.application.port.out.RouteRepository;
import com.metro.afc.route.infrastructure.messaging.RouteSyncMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncRouteService implements SyncRouteUseCase {

    private final RouteRepository routeRepository;

    @Override
    @Transactional
    public void sync(RouteSyncMessage message) {
        // TO-DO
    }
}