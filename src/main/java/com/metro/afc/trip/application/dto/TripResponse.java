package com.metro.afc.trip.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TripResponse(
        UUID id,
        UUID externalTransactionId,
        UUID cardId,
        String cardUid,
        UUID operatorId,
        String operatorCode,
        String tapInStationCode,
        Instant tapInAt,
        String tapOutStationCode,
        Instant tapOutAt,
        BigDecimal distanceKm,
        BigDecimal fareAmount,
        String ticketType,
        Instant createdAt
) {}