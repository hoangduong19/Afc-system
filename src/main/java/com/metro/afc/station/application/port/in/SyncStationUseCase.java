package com.metro.afc.station.application.port.in;

import com.metro.afc.station.infrastructure.messaging.StationSyncMessage;

public interface SyncStationUseCase {
    void sync(StationSyncMessage message);
}
