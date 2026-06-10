package com.metro.afc.lookup.dto;

import com.metro.afc.ticket.domain.Ticket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AfcTicketResponse(
        UUID ticketId,
        String type,
        String mode,
        String scope,
        String status,
        UUID cardId,
        UUID userId,
        String fromStationCode,
        String toStationCode,
        BigDecimal price,
        LocalDate validFrom,
        LocalDate validTo,
        boolean isExpired
) {
    public static AfcTicketResponse from(Ticket t) {
        return new AfcTicketResponse(
                t.getId(), t.getType().name(),
                t.getMode().name(),
                t.getScope() != null ? t.getScope().name() : null,
                t.getStatus().name(),
                t.getCardId(), t.getUserId(),
                null, null,
                t.getPrice().getAmount(),
                t.getValidFrom(), t.getValidTo(),
                t.getValidTo().isBefore(LocalDate.now())
        );
    }
}