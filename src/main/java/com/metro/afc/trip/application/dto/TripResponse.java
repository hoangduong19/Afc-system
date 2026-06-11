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
        String tapInDeviceId,
        Instant tapInAt,
        String tapOutStationCode,
        String tapOutDeviceId,
        Instant tapOutAt,
        BigDecimal distanceKm,
        BigDecimal fareAmount,
        String paymentMethod,
        String ticketType,
        String status,
        BigDecimal debtAmount,
        Instant createdAt
) {}