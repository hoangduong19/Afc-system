package com.metro.afc.route.infrastructure.messaging;

public record RouteSyncMessage(
        String routeCode,
        String routeName,
        String transportType,
        String operatorCode,
        String status
) {}
