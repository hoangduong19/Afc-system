package com.metro.afc.ticket;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.TicketStatus;
import com.metro.afc.ticket.domain.enums.TicketType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Ticket Domain")
class TicketTest {

    private final UUID userId     = UUID.randomUUID();
    private final UUID fromId     = UUID.randomUUID();
    private final UUID toId       = UUID.randomUUID();
    private final UUID fareRuleId = UUID.randomUUID();
    private final Money price     = Money.of(new BigDecimal("18625"));

    // ── createSingleTrip ─────────────────────────────────────────

    @Test
    @DisplayName("createSingleTrip sets status ACTIVE")
    void createSingleTrip_setsActiveStatus() {
        Ticket ticket = Ticket.createSingleTrip(
                userId, fromId, toId, FareMode.METRO,
                price, fareRuleId, null
        );

        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertEquals(TicketType.SINGLE_TRIP, ticket.getType());
        assertNull(ticket.getCardId());
        assertEquals(userId, ticket.getUserId());
    }

    @Test
    @DisplayName("createSingleTrip validFrom = today, validTo = today + 1")
    void createSingleTrip_setsValidDates() {
        Ticket ticket = Ticket.createSingleTrip(
                userId, fromId, toId, FareMode.METRO,
                price, fareRuleId, null
        );

        assertEquals(LocalDate.now(), ticket.getValidFrom());
        assertEquals(LocalDate.now().plusDays(1), ticket.getValidTo());
    }

    // ── createMonthlyPass ────────────────────────────────────────

    @Test
    @DisplayName("createMonthlyPass sets status ACTIVE")
    void createMonthlyPass_setsActiveStatus() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.METRO, price,
                fareRuleId, null,
                LocalDate.now(), 30
        );

        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertEquals(TicketType.MONTHLY_PASS, ticket.getType());
    }

    @Test
    @DisplayName("createMonthlyPass validTo = validFrom + durationDays")
    void createMonthlyPass_setsCorrectValidTo() {
        LocalDate validFrom = LocalDate.of(2026, 7, 1);
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.METRO, price,
                fareRuleId, null, validFrom, 30
        );

        assertEquals(LocalDate.of(2026, 7, 31), ticket.getValidTo());
    }

    // ── markUsed ─────────────────────────────────────────────────

    @Test
    @DisplayName("markUsed transitions ACTIVE to USED")
    void markUsed_activTicket_becomesUsed() {
        Ticket ticket = Ticket.createSingleTrip(
                userId, fromId, toId, FareMode.METRO,
                price, fareRuleId, null
        );

        ticket.markUsed();

        assertEquals(TicketStatus.USED, ticket.getStatus());
        assertNotNull(ticket.getUsedAt());
    }

    @Test
    @DisplayName("markUsed on USED ticket throws exception")
    void markUsed_alreadyUsed_throwsException() {
        Ticket ticket = Ticket.createSingleTrip(
                userId, fromId, toId, FareMode.METRO,
                price, fareRuleId, null
        );
        ticket.markUsed();

        assertThrows(BusinessRuleException.class, ticket::markUsed);
    }

    // ── linkToCard ───────────────────────────────────────────────

    @Test
    @DisplayName("linkToCard sets cardId on MONTHLY_PASS")
    void linkToCard_monthlyPass_setsCardId() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.METRO, price,
                fareRuleId, null, LocalDate.now(), 30
        );
        UUID cardId = UUID.randomUUID();

        ticket.linkToCard(cardId);

        assertEquals(cardId, ticket.getCardId());
        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
    }

    @Test
    @DisplayName("linkToCard on SINGLE_TRIP throws exception")
    void linkToCard_singleTrip_throwsException() {
        Ticket ticket = Ticket.createSingleTrip(
                userId, fromId, toId, FareMode.METRO,
                price, fareRuleId, null
        );

        assertThrows(BusinessRuleException.class,
                () -> ticket.linkToCard(UUID.randomUUID()));
    }

    @Test
    @DisplayName("linkToCard when already linked throws exception")
    void linkToCard_alreadyLinked_throwsException() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.METRO, price,
                fareRuleId, null, LocalDate.now(), 30
        );
        ticket.linkToCard(UUID.randomUUID());

        assertThrows(BusinessRuleException.class,
                () -> ticket.linkToCard(UUID.randomUUID()));
    }

    // ── unlinkFromCard ───────────────────────────────────────────

    @Test
    @DisplayName("unlinkFromCard clears cardId")
    void unlinkFromCard_linkedTicket_clearsCardId() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.METRO, price,
                fareRuleId, null, LocalDate.now(), 30
        );
        ticket.linkToCard(UUID.randomUUID());

        ticket.unlinkFromCard();

        assertNull(ticket.getCardId());
    }

    @Test
    @DisplayName("unlinkFromCard when not linked throws exception")
    void unlinkFromCard_notLinked_throwsException() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.METRO, price,
                fareRuleId, null, LocalDate.now(), 30
        );

        assertThrows(BusinessRuleException.class, ticket::unlinkFromCard);
    }
}