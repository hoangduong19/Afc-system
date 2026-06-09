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
        String status,
        BigDecimal price,
        String fromStationCode,
        String toStationCode,
        LocalDate validFrom,
        LocalDate validTo,
        String qrToken,
        boolean isExpired,
        Instant purchasedAt
) {
    public static PassengerTicketResponse from(Ticket t) {
        return new PassengerTicketResponse(
                t.getId(), t.getType().name(),
                t.getMode().name(), t.getStatus().name(),
                t.getPrice().getAmount(),
                null, null,
                t.getValidFrom(), t.getValidTo(),
                t.getId().toString(),          // qrToken = ticketId UUID
                t.getValidTo().isBefore(LocalDate.now()),
                t.getPurchasedAt()
        );
    }
}