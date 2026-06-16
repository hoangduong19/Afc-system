package com.metro.afc.ticket;

import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.card.domain.model.enums.CardStatus;
import com.metro.afc.fare.application.port.out.FareDiscountRepository;
import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.FareDiscount;
import com.metro.afc.fare.domain.model.FarePassPrice;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRule.PassDurationType;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.DiscountType;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import com.metro.afc.ticket.application.TicketService;
import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.PassScope;
import com.metro.afc.ticket.domain.enums.TicketStatus;
import com.metro.afc.ticket.domain.enums.TicketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pass price fixtures:
 *
 * METRO  DAILY=40_000  WEEKLY=160_000  MONTHLY 1=200_000  MONTHLY 3=590_000
 * BUS    DAILY=30_000  WEEKLY=120_000
 *        MONTHLY 1 SINGLE=140_000   MONTHLY 1 MULTI=280_000
 *        MONTHLY 2 SINGLE=270_000   MONTHLY 2 MULTI=550_000
 * ANY    DAILY=50_000  WEEKLY=200_000  MONTHLY 1=500_000
 *
 * STUDENT 50% discount halves all prices.
 *
 * Single-trip (METRO, 12.5 km): 8000 + 12.5 × 850 = 18625
 * Single-trip (BUS,   12.5 km): 3000 + 12.5 × 450 = 8625
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TicketService")
class TicketServiceTest {

    // ── Mocks ─────────────────────────────────────────────────────

    @Mock private TicketRepository       ticketRepository;
    @Mock private CardRepository         cardRepository;
    @Mock private StationRepository      stationRepository;
    @Mock private FareRuleRepository     fareRuleRepository;
    @Mock private FareDiscountRepository fareDiscountRepository;

    @InjectMocks
    private TicketService ticketService;

    // ── Shared fixtures ───────────────────────────────────────────

    private static final UUID      ACTOR      = UUID.randomUUID();
    private static final LocalDate VALID_FROM = LocalDate.of(2025, 7, 1);
    private static final LocalDate TODAY      = LocalDate.now();

    private UUID    userId;
    private UUID    cardId;
    private UUID    fromId;
    private UUID    toId;
    private Card    activeCard;
    private Station fromStation;
    private Station toStation;

    private FareRule metroFareRule;
    private FareRule busFareRule;
    private FareRule anyFareRule;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cardId = UUID.randomUUID();
        fromId = UUID.randomUUID();
        toId   = UUID.randomUUID();

        // ── Card ──────────────────────────────────────────────────
        activeCard = mock(Card.class);
        when(activeCard.getId()).thenReturn(cardId);
        when(activeCard.getStatus()).thenReturn(CardStatus.ACTIVE);

        // ── Stations ──────────────────────────────────────────────
        fromStation = mock(Station.class);
        when(fromStation.getId()).thenReturn(fromId);
        when(fromStation.getCode()).thenReturn("HN_2A_01");
        when(fromStation.getKmMarker()).thenReturn(new BigDecimal("0.000"));

        toStation = mock(Station.class);
        when(toStation.getId()).thenReturn(toId);
        when(toStation.getCode()).thenReturn("HN_2A_12");
        when(toStation.getKmMarker()).thenReturn(new BigDecimal("12.500"));

        // ── Fare rules ────────────────────────────────────────────
        metroFareRule = FareRule.create(
                "HN_METRO_STANDARD", FareMode.METRO,
                new BigDecimal("8000"), new BigDecimal("850"),
                new BigDecimal("8000"), new BigDecimal("30000"),
                List.of(
                        FarePassPrice.of(PassDurationType.DAILY,   null, null, new BigDecimal("40000")),
                        FarePassPrice.of(PassDurationType.WEEKLY,  null, null, new BigDecimal("160000")),
                        FarePassPrice.of(PassDurationType.MONTHLY, 1,    null, new BigDecimal("200000")),
                        FarePassPrice.of(PassDurationType.MONTHLY, 3,    null, new BigDecimal("590000"))
                ),
                VALID_FROM, null, ACTOR
        );

        busFareRule = FareRule.create(
                "HN_BUS_STANDARD", FareMode.BUS,
                new BigDecimal("3000"), new BigDecimal("450"),
                new BigDecimal("3000"), new BigDecimal("30000"),
                List.of(
                        FarePassPrice.of(PassDurationType.DAILY,   null, null,                   new BigDecimal("30000")),
                        FarePassPrice.of(PassDurationType.WEEKLY,  null, null,                   new BigDecimal("120000")),
                        FarePassPrice.of(PassDurationType.MONTHLY, 1,    PassScope.SINGLE_ROUTE, new BigDecimal("140000")),
                        FarePassPrice.of(PassDurationType.MONTHLY, 1,    PassScope.MULTI_ROUTE,  new BigDecimal("280000")),
                        FarePassPrice.of(PassDurationType.MONTHLY, 2,    PassScope.SINGLE_ROUTE, new BigDecimal("270000")),
                        FarePassPrice.of(PassDurationType.MONTHLY, 2,    PassScope.MULTI_ROUTE,  new BigDecimal("550000"))
                ),
                VALID_FROM, null, ACTOR
        );

        anyFareRule = FareRule.create(
                "HN_ANY_STANDARD", FareMode.ANY,
                new BigDecimal("8000"), new BigDecimal("850"),
                new BigDecimal("8000"), new BigDecimal("30000"),
                List.of(
                        FarePassPrice.of(PassDurationType.DAILY,   null, null, new BigDecimal("50000")),
                        FarePassPrice.of(PassDurationType.WEEKLY,  null, null, new BigDecimal("200000")),
                        FarePassPrice.of(PassDurationType.MONTHLY, 1,    null, new BigDecimal("500000"))
                ),
                VALID_FROM, null, ACTOR
        );

        // ── Default stubs ─────────────────────────────────────────
        when(stationRepository.findById(fromId)).thenReturn(Optional.of(fromStation));
        when(stationRepository.findById(toId)).thenReturn(Optional.of(toStation));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(activeCard));
        when(fareRuleRepository.findActiveByMode(FareMode.METRO)).thenReturn(Optional.of(metroFareRule));
        when(fareRuleRepository.findActiveByMode(FareMode.BUS)).thenReturn(Optional.of(busFareRule));
        when(fareRuleRepository.findActiveByMode(FareMode.ANY)).thenReturn(Optional.of(anyFareRule));
        when(fareDiscountRepository.findActiveByPassengerType(any())).thenReturn(Optional.empty());
        when(ticketRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void stubStudentDiscount() {
        FareDiscount discount = FareDiscount.create(
                PassengerType.STUDENT, DiscountType.PERCENT,
                new BigDecimal("50"), VALID_FROM, null, ACTOR
        );
        when(fareDiscountRepository.findActiveByPassengerType(PassengerType.STUDENT))
                .thenReturn(Optional.of(discount));
    }

    private void assertMoney(String expected, Money actual) {
        assertThat(actual.getAmount().stripTrailingZeros())
                .isEqualByComparingTo(new BigDecimal(expected).stripTrailingZeros());
    }

    private Ticket buildLinkedMonthlyPass() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId, FareMode.METRO, null,
                Money.of(new BigDecimal("200000")),
                metroFareRule.getId(), null,
                TODAY, 30
        );
        ticket.linkToCard(cardId);
        return ticket;
    }

    // ── createSingleTrip ──────────────────────────────────────────

    @Nested
    @DisplayName("createSingleTrip")
    class CreateSingleTrip {

        @Test
        @DisplayName("METRO 12.5 km, no discount → 18625, SINGLE_TRIP, ACTIVE")
        void noDiscount_correctPriceAndStatus() {
            Ticket ticket = ticketService.createSingleTrip(
                    userId, fromId, toId, FareMode.METRO, null
            );

            assertThat(ticket.getType()).isEqualTo(TicketType.SINGLE_TRIP);
            assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ACTIVE);
            assertMoney("18625", ticket.getPrice());
        }

        @Test
        @DisplayName("METRO 12.5 km, STUDENT 50% → 9312.50")
        void studentDiscount_halfPrice() {
            stubStudentDiscount();

            Ticket ticket = ticketService.createSingleTrip(
                    userId, fromId, toId, FareMode.METRO, PassengerType.STUDENT
            );

            assertMoney("9312.50", ticket.getPrice());
        }

        @Test
        @DisplayName("METRO 12.5 km, SENIOR 100% → 0")
        void seniorDiscount_free() {
            FareDiscount discount = FareDiscount.create(
                    PassengerType.SENIOR, DiscountType.PERCENT,
                    new BigDecimal("100"), VALID_FROM, null, ACTOR
            );
            when(fareDiscountRepository.findActiveByPassengerType(PassengerType.SENIOR))
                    .thenReturn(Optional.of(discount));

            Ticket ticket = ticketService.createSingleTrip(
                    userId, fromId, toId, FareMode.METRO, PassengerType.SENIOR
            );

            assertMoney("0", ticket.getPrice());
        }

        @Test
        @DisplayName("passengerType có nhưng không có discount active → full price")
        void noActiveDiscount_fullPrice() {
            Ticket ticket = ticketService.createSingleTrip(
                    userId, fromId, toId, FareMode.METRO, PassengerType.STUDENT
            );

            assertMoney("18625", ticket.getPrice());
        }

        @Test
        @DisplayName("BUS 12.5 km, no discount → 8625")
        void bus_noDiscount_correctPrice() {
            Ticket ticket = ticketService.createSingleTrip(
                    userId, fromId, toId, FareMode.BUS, null
            );

            assertMoney("8625", ticket.getPrice());
        }

        @Test
        @DisplayName("from station not found → NotFoundException")
        void fromStationNotFound_throws() {
            when(stationRepository.findById(fromId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    ticketService.createSingleTrip(userId, fromId, toId, FareMode.METRO, null))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("to station not found → NotFoundException")
        void toStationNotFound_throws() {
            when(stationRepository.findById(toId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    ticketService.createSingleTrip(userId, fromId, toId, FareMode.METRO, null))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("same station → BusinessRuleException")
        void sameStation_throws() {
            assertThatThrownBy(() ->
                    ticketService.createSingleTrip(userId, fromId, fromId, FareMode.METRO, null))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("no active fare rule → NotFoundException")
        void noFareRule_throws() {
            when(fareRuleRepository.findActiveByMode(FareMode.METRO)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    ticketService.createSingleTrip(userId, fromId, toId, FareMode.METRO, null))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // ── createPass ────────────────────────────────────────────────

    @Nested
    @DisplayName("createPass")
    class CreatePass {

        @Test
        @DisplayName("METRO MONTHLY 1 tháng, no discount → 200000, MONTHLY_PASS, ACTIVE, scope null")
        void metro_monthly1_noDiscount() {
            Ticket ticket = ticketService.createPass(
                    userId, FareMode.METRO, null, null,
                    TODAY, PassDurationType.MONTHLY, 1
            );

            assertThat(ticket.getType()).isEqualTo(TicketType.MONTHLY_PASS);
            assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ACTIVE);
            assertThat(ticket.getScope()).isNull();
            assertMoney("200000", ticket.getPrice());
        }

        @Test
        @DisplayName("METRO MONTHLY 3 tháng, no discount → 590000")
        void metro_monthly3_noDiscount() {
            Ticket ticket = ticketService.createPass(
                    userId, FareMode.METRO, null, null,
                    TODAY, PassDurationType.MONTHLY, 3
            );

            assertMoney("590000", ticket.getPrice());
        }

        @Test
        @DisplayName("METRO DAILY, no discount → 40000")
        void metro_daily_noDiscount() {
            Ticket ticket = ticketService.createPass(
                    userId, FareMode.METRO, null, null,
                    TODAY, PassDurationType.DAILY, null
            );

            assertMoney("40000", ticket.getPrice());
        }

        @Test
        @DisplayName("METRO WEEKLY, no discount → 160000")
        void metro_weekly_noDiscount() {
            Ticket ticket = ticketService.createPass(
                    userId, FareMode.METRO, null, null,
                    TODAY, PassDurationType.WEEKLY, null
            );

            assertMoney("160000", ticket.getPrice());
        }

        @Test
        @DisplayName("METRO MONTHLY 1 tháng, STUDENT 50% → 100000")
        void metro_monthly1_studentDiscount() {
            stubStudentDiscount();

            Ticket ticket = ticketService.createPass(
                    userId, FareMode.METRO, null, PassengerType.STUDENT,
                    TODAY, PassDurationType.MONTHLY, 1
            );

            assertMoney("100000", ticket.getPrice());
        }

        @Test
        @DisplayName("BUS SINGLE_ROUTE MONTHLY 1 tháng, no discount → 140000")
        void busSingleRoute_monthly1_noDiscount() {
            Ticket ticket = ticketService.createPass(
                    userId, FareMode.BUS, PassScope.SINGLE_ROUTE, null,
                    TODAY, PassDurationType.MONTHLY, 1
            );

            assertThat(ticket.getScope()).isEqualTo(PassScope.SINGLE_ROUTE);
            assertMoney("140000", ticket.getPrice());
        }

        @Test
        @DisplayName("BUS MULTI_ROUTE MONTHLY 1 tháng, no discount → 280000")
        void busMultiRoute_monthly1_noDiscount() {
            Ticket ticket = ticketService.createPass(
                    userId, FareMode.BUS, PassScope.MULTI_ROUTE, null,
                    TODAY, PassDurationType.MONTHLY, 1
            );

            assertThat(ticket.getScope()).isEqualTo(PassScope.MULTI_ROUTE);
            assertMoney("280000", ticket.getPrice());
        }

        @Test
        @DisplayName("BUS SINGLE_ROUTE MONTHLY 2 tháng, no discount → 270000")
        void busSingleRoute_monthly2_noDiscount() {
            Ticket ticket = ticketService.createPass(
                    userId, FareMode.BUS, PassScope.SINGLE_ROUTE, null,
                    TODAY, PassDurationType.MONTHLY, 2
            );

            assertMoney("270000", ticket.getPrice());
        }

        @Test
        @DisplayName("BUS SINGLE_ROUTE MONTHLY 1 tháng, STUDENT 50% → 70000")
        void busSingleRoute_monthly1_studentDiscount() {
            stubStudentDiscount();

            Ticket ticket = ticketService.createPass(
                    userId, FareMode.BUS, PassScope.SINGLE_ROUTE, PassengerType.STUDENT,
                    TODAY, PassDurationType.MONTHLY, 1
            );

            assertMoney("70000", ticket.getPrice());
        }

        @Test
        @DisplayName("BUS MULTI_ROUTE MONTHLY 1 tháng, STUDENT 50% → 140000")
        void busMultiRoute_monthly1_studentDiscount() {
            stubStudentDiscount();

            Ticket ticket = ticketService.createPass(
                    userId, FareMode.BUS, PassScope.MULTI_ROUTE, PassengerType.STUDENT,
                    TODAY, PassDurationType.MONTHLY, 1
            );

            assertMoney("140000", ticket.getPrice());
        }

        @Test
        @DisplayName("ANY MONTHLY 1 tháng, no discount → 500000")
        void any_monthly1_noDiscount() {
            Ticket ticket = ticketService.createPass(
                    userId, FareMode.ANY, null, null,
                    TODAY, PassDurationType.MONTHLY, 1
            );

            assertThat(ticket.getScope()).isNull();
            assertMoney("500000", ticket.getPrice());
        }

        @Test
        @DisplayName("no active fare rule → NotFoundException")
        void noFareRule_throws() {
            when(fareRuleRepository.findActiveByMode(FareMode.METRO)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    ticketService.createPass(userId, FareMode.METRO, null, null,
                            TODAY, PassDurationType.MONTHLY, 1))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("BUS không có scope → BusinessRuleException (key không tồn tại)")
        void busWithoutScope_throws() {
            assertThatThrownBy(() ->
                    ticketService.createPass(userId, FareMode.BUS, null, null,
                            TODAY, PassDurationType.MONTHLY, 1))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("METRO có scope → BusinessRuleException (key không tồn tại)")
        void metroWithScope_throws() {
            assertThatThrownBy(() ->
                    ticketService.createPass(userId, FareMode.METRO, PassScope.SINGLE_ROUTE, null,
                            TODAY, PassDurationType.MONTHLY, 1))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("duration không có trong bảng giá → BusinessRuleException")
        void missingDuration_throws() {
            // metroRule không có MONTHLY 6
            assertThatThrownBy(() ->
                    ticketService.createPass(userId, FareMode.METRO, null, null,
                            TODAY, PassDurationType.MONTHLY, 6))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    // ── linkToCard ────────────────────────────────────────────────

    @Nested
    @DisplayName("linkToCard")
    class LinkToCard {

        private Ticket unlinkedTicket;

        @BeforeEach
        void setUpTicket() {
            unlinkedTicket = Ticket.createMonthlyPass(
                    userId, FareMode.METRO, null,
                    Money.of(new BigDecimal("200000")),
                    metroFareRule.getId(), null,
                    TODAY, 30
            );
            when(ticketRepository.findById(unlinkedTicket.getId()))
                    .thenReturn(Optional.of(unlinkedTicket));
            when(ticketRepository.existsActiveTicketByCardId(cardId))
                    .thenReturn(false);
        }

        @Test
        @DisplayName("success → ticket.cardId is set")
        void success_setsCardId() {
            Ticket result = ticketService.linkToCard(unlinkedTicket.getId(), cardId);

            assertThat(result.getCardId()).isEqualTo(cardId);
        }

        @Test
        @DisplayName("ticket not found → NotFoundException")
        void ticketNotFound_throws() {
            UUID unknown = UUID.randomUUID();
            when(ticketRepository.findById(unknown)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.linkToCard(unknown, cardId))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("card not found → NotFoundException")
        void cardNotFound_throws() {
            when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.linkToCard(unlinkedTicket.getId(), cardId))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("card SUSPENDED → BusinessRuleException")
        void cardSuspended_throws() {
            when(activeCard.getStatus()).thenReturn(CardStatus.SUSPENDED);

            assertThatThrownBy(() -> ticketService.linkToCard(unlinkedTicket.getId(), cardId))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("card REVOKED → BusinessRuleException")
        void cardRevoked_throws() {
            when(activeCard.getStatus()).thenReturn(CardStatus.REVOKED);

            assertThatThrownBy(() -> ticketService.linkToCard(unlinkedTicket.getId(), cardId))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("card đã có ticket active → ConflictException")
        void cardAlreadyHasActiveTicket_throws() {
            when(ticketRepository.existsActiveTicketByCardId(cardId)).thenReturn(true);

            assertThatThrownBy(() -> ticketService.linkToCard(unlinkedTicket.getId(), cardId))
                    .isInstanceOf(ConflictException.class);
        }
    }

    // ── unlinkFromCard ────────────────────────────────────────────

    @Nested
    @DisplayName("unlinkFromCard")
    class UnlinkFromCard {

        @Test
        @DisplayName("success → ticket.cardId is null")
        void success_clearsCardId() {
            Ticket ticket = buildLinkedMonthlyPass();
            when(ticketRepository.findActiveTicketByCardId(cardId))
                    .thenReturn(Optional.of(ticket));

            Ticket result = ticketService.unlinkFromCard(cardId);

            assertThat(result.getCardId()).isNull();
        }

        @Test
        @DisplayName("no active ticket for card → NotFoundException")
        void noActiveTicket_throws() {
            when(ticketRepository.findActiveTicketByCardId(cardId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.unlinkFromCard(cardId))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}