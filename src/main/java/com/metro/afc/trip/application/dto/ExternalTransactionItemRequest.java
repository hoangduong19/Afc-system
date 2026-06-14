package com.metro.afc.trip.application.dto;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.trip.domain.enums.trip.PaymentMethod;
import com.metro.afc.trip.domain.enums.trip.TicketTypeUsed;
import com.metro.afc.trip.domain.enums.trip.TripStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExternalTransactionItemRequest(
        @NotNull UUID    transactionId,
        String           cardUid,
        UUID ticketId,
        @NotBlank String operatorCode,
        String           lineCode,
        @NotNull Integer tapInStationId,
        @NotNull Instant tapInAt,
        String           tapInDeviceId,
        Integer          tapOutStationId,
        Instant          tapOutAt,
        String           tapOutDeviceId,
        BigDecimal       distanceKm,
        BigDecimal fareAmount,
        @NotNull FareMode mode,
        @NotNull PaymentMethod paymentMethod,
        TicketTypeUsed ticketType,
        @NotNull TripStatus tripStatus,
        BigDecimal       debtAmount
) {}