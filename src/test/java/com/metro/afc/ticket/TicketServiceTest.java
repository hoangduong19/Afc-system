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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TicketService")
class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private CardRepository cardRepository;
    @Mock private StationRepository stationRepository;
    @Mock private FareRuleRepository fareRuleRepository;
    @Mock private FareDiscountRepository fareDiscountRepository;

    @InjectMocks
    private TicketService ticketService;

    private Card activeCard;
    private Station fromStation;
    private Station toStation;
    private FareRule metroFareRule;
    private FareRule busFareRule;
    private UUID userId;
    private UUID cardId;
    private UUID fromId;
    private UUID toId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cardId = UUID.randomUUID();
        fromId = UUID.randomUUID();
        toId = UUID.randomUUID();

        activeCard = mock(Card.class);
        when(activeCard.getId()).thenReturn(cardId);
        when(activeCard.getStatus()).thenReturn(CardStatus.ACTIVE);
        when(activeCard.getLinkedUserId()).thenReturn(userId);

        fromStation = mock(Station.class);
        when(fromStation.getId()).thenReturn(fromId);
        when(fromStation.getCode()).thenReturn("HN_2A_01");
        when(fromStation.getKmMarker()).thenReturn(new BigDecimal("0.000"));

        toStation = mock(Station.class);
        when(toStation.getId()).thenReturn(toId);
        when(toStation.getCode()).thenReturn("HN_2A_12");
        when(toStation.getKmMarker()).thenReturn(new BigDecimal("12.500"));

        metroFareRule = FareRule.create(
                "HN_METRO_STANDARD",
                FareMode.METRO,
                new BigDecimal("8000"),
                new BigDecimal("850"),
                new BigDecimal("8000"),
                new BigDecimal("30000"),
                LocalDate.of(2025, 7, 1),
                null,
                UUID.randomUUID()
        );

        busFareRule = FareRule.create(
                "HN_BUS_STANDARD",
                FareMode.BUS,
                new BigDecimal("3000"),
                new BigDecimal("450"),
                new BigDecimal("3000"),
                new BigDecimal("30000"),
                LocalDate.of(2025, 7, 1),
                null,
                UUID.randomUUID()
        );

        when(cardRepository.findById(cardId))
                .thenReturn(Optional.of(activeCard));

        when(stationRepository.findById(fromId))
                .thenReturn(Optional.of(fromStation));

        when(stationRepository.findById(toId))
                .thenReturn(Optional.of(toStation));

        when(fareRuleRepository.findActiveByMode(FareMode.METRO))
                .thenReturn(Optional.of(metroFareRule));

        when(fareRuleRepository.findActiveByMode(FareMode.BUS))
                .thenReturn(Optional.of(busFareRule));

        when(fareDiscountRepository.findActiveByPassengerType(any()))
                .thenReturn(Optional.empty());

        when(ticketRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("createSingleTrip without discount returns correct price")
    void createSingleTrip_noDiscount_returnsCorrectPrice() {
        Ticket ticket = ticketService.createSingleTrip(
                userId,
                fromId,
                toId,
                FareMode.METRO,
                null
        );

        assertEquals(TicketType.SINGLE_TRIP, ticket.getType());
        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertEquals(0,
                new BigDecimal("18625").compareTo(ticket.getPrice().getAmount()));
    }

    @Test
    @DisplayName("createSingleTrip with STUDENT 50% discount")
    void createSingleTrip_studentDiscount_returnsHalfPrice() {
        FareDiscount discount = FareDiscount.create(
                PassengerType.STUDENT,
                DiscountType.PERCENT,
                new BigDecimal("50"),
                LocalDate.of(2025, 7, 1),
                null,
                UUID.randomUUID()
        );

        when(fareDiscountRepository.findActiveByPassengerType(PassengerType.STUDENT))
                .thenReturn(Optional.of(discount));

        Ticket ticket = ticketService.createSingleTrip(
                userId,
                fromId,
                toId,
                FareMode.METRO,
                PassengerType.STUDENT
        );

        assertEquals(0,
                new BigDecimal("9312.50").compareTo(ticket.getPrice().getAmount()));
    }

    @Test
    @DisplayName("createSingleTrip same station throws exception")
    void createSingleTrip_sameStation_throwsException() {
        assertThrows(BusinessRuleException.class, () ->
                ticketService.createSingleTrip(
                        userId,
                        fromId,
                        fromId,
                        FareMode.METRO,
                        null
                )
        );
    }

    @Test
    @DisplayName("createSingleTrip station not found throws exception")
    void createSingleTrip_stationNotFound_throwsException() {
        when(stationRepository.findById(fromId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                ticketService.createSingleTrip(
                        userId,
                        fromId,
                        toId,
                        FareMode.METRO,
                        null
                )
        );
    }

    @Test
    @DisplayName("createSingleTrip fare rule not found throws exception")
    void createSingleTrip_noFareRule_throwsException() {
        when(fareRuleRepository.findActiveByMode(FareMode.METRO))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                ticketService.createSingleTrip(
                        userId,
                        fromId,
                        toId,
                        FareMode.METRO,
                        null
                )
        );
    }

    @Test
    @DisplayName("createMonthlyPass METRO normal price is 200000")
    void createMonthlyPass_metro_normalPrice() {
        Ticket ticket = ticketService.createMonthlyPass(
                userId,
                FareMode.METRO,
                null,
                null,
                LocalDate.now(),
                30
        );

        assertEquals(TicketType.MONTHLY_PASS, ticket.getType());
        assertEquals(TicketStatus.ACTIVE, ticket.getStatus());
        assertNull(ticket.getScope());
        assertEquals(0,
                new BigDecimal("200000").compareTo(ticket.getPrice().getAmount()));
    }

    @Test
    @DisplayName("createMonthlyPass METRO STUDENT price is 100000")
    void createMonthlyPass_metro_studentPrice() {
        FareDiscount discount = FareDiscount.create(
                PassengerType.STUDENT,
                DiscountType.PERCENT,
                new BigDecimal("50"),
                LocalDate.of(2025, 7, 1),
                null,
                UUID.randomUUID()
        );

        when(fareDiscountRepository.findActiveByPassengerType(PassengerType.STUDENT))
                .thenReturn(Optional.of(discount));

        Ticket ticket = ticketService.createMonthlyPass(
                userId,
                FareMode.METRO,
                null,
                PassengerType.STUDENT,
                LocalDate.now(),
                30
        );

        assertEquals(TicketType.MONTHLY_PASS, ticket.getType());
        assertNull(ticket.getScope());
        assertEquals(0,
                new BigDecimal("100000").compareTo(ticket.getPrice().getAmount()));
    }

    @Test
    @DisplayName("createMonthlyPass BUS SINGLE_ROUTE normal price is 140000")
    void createMonthlyPass_busSingleRoute_normalPrice() {
        Ticket ticket = ticketService.createMonthlyPass(
                userId,
                FareMode.BUS,
                PassScope.SINGLE_ROUTE,
                null,
                LocalDate.now(),
                30
        );

        assertEquals(TicketType.MONTHLY_PASS, ticket.getType());
        assertEquals(PassScope.SINGLE_ROUTE, ticket.getScope());
        assertEquals(0,
                new BigDecimal("140000").compareTo(ticket.getPrice().getAmount()));
    }

    @Test
    @DisplayName("createMonthlyPass BUS MULTI_ROUTE normal price is 280000")
    void createMonthlyPass_busMultiRoute_normalPrice() {
        Ticket ticket = ticketService.createMonthlyPass(
                userId,
                FareMode.BUS,
                PassScope.MULTI_ROUTE,
                null,
                LocalDate.now(),
                30
        );

        assertEquals(TicketType.MONTHLY_PASS, ticket.getType());
        assertEquals(PassScope.MULTI_ROUTE, ticket.getScope());
        assertEquals(0,
                new BigDecimal("280000").compareTo(ticket.getPrice().getAmount()));
    }

    @Test
    @DisplayName("createMonthlyPass BUS SINGLE_ROUTE STUDENT price is 70000")
    void createMonthlyPass_busSingleRoute_studentPrice() {
        FareDiscount discount = FareDiscount.create(
                PassengerType.STUDENT,
                DiscountType.PERCENT,
                new BigDecimal("50"),
                LocalDate.of(2025, 7, 1),
                null,
                UUID.randomUUID()
        );

        when(fareDiscountRepository.findActiveByPassengerType(PassengerType.STUDENT))
                .thenReturn(Optional.of(discount));

        Ticket ticket = ticketService.createMonthlyPass(
                userId,
                FareMode.BUS,
                PassScope.SINGLE_ROUTE,
                PassengerType.STUDENT,
                LocalDate.now(),
                30
        );

        assertEquals(PassScope.SINGLE_ROUTE, ticket.getScope());
        assertEquals(0,
                new BigDecimal("70000").compareTo(ticket.getPrice().getAmount()));
    }

    @Test
    @DisplayName("createMonthlyPass BUS MULTI_ROUTE STUDENT price is 140000")
    void createMonthlyPass_busMultiRoute_studentPrice() {
        FareDiscount discount = FareDiscount.create(
                PassengerType.STUDENT,
                DiscountType.PERCENT,
                new BigDecimal("50"),
                LocalDate.of(2025, 7, 1),
                null,
                UUID.randomUUID()
        );

        when(fareDiscountRepository.findActiveByPassengerType(PassengerType.STUDENT))
                .thenReturn(Optional.of(discount));

        Ticket ticket = ticketService.createMonthlyPass(
                userId,
                FareMode.BUS,
                PassScope.MULTI_ROUTE,
                PassengerType.STUDENT,
                LocalDate.now(),
                30
        );

        assertEquals(PassScope.MULTI_ROUTE, ticket.getScope());
        assertEquals(0,
                new BigDecimal("140000").compareTo(ticket.getPrice().getAmount()));
    }

    @Test
    @DisplayName("createMonthlyPass ANY normal price is 500000")
    void createMonthlyPass_any_normalPrice() {
        when(fareRuleRepository.findActiveByMode(FareMode.ANY))
                .thenReturn(Optional.of(metroFareRule));

        Ticket ticket = ticketService.createMonthlyPass(
                userId,
                FareMode.ANY,
                null,
                null,
                LocalDate.now(),
                30
        );

        assertEquals(TicketType.MONTHLY_PASS, ticket.getType());
        assertNull(ticket.getScope());
        assertEquals(0,
                new BigDecimal("500000").compareTo(ticket.getPrice().getAmount()));
    }

    @Test
    @DisplayName("createMonthlyPass BUS without scope throws exception")
    void createMonthlyPass_busWithoutScope_throwsException() {
        assertThrows(BusinessRuleException.class, () ->
                ticketService.createMonthlyPass(
                        userId,
                        FareMode.BUS,
                        null,
                        null,
                        LocalDate.now(),
                        30
                )
        );
    }

    @Test
    @DisplayName("createMonthlyPass METRO with scope throws exception")
    void createMonthlyPass_metroWithScope_throwsException() {
        assertThrows(BusinessRuleException.class, () ->
                ticketService.createMonthlyPass(
                        userId,
                        FareMode.METRO,
                        PassScope.SINGLE_ROUTE,
                        null,
                        LocalDate.now(),
                        30
                )
        );
    }

    @Test
    @DisplayName("linkToCard success sets cardId on ticket")
    void linkToCard_success() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId,
                FareMode.METRO,
                null,
                Money.of(new BigDecimal("200000")),
                UUID.randomUUID(),
                null,
                LocalDate.now(),
                30
        );

        when(ticketRepository.findById(ticket.getId()))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.existsActiveTicketByCardId(cardId))
                .thenReturn(false);

        Ticket result = ticketService.linkToCard(ticket.getId(), cardId);

        assertEquals(cardId, result.getCardId());
    }

    @Test
    @DisplayName("linkToCard card not active throws exception")
    void linkToCard_cardNotActive_throwsException() {
        when(activeCard.getStatus())
                .thenReturn(CardStatus.SUSPENDED);

        Ticket ticket = Ticket.createMonthlyPass(
                userId,
                FareMode.METRO,
                null,
                Money.of(new BigDecimal("200000")),
                UUID.randomUUID(),
                null,
                LocalDate.now(),
                30
        );

        when(ticketRepository.findById(ticket.getId()))
                .thenReturn(Optional.of(ticket));

        assertThrows(BusinessRuleException.class, () ->
                ticketService.linkToCard(ticket.getId(), cardId)
        );
    }

    @Test
    @DisplayName("linkToCard card already has active ticket throws exception")
    void linkToCard_cardAlreadyHasTicket_throwsException() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId,
                FareMode.METRO,
                null,
                Money.of(new BigDecimal("200000")),
                UUID.randomUUID(),
                null,
                LocalDate.now(),
                30
        );

        when(ticketRepository.findById(ticket.getId()))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.existsActiveTicketByCardId(cardId))
                .thenReturn(true);

        assertThrows(ConflictException.class, () ->
                ticketService.linkToCard(ticket.getId(), cardId)
        );
    }

    @Test
    @DisplayName("unlinkFromCard success clears cardId")
    void unlinkFromCard_success() {
        Ticket ticket = Ticket.createMonthlyPass(
                userId,
                FareMode.METRO,
                null,
                Money.of(new BigDecimal("200000")),
                UUID.randomUUID(),
                null,
                LocalDate.now(),
                30
        );

        ticket.linkToCard(cardId);

        when(ticketRepository.findActiveTicketByCardId(cardId))
                .thenReturn(Optional.of(ticket));

        Ticket result = ticketService.unlinkFromCard(cardId);

        assertNull(result.getCardId());
    }

    @Test
    @DisplayName("unlinkFromCard ticket not found throws exception")
    void unlinkFromCard_notFound_throwsException() {
        when(ticketRepository.findActiveTicketByCardId(cardId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                ticketService.unlinkFromCard(cardId)
        );
    }
}