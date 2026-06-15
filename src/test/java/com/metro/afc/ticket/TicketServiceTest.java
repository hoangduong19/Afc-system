package com.metro.afc.ticket;

import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.card.domain.model.enums.CardStatus;
import com.metro.afc.fare.application.port.out.FareDiscountRepository;
import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.FareDiscount;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Monthly pass price matrix (fixtures below):
 *
 * METRO  monthlySinglePrice = 200_000  (scope: null)
 * BUS    monthlySinglePrice = 140_000  (scope: SINGLE_ROUTE)
 *        monthlyMultiPrice  = 280_000  (scope: MULTI_ROUTE)
 * ANY    monthlySinglePrice = 500_000  (scope: null)
 *
 * STUDENT 50% discount halves those values.
 *
 * Single-trip (METRO, 12.5 km):
 *   8000 + 12.5 × 850 = 18625
 *   50% discount       → 9312.50
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

    private static final UUID   ACTOR      = UUID.randomUUID();
    private static final LocalDate VALID_FROM = LocalDate.of(2025, 7, 1);
    private static final LocalDate TODAY      = LocalDate.now();

    private UUID    userId;
    private UUID    cardId;
    private UUID    fromId;
    private UUID    toId;
    private Card    activeCard;
    private Station fromStation;
    private Station toStation;

    /*
     * METRO rule:
     *   baseFare=8000, rate=850, min=8000, max=30000
     *   monthlySinglePrice=200_000, monthlyMultiPrice=null
     */
    private FareRule metroFareRule;

    /*
     * BUS rule:
     *   baseFare=3000, rate=450, min=3000, max=30000
     *   monthlySinglePrice=140_000 (SINGLE_ROUTE), monthlyMultiPrice=280_000 (MULTI_ROUTE)
     */
    private FareRule busFareRule;

    /*
     * ANY rule: monthlySinglePrice=500_000, scope must be null
     */
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
        when(activeCard.getLinkedUserId()).thenReturn(userId);

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
                new BigDecimal("200000"), null,         // monthlySingle=200k, monthlyMulti=null
                VALID_FROM, null, ACTOR
        );

        busFareRule = FareRule.create(
                "HN_BUS_STANDARD", FareMode.BUS,
                new BigDecimal("3000"), new BigDecimal("450"),
                new BigDecimal("3000"), new BigDecimal("30000"),
                new BigDecimal("140000"), new BigDecimal("280000"), // single=140k, multi=280k
                VALID_FROM, null, ACTOR
        );

        anyFareRule = FareRule.create(
                "HN_ANY_STANDARD", FareMode.ANY,
                new BigDecimal("8000"), new BigDecimal("850"),
                new BigDecimal("8000"), new BigDecimal("30000"),
                new BigDecimal("500000"), null,
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
        @DisplayName("METRO 12.5 km, no discount → price 18625, type SINGLE_TRIP, status ACTIVE")
        void noDiscount_correctPriceAndStatus() {
            Ticket ticket = ticketService.createSingleTrip(
                    userId, fromId, toId, FareMode.METRO, null
            );

            assertThat(ticket.getType()).isEqualTo(TicketType.SINGLE_TRIP);
            assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ACTIVE);
            assertMoney("18625", ticket.getPrice());
        }

        @Test
        @DisplayName("METRO 12.5 km, STUDENT 50% discount → price 9312.50")
        void studentDiscount_halfPrice() {
            stubStudentDiscount();

            Ticket ticket = ticketService.createSingleTrip(
                    userId, fromId, toId, FareMode.METRO, PassengerType.STUDENT
            );

            assertMoney("9312.50", ticket.getPrice());
        }

        @Test
        @DisplayName("METRO 12.5 km, SENIOR 100% discount → price 0")
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
        @DisplayName("passengerType provided but no active discount → full price applied")
        void noActiveDiscount_fullPrice() {
            when(fareDiscountRepository.findActiveByPassengerType(PassengerType.STUDENT))
                    .thenReturn(Optional.empty());

            Ticket ticket = ticketService.createSingleTrip(
                    userId, fromId, toId, FareMode.METRO, PassengerType.STUDENT
            );

            assertMoney("18625", ticket.getPrice());
        }

        @Test
        @DisplayName("BUS 12.5 km, no discount → price 8625")
        void bus_noDiscount_correctPrice() {
            // 3000 + 12.5 × 450 = 8625
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
            // findById(fromId) is stubbed to return fromStation for both calls;
            // service must throw before reaching fareRule lookup
            when(stationRepository.findById(fromId)).thenReturn(Optional.of(fromStation));

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

    // ── createMonthlyPass ─────────────────────────────────────────

    @Nested
    @DisplayName("createMonthlyPass")
    class CreateMonthlyPass {

        @Test
        @DisplayName("METRO 30-day, no discount → 200000, type MONTHLY_PASS, scope null")
        void metro_noDiscount_correctPrice() {
            Ticket ticket = ticketService.createMonthlyPass(
                    userId, FareMode.METRO, null, null, TODAY, 30
            );

            assertThat(ticket.getType()).isEqualTo(TicketType.MONTHLY_PASS);
            assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ACTIVE);
            assertThat(ticket.getScope()).isNull();
            assertMoney("200000", ticket.getPrice());
        }

        @Test
        @DisplayName("METRO 30-day, STUDENT 50% → 100000")
        void metro_studentDiscount_halfPrice() {
            stubStudentDiscount();

            Ticket ticket = ticketService.createMonthlyPass(
                    userId, FareMode.METRO, null, PassengerType.STUDENT, TODAY, 30
            );

            assertThat(ticket.getScope()).isNull();
            assertMoney("100000", ticket.getPrice());
        }

        @Test
        @DisplayName("BUS SINGLE_ROUTE 30-day, no discount → 140000")
        void busSingleRoute_noDiscount_correctPrice() {
            Ticket ticket = ticketService.createMonthlyPass(
                    userId, FareMode.BUS, PassScope.SINGLE_ROUTE, null, TODAY, 30
            );

            assertThat(ticket.getType()).isEqualTo(TicketType.MONTHLY_PASS);
            assertThat(ticket.getScope()).isEqualTo(PassScope.SINGLE_ROUTE);
            assertMoney("140000", ticket.getPrice());
        }

        @Test
        @DisplayName("BUS MULTI_ROUTE 30-day, no discount → 280000")
        void busMultiRoute_noDiscount_correctPrice() {
            Ticket ticket = ticketService.createMonthlyPass(
                    userId, FareMode.BUS, PassScope.MULTI_ROUTE, null, TODAY, 30
            );

            assertThat(ticket.getScope()).isEqualTo(PassScope.MULTI_ROUTE);
            assertMoney("280000", ticket.getPrice());
        }

        @Test
        @DisplayName("BUS SINGLE_ROUTE 30-day, STUDENT 50% → 70000")
        void busSingleRoute_studentDiscount_halfPrice() {
            stubStudentDiscount();

            Ticket ticket = ticketService.createMonthlyPass(
                    userId, FareMode.BUS, PassScope.SINGLE_ROUTE, PassengerType.STUDENT, TODAY, 30
            );

            assertThat(ticket.getScope()).isEqualTo(PassScope.SINGLE_ROUTE);
            assertMoney("70000", ticket.getPrice());
        }

        @Test
        @DisplayName("BUS MULTI_ROUTE 30-day, STUDENT 50% → 140000")
        void busMultiRoute_studentDiscount_halfPrice() {
            stubStudentDiscount();

            Ticket ticket = ticketService.createMonthlyPass(
                    userId, FareMode.BUS, PassScope.MULTI_ROUTE, PassengerType.STUDENT, TODAY, 30
            );

            assertThat(ticket.getScope()).isEqualTo(PassScope.MULTI_ROUTE);
            assertMoney("140000", ticket.getPrice());
        }

        @Test
        @DisplayName("ANY 30-day, no discount → 500000")
        void any_noDiscount_correctPrice() {
            Ticket ticket = ticketService.createMonthlyPass(
                    userId, FareMode.ANY, null, null, TODAY, 30
            );

            assertThat(ticket.getType()).isEqualTo(TicketType.MONTHLY_PASS);
            assertThat(ticket.getScope()).isNull();
            assertMoney("500000", ticket.getPrice());
        }

        @Test
        @DisplayName("METRO 15-day, no discount → 100000 (pro-rated)")
        void metro_15day_proRated() {
            // 200000 × 15 / 30 = 100000
            Ticket ticket = ticketService.createMonthlyPass(
                    userId, FareMode.METRO, null, null, TODAY, 15
            );

            assertMoney("100000", ticket.getPrice());
        }

        @Test
        @DisplayName("BUS SINGLE_ROUTE 7-day → 32667 (HALF_UP pro-rated)")
        void busSingleRoute_7day_proRated() {
            // 140000 × 7 / 30 = 32666.67 → HALF_UP → 32667
            Ticket ticket = ticketService.createMonthlyPass(
                    userId, FareMode.BUS, PassScope.SINGLE_ROUTE, null, TODAY, 7
            );

            assertMoney("32667", ticket.getPrice());
        }

        @Test
        @DisplayName("no active fare rule → NotFoundException")
        void noFareRule_throws() {
            when(fareRuleRepository.findActiveByMode(FareMode.METRO)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    ticketService.createMonthlyPass(userId, FareMode.METRO, null, null, TODAY, 30))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("BUS without scope → BusinessRuleException (domain validation)")
        void busWithoutScope_throws() {
            assertThatThrownBy(() ->
                    ticketService.createMonthlyPass(userId, FareMode.BUS, null, null, TODAY, 30))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("METRO with scope → BusinessRuleException (domain validation)")
        void metroWithScope_throws() {
            assertThatThrownBy(() ->
                    ticketService.createMonthlyPass(userId, FareMode.METRO, PassScope.SINGLE_ROUTE, null, TODAY, 30))
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
        @DisplayName("card BLOCKED → BusinessRuleException")
        void cardBlocked_throws() {
            when(activeCard.getStatus()).thenReturn(CardStatus.REVOKED);

            assertThatThrownBy(() -> ticketService.linkToCard(unlinkedTicket.getId(), cardId))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("card already has active ticket → ConflictException")
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