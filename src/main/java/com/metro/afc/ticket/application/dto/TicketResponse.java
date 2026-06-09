package com.metro.afc.ticket.application.dto;

import com.metro.afc.ticket.domain.Ticket;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TicketResponse(
        UUID ticketId,
        String type,
        String mode,
        String status,
        UUID cardId,
        UUID userId,
        String fromStationCode,
        String toStationCode,
        BigDecimal price,
        UUID fareRuleId,
        UUID discountId,
        LocalDate validFrom,
        LocalDate validTo,
        Instant purchasedAt
) {
    public static TicketResponse from(Ticket t,
                                      String fromCode,
                                      String toCode) {
        return new TicketResponse(
                t.getId(), t.getType().name(),
                t.getMode().name(), t.getStatus().name(),
                t.getCardId(), t.getUserId(),
                fromCode, toCode,
                t.getPrice().getAmount(),
                t.getFareRuleId(), t.getDiscountId(),
                t.getValidFrom(), t.getValidTo(),
                t.getPurchasedAt()
        );
    }
}