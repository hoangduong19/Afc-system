package com.metro.afc.lookup.dto;

import com.metro.afc.ticket.domain.Ticket;

import java.time.LocalDate;
import java.util.UUID;

public record AfcActiveTicketInfo(
        UUID ticketId,
        String type,
        String mode,
        String scope,
        LocalDate validFrom,
        LocalDate validTo,
        String status
) {
    public static AfcActiveTicketInfo from(Ticket t) {
        return new AfcActiveTicketInfo(
                t.getId(), t.getType().name(), t.getMode().name(),
                t.getScope() != null ? t.getScope().name() : null,
                t.getValidFrom(), t.getValidTo(), t.getStatus().name()
        );
    }
}
