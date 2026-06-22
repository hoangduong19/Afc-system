package com.metro.afc.ticket;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.PassScope;
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
    private final UUID routeId    = UUID.randomUUID();
    private final Money price     = Money.of(new BigDecimal("18625"));

    // ── createSingleTrip ─────────────────────────────────────────

    @Test
    @DisplayName("createSingleTrip sets status ACTIVE")
    void createSingleTrip_setsActiveStatus() {
        Ticket ticket = Ticket.createSingleTrip(
                userId, fromId, toId, FareMode.METRO, price, fareRuleId, null);

        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertEquals(TicketType.SINGLE_TRIP, ticket.getType());
        assertNull(ticket.getCardId());
        assertNull(ticket.getScope());
        assertEquals(userId, ticket.getUserId());
    }

    @Test
    @DisplayName("createSingleTrip validFrom = today, validTo = today + 1")
    void createSingleTrip_setsValidDates() {
        Ticket ticket = Ticket.createSingleTrip(
                userId, fromId, toId, FareMode.METRO, price, fareRuleId, null);

        assertEquals(LocalDate.now(), ticket.getValidFrom());
        assertEquals(LocalDate.now().plusDays(1), ticket.getValidTo());
    }

    // ── createMonthlyPass — factory ──────────────────────────────

    @Test
    @DisplayName("createMonthlyPass METRO sets status ACTIVE and scope null")
    void createMonthlyPass_metro_setsActiveStatusAndNullScope() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.METRO, null, null,
                price, fareRuleId, null, LocalDate.now(), 30);

        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertEquals(TicketType.MONTHLY_PASS, ticket.getType());
        assertNull(ticket.getScope());
        assertNull(ticket.getRouteId());
    }

    @Test
    @DisplayName("createMonthlyPass BUS SINGLE_ROUTE sets scope and routeId")
    void createMonthlyPass_busSingleRoute_setsScope() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.BUS, PassScope.SINGLE_ROUTE, routeId,
                price, fareRuleId, null, LocalDate.now(), 30);

        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertEquals(TicketType.MONTHLY_PASS, ticket.getType());
        assertEquals(PassScope.SINGLE_ROUTE, ticket.getScope());
        assertEquals(routeId, ticket.getRouteId());
    }

    @Test
    @DisplayName("createMonthlyPass BUS MULTI_ROUTE sets scope, routeId null")
    void createMonthlyPass_busMultiRoute_setsScope() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.BUS, PassScope.MULTI_ROUTE, null,
                price, fareRuleId, null, LocalDate.now(), 30);

        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertEquals(TicketType.MONTHLY_PASS, ticket.getType());
        assertEquals(PassScope.MULTI_ROUTE, ticket.getScope());
        assertNull(ticket.getRouteId());
    }

    @Test
    @DisplayName("createMonthlyPass ANY sets scope null")
    void createMonthlyPass_any_setsNullScope() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.ANY, null, null,
                price, fareRuleId, null, LocalDate.now(), 30);

        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertEquals(TicketType.MONTHLY_PASS, ticket.getType());
        assertNull(ticket.getScope());
        assertNull(ticket.getRouteId());
    }

    @Test
    @DisplayName("createMonthlyPass validTo = validFrom + durationDays")
    void createMonthlyPass_setsCorrectValidTo() {
        LocalDate validFrom = LocalDate.of(2026, 7, 1);

        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.METRO, null, null,
                price, fareRuleId, null, validFrom, 30);

        assertEquals(LocalDate.of(2026, 7, 31), ticket.getValidTo());
    }

    // ── createMonthlyPass — scope validation ─────────────────────

    @Test
    @DisplayName("createMonthlyPass BUS without scope throws exception")
    void createMonthlyPass_busWithoutScope_throwsException() {
        assertThrows(BusinessRuleException.class, () ->
                Ticket.createMonthlyPass(
                        userId, FareMode.BUS, null, null,
                        price, fareRuleId, null, LocalDate.now(), 30));
    }

    @Test
    @DisplayName("createMonthlyPass METRO with scope throws exception")
    void createMonthlyPass_metroWithScope_throwsException() {
        assertThrows(BusinessRuleException.class, () ->
                Ticket.createMonthlyPass(
                        userId, FareMode.METRO, PassScope.SINGLE_ROUTE, null,
                        price, fareRuleId, null, LocalDate.now(), 30));
    }

    @Test
    @DisplayName("createMonthlyPass ANY with scope throws exception")
    void createMonthlyPass_anyWithScope_throwsException() {
        assertThrows(BusinessRuleException.class, () ->
                Ticket.createMonthlyPass(
                        userId, FareMode.ANY, PassScope.MULTI_ROUTE, null,
                        price, fareRuleId, null, LocalDate.now(), 30));
    }

    @Test
    @DisplayName("createMonthlyPass BUS SINGLE_ROUTE without routeId throws exception")
    void createMonthlyPass_busSingleRoute_nullRouteId_throwsException() {
        assertThrows(BusinessRuleException.class, () ->
                Ticket.createMonthlyPass(
                        userId, FareMode.BUS, PassScope.SINGLE_ROUTE, null,
                        price, fareRuleId, null, LocalDate.now(), 30));
    }

    @Test
    @DisplayName("createMonthlyPass BUS MULTI_ROUTE with routeId throws exception")
    void createMonthlyPass_busMultiRoute_nonNullRouteId_throwsException() {
        assertThrows(BusinessRuleException.class, () ->
                Ticket.createMonthlyPass(
                        userId, FareMode.BUS, PassScope.MULTI_ROUTE, routeId,
                        price, fareRuleId, null, LocalDate.now(), 30));
    }

    // ── createMonthlyPass — validFrom / durationDays validation ──

    @Test
    @DisplayName("createMonthlyPass validFrom in past throws exception")
    void createMonthlyPass_validFromInPast_throwsException() {
        assertThrows(BusinessRuleException.class, () ->
                Ticket.createMonthlyPass(
                        userId, FareMode.METRO, null, null,
                        price, fareRuleId, null, LocalDate.now().minusDays(1), 30));
    }

    @Test
    @DisplayName("createMonthlyPass durationDays = 0 throws exception")
    void createMonthlyPass_zeroDuration_throwsException() {
        assertThrows(BusinessRuleException.class, () ->
                Ticket.createMonthlyPass(
                        userId, FareMode.METRO, null, null,
                        price, fareRuleId, null, LocalDate.now(), 0));
    }

    @Test
    @DisplayName("createMonthlyPass durationDays > 365 throws exception")
    void createMonthlyPass_tooLongDuration_throwsException() {
        assertThrows(BusinessRuleException.class, () ->
                Ticket.createMonthlyPass(
                        userId, FareMode.METRO, null, null,
                        price, fareRuleId, null, LocalDate.now(), 366));
    }

    // ── markUsed ─────────────────────────────────────────────────

    @Test
    @DisplayName("markUsed transitions ACTIVE to USED")
    void markUsed_activeTicket_becomesUsed() {
        Ticket ticket = Ticket.createSingleTrip(
                userId, fromId, toId, FareMode.METRO, price, fareRuleId, null);

        ticket.markUsed();

        assertEquals(TicketStatus.USED, ticket.getStatus());
        assertNotNull(ticket.getUsedAt());
    }

    @Test
    @DisplayName("markUsed on USED ticket throws exception")
    void markUsed_alreadyUsed_throwsException() {
        Ticket ticket = Ticket.createSingleTrip(
                userId, fromId, toId, FareMode.METRO, price, fareRuleId, null);
        ticket.markUsed();

        assertThrows(BusinessRuleException.class, ticket::markUsed);
    }

    // ── linkToCard ───────────────────────────────────────────────

    @Test
    @DisplayName("linkToCard sets cardId on MONTHLY_PASS")
    void linkToCard_monthlyPass_setsCardId() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.METRO, null, null,
                price, fareRuleId, null, LocalDate.now(), 30);
        UUID cardId = UUID.randomUUID();

        ticket.linkToCard(cardId);

        assertEquals(cardId, ticket.getCardId());
        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
    }

    @Test
    @DisplayName("linkToCard on SINGLE_TRIP throws exception")
    void linkToCard_singleTrip_throwsException() {
        Ticket ticket = Ticket.createSingleTrip(
                userId, fromId, toId, FareMode.METRO, price, fareRuleId, null);

        assertThrows(BusinessRuleException.class,
                () -> ticket.linkToCard(UUID.randomUUID()));
    }

    @Test
    @DisplayName("linkToCard when already linked throws exception")
    void linkToCard_alreadyLinked_throwsException() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.METRO, null, null,
                price, fareRuleId, null, LocalDate.now(), 30);
        ticket.linkToCard(UUID.randomUUID());

        assertThrows(BusinessRuleException.class,
                () -> ticket.linkToCard(UUID.randomUUID()));
    }

    @Test
    @DisplayName("linkToCard on valid ticket succeeds")
    void linkToCard_expiredTicket_throwsException() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.METRO, null, null,
                price, fareRuleId, null, LocalDate.now(), 30);

        assertDoesNotThrow(() -> ticket.linkToCard(UUID.randomUUID()));
    }

    // ── unlinkFromCard ───────────────────────────────────────────

    @Test
    @DisplayName("unlinkFromCard clears cardId")
    void unlinkFromCard_linkedTicket_clearsCardId() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.METRO, null, null,
                price, fareRuleId, null, LocalDate.now(), 30);
        ticket.linkToCard(UUID.randomUUID());

        ticket.unlinkFromCard();

        assertNull(ticket.getCardId());
    }

    @Test
    @DisplayName("unlinkFromCard when not linked throws exception")
    void unlinkFromCard_notLinked_throwsException() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.METRO, null, null,
                price, fareRuleId, null, LocalDate.now(), 30);

        assertThrows(BusinessRuleException.class, ticket::unlinkFromCard);
    }
}