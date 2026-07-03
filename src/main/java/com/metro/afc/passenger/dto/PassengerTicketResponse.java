package com.metro.afc.passenger.dto;

import com.metro.afc.ticket.domain.Ticket;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PassengerTicketResponse(
        UUID ticketId,
        String type,
        String mode,
        String scope,
        String status,
        BigDecimal price,
        String fromStationCode,
        String toStationCode,
        LocalDate validFrom,
        LocalDate validTo,
        boolean isExpired,
        Instant purchasedAt
) {
    public static PassengerTicketResponse from(
            Ticket t,
            String fromStationCode,
            String toStationCode
    ) {
        return new PassengerTicketResponse(
                t.getId(),
                t.getType().name(),
                t.getMode().name(),
                t.getScope() != null ? t.getScope().name() : null,
                t.getStatus().name(),
                t.getPrice().getAmount(),
                fromStationCode,
                toStationCode,
                t.getValidFrom(),
                t.getValidTo(),
                t.getValidTo().isBefore(LocalDate.now()),
                t.getPurchasedAt()
        );
    }
}