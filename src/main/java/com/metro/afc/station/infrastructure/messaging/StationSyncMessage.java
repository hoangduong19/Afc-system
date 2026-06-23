package com.metro.afc.station.infrastructure.messaging;

import java.math.BigDecimal;

public record StationSyncMessage(
        String stationCode,
        String stationName,
        Integer stationOrder,
        String routeCode,
        String status,
        BigDecimal distance
) {}