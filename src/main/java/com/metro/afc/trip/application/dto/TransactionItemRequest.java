package com.metro.afc.trip.application.dto;

import com.metro.afc.trip.domain.enums.PaymentMethod;
import com.metro.afc.trip.domain.enums.TicketTypeUsed;
import com.metro.afc.trip.domain.enums.TripStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionItemRequest(
        @NotNull UUID transactionId,
        @NotBlank String cardUid,
        @NotBlank String operatorCode,
        String lineCode,
        @NotBlank String tapInStationCode,
        @NotNull Instant tapInAt,
        String tapInDeviceId,
        String tapOutStationCode,
        Instant tapOutAt,
        String tapOutDeviceId,
        BigDecimal distanceKm,
        BigDecimal fareAmount,
        @NotNull PaymentMethod paymentMethod,
        TicketTypeUsed ticketType,
        @NotNull TripStatus tripStatus,
        BigDecimal debtAmount
) {}