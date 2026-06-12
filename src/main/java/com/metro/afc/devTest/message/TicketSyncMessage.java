package com.metro.afc.devTest.message;

import com.metro.afc.ticket.domain.Ticket;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TicketSyncMessage(
        UUID id,
        UUID cardId,
        UUID userId,
        String type,
        BigDecimal price,
        UUID fareRuleId,
        UUID discountId,
        UUID fromStationId,
        UUID toStationId,
        String mode,
        LocalDate validFrom,
        LocalDate validTo,
        String status,
        String scope,
        Instant purchasedAt,
        Instant usedAt
) {
    public static TicketSyncMessage from(Ticket ticket) {
        return new TicketSyncMessage(
                ticket.getId(), ticket.getCardId(),
                ticket.getUserId(), ticket.getType().name(),
                ticket.getPrice().getAmount(),
                ticket.getFareRuleId(), ticket.getDiscountId(),
                ticket.getFromStationId(), ticket.getToStationId(),
                ticket.getMode() != null ? ticket.getMode().name() : null,
                ticket.getValidFrom(), ticket.getValidTo(),
                ticket.getStatus().name(),
                ticket.getScope() != null ? ticket.getScope().name() : null,
                ticket.getPurchasedAt(), ticket.getUsedAt()
        );
    }
}