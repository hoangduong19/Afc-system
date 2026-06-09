package com.metro.afc.shared.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TicketMessage(
        UUID ticketId,
        String type,
        String mode,
        UUID cardId,
        UUID userId,
        String fromStationCode,
        String toStationCode,
        BigDecimal fareAmount,
        LocalDate validFrom,
        LocalDate validTo,
        Instant issuedAt
) {}