package com.metro.afc.trip.application.dto;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.trip.domain.enums.trip.TicketTypeUsed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionItemRequest(
        @NotNull UUID    transactionId,
        String           cardUid,
        UUID             ticketId,
        @NotBlank String operatorCode,
        String           lineCode,
        @NotBlank String tapInStationCode,
        @NotNull Instant tapInAt,
        String           tapOutStationCode,
        Instant          tapOutAt,
        BigDecimal       distanceKm,
        BigDecimal       fareAmount,
        @NotNull FareMode mode,
        TicketTypeUsed   ticketType
) {}