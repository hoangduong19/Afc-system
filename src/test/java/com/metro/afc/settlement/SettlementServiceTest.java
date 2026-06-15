package com.metro.afc.settlement;

import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.settlement.application.SettlementService;
import com.metro.afc.settlement.application.port.out.SettlementRepository;
import com.metro.afc.settlement.domain.Settlement;
import com.metro.afc.settlement.domain.enums.settlement.ReconcileStatus;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.PassScope;
import com.metro.afc.trip.application.port.out.TripAnomalyRepository;
import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.Trip;
import com.metro.afc.trip.domain.enums.trip.PaymentMethod;
import com.metro.afc.trip.domain.enums.trip.TicketTypeUsed;
import com.metro.afc.trip.domain.enums.trip.TripStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementService")
class SettlementServiceTest {

    @Mock SettlementRepository  settlementRepository;
    @Mock TripRepository        tripRepository;
    @Mock TripAnomalyRepository anomalyRepository;
    @Mock TicketRepository      ticketRepository;
    @Mock FareRuleRepository    fareRuleRepository;

    @InjectMocks
    SettlementService service;

    private final UUID operatorA = UUID.randomUUID();
    private final UUID operatorB = UUID.randomUUID();
    private final UUID ranBy     = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lenient().when(settlementRepository.existsByPeriod(anyString()))
                .thenReturn(false);
        lenient().when(anomalyRepository.countUnresolvedInPeriod(any(), any()))
                .thenReturn(0L);
        lenient().when(settlementRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));
        lenient().when(fareRuleRepository.findActiveByMode(any()))
                .thenReturn(Optional.empty());
    }

    // ── Happy path ───────────────────────────────────────────────────

    @Test
    @DisplayName("run — chỉ có vé lượt: tạo settlement, phân bổ đúng operator")
    void run_singleTripOnly_createsSettlement() {
        Trip tripA = makeSingleTrip(operatorA, new BigDecimal("50000"),
                new BigDecimal("10"));
        Trip tripB = makeSingleTrip(operatorB, new BigDecimal("30000"),
                new BigDecimal("6"));

        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of(tripA, tripB));

        Settlement result = service.run(6, 2026, ranBy);

        assertNotNull(result);
        assertEquals("2026-06", result.getPeriod());
        assertEquals(0,
                new BigDecimal("80000").compareTo(result.getTotalExpected().getAmount()));
        verify(settlementRepository, atLeastOnce()).saveShare(any());
    }

    @Test
    @DisplayName("run — vé tháng METRO: phân bổ 100% về metro operator")
    void run_metroMonthly_directAllocation() {
        UUID ticketId = UUID.randomUUID();
        Trip trip = makeMonthlyTrip(operatorA, FareMode.METRO, ticketId,
                new BigDecimal("15"));

        Ticket ticket = mockTicket(ticketId,
                Money.of(new BigDecimal("200000")), FareMode.METRO, null);

        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of(trip));
        when(ticketRepository.findAllByIds(anySet()))
                .thenReturn(List.of(ticket));

        Settlement result = service.run(6, 2026, ranBy);

        assertEquals(0,
                new BigDecimal("200000").compareTo(result.getTotalExpected().getAmount()));
        assertEquals(ReconcileStatus.MATCH, result.getReconciliationStatus());
        verify(settlementRepository, times(1)).saveShare(any());
    }

    @Test
    @DisplayName("run — vé tháng MULTI_ROUTE: 2 operators nhận share")
    void run_busMultiRoute_twoShares() {
        UUID ticketId = UUID.randomUUID();
        Trip tripA = makeMonthlyTrip(operatorA, FareMode.BUS, ticketId,
                new BigDecimal("40"));
        Trip tripB = makeMonthlyTrip(operatorB, FareMode.BUS, ticketId,
                new BigDecimal("20"));

        Ticket ticket = mockTicket(ticketId,
                Money.of(new BigDecimal("280000")), FareMode.BUS, PassScope.MULTI_ROUTE);

        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of(tripA, tripB));
        when(ticketRepository.findAllByIds(anySet()))
                .thenReturn(List.of(ticket));

        Settlement result = service.run(6, 2026, ranBy);

        assertEquals(0,
                new BigDecimal("280000").compareTo(result.getTotalExpected().getAmount()));
        verify(settlementRepository, times(2)).saveShare(any());
    }

    @Test
    @DisplayName("run — mix pool1 + pool3: totalExpected = sum cả 2 pool")
    void run_mixPools_correctTotalExpected() {
        Trip singleTrip = makeSingleTrip(operatorA, new BigDecimal("80000"),
                new BigDecimal("10"));

        UUID ticketId = UUID.randomUUID();
        Trip monthlyA = makeMonthlyTrip(operatorA, FareMode.BUS, ticketId,
                new BigDecimal("30"));
        Trip monthlyB = makeMonthlyTrip(operatorB, FareMode.BUS, ticketId,
                new BigDecimal("20"));

        Ticket ticket = mockTicket(ticketId,
                Money.of(new BigDecimal("280000")), FareMode.BUS, PassScope.MULTI_ROUTE);

        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of(singleTrip, monthlyA, monthlyB));
        when(ticketRepository.findAllByIds(anySet()))
                .thenReturn(List.of(ticket));

        Settlement result = service.run(6, 2026, ranBy);

        // 80000 (pool1) + 280000 (pool3) = 360000
        assertEquals(0,
                new BigDecimal("360000").compareTo(result.getTotalExpected().getAmount()));
    }

    // ── Guard conditions ─────────────────────────────────────────────

    @Test
    @DisplayName("run — period đã tồn tại: throw ConflictException")
    void run_periodExists_throwsConflict() {
        when(settlementRepository.existsByPeriod("2026-06")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.run(6, 2026, ranBy));

        verify(tripRepository, never()).findCompletedTripsInPeriod(any(), any());
    }

    @Test
    @DisplayName("run — không có trip nào: throw BusinessRuleException")
    void run_noTrips_throwsBusinessRule() {
        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of());

        assertThrows(BusinessRuleException.class,
                () -> service.run(6, 2026, ranBy));
    }

    @Test
    @DisplayName("run — còn anomaly chưa resolve: throw BusinessRuleException")
    void run_unresolvedAnomalies_throwsBusinessRule() {
        Trip trip = makeSingleTrip(operatorA, new BigDecimal("50000"),
                new BigDecimal("10"));

        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of(trip));
        when(anomalyRepository.countUnresolvedInPeriod(any(), any()))
                .thenReturn(3L);

        assertThrows(BusinessRuleException.class,
                () -> service.run(6, 2026, ranBy));

        verify(settlementRepository, never()).save(any());
    }

    // ── Helpers: real domain objects, không dùng mock Trip ───────────

    private Trip makeSingleTrip(UUID operatorId, BigDecimal fareAmount,
                                BigDecimal distanceKm) {
        return Trip.from(
                UUID.randomUUID(), null, null,
                operatorId,
                null, null, Instant.now(),
                null, null, Instant.now(),
                distanceKm, fareAmount,
                FareMode.BUS,
                PaymentMethod.TICKET,
                TicketTypeUsed.SINGLE_TRIP,
                TripStatus.COMPLETED,
                null
        );
    }

    private Trip makeMonthlyTrip(UUID operatorId, FareMode mode,
                                 UUID ticketId, BigDecimal distanceKm) {
        return Trip.from(
                UUID.randomUUID(), null, ticketId,
                operatorId,
                null, null, Instant.now(),
                null, null, Instant.now(),
                distanceKm, null,
                mode,
                PaymentMethod.TICKET,
                TicketTypeUsed.MONTHLY_PASS,
                TripStatus.COMPLETED,
                null
        );
    }

    private Ticket mockTicket(UUID id, Money price,
                              FareMode mode, PassScope scope) {
        Ticket t = mock(Ticket.class);
        when(t.getId()).thenReturn(id);
        when(t.getPrice()).thenReturn(price);
        when(t.getMode()).thenReturn(mode);
        when(t.getScope()).thenReturn(scope);
        return t;
    }
}