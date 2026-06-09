package com.metro.afc.lookup.dto;

import com.metro.afc.card.domain.model.Card;
import com.metro.afc.ticket.domain.Ticket;

import java.util.UUID;

public record AfcCardResponse(
        UUID cardId,
        String cardUid,
        String status,
        String type,
        Boolean supportsMetro,
        Boolean supportsBus,
        Boolean isBlacklisted,
        AfcActiveTicketInfo activeTicket
) {
    public static AfcCardResponse from(Card card, boolean isBlacklisted,
                                       Ticket ticket) {
        return new AfcCardResponse(
                card.getId(), card.getCardUid(),
                card.getStatus().name(), card.getType().name(),
                card.getSupportsMetro(), card.getSupportsBus(),
                isBlacklisted,
                ticket != null ? AfcActiveTicketInfo.from(ticket) : null
        );
    }
}

