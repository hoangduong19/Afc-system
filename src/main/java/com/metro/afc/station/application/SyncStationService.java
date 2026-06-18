package com.metro.afc.station.application;

import com.metro.afc.station.application.port.in.SyncStationUseCase;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.infrastructure.messaging.StationSyncMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncStationService implements SyncStationUseCase {

    private final StationRepository stationRepository;

    @Override
    @Transactional
    public void sync(StationSyncMessage message) {
        // TO-DO
    }
}