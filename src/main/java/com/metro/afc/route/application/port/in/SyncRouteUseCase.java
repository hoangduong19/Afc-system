package com.metro.afc.route.application.port.in;

import com.metro.afc.route.infrastructure.messaging.RouteSyncMessage;

public interface SyncRouteUseCase {
    void sync(RouteSyncMessage message);
}