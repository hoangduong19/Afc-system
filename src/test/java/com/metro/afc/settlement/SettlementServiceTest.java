package com.metro.afc.settlement;

import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.settlement.application.SettlementService;
import com.metro.afc.settlement.application.port.out.SettlementRepository;
import com.metro.afc.settlement.domain.Settlement;
import com.metro.afc.settlement.domain.enums.settlement.SettlementStatus;
import com.metro.afc.settlement.domain.settlementAllocation.AllocationStrategy;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.trip.application.port.out.TripAnomalyRepository;
import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.Trip;
import com.metro.afc.trip.domain.enums.trip.TicketTypeUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementService (Orchestration Flow)")
class SettlementServiceTest {

    @Mock SettlementRepository  settlementRepository;
    @Mock TripRepository        tripRepository;
    @Mock TripAnomalyRepository anomalyRepository;
    @Mock TicketRepository      ticketRepository;
    @Mock FareRuleRepository    fareRuleRepository;
    @Mock AllocationStrategy    allocationStrategy;

    private SettlementService service;

    private final UUID operatorId = UUID.randomUUID();
    private final UUID ranBy      = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new SettlementService(
                settlementRepository, tripRepository, anomalyRepository,
                ticketRepository, fareRuleRepository, allocationStrategy);

        lenient().when(settlementRepository.existsByPeriod(anyString())).thenReturn(false);
        lenient().when(anomalyRepository.countUnresolvedInPeriod(any(), any())).thenReturn(0L);
        lenient().when(settlementRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(allocationStrategy.formulaCode()).thenReturn("QD3316_2025");
        lenient().when(ticketRepository.findActiveInPeriod(any(), any())).thenReturn(List.of());
    }

    @Test
    @DisplayName("run() - Happy Path: Gom đủ dữ liệu, delegate domain, lưu kết quả")
    void run_validData_delegatesToDomainAndSaves() {
        BigDecimal amount = new BigDecimal("15000.00");
        Trip trip = makeSingleTrip(amount);

        // Mock 1 Ticket tương ứng bán ra trong kỳ để tổng Expected khớp với Actual (15,000)
        Ticket mockTicket = mock(Ticket.class);
        when(mockTicket.getPrice()).thenReturn(Money.of(amount));

        when(tripRepository.findCompletedTripsInPeriod(any(), any())).thenReturn(List.of(trip));
        when(ticketRepository.findActiveInPeriod(any(), any())).thenReturn(List.of(mockTicket)); // <-- Sửa ở đây
        when(ticketRepository.findAllByIds(any())).thenReturn(List.of());
        when(fareRuleRepository.findAllActive()).thenReturn(List.of());
        when(allocationStrategy.allocate(any(), any(), any()))
                .thenReturn(Map.of(operatorId, Money.of(amount)));

        Settlement result = service.run(6, 2026, ranBy);

        assertNotNull(result);
        assertEquals("2026-06", result.getPeriod());
        assertEquals(SettlementStatus.DRAFT, result.getStatus());
        assertEquals("QD3316_2025", result.getFormulaCode());

        verify(settlementRepository).save(result);
        verify(settlementRepository, atLeastOnce()).saveShare(any());
        verify(settlementRepository, never()).saveLog(any()); // Vượt qua an toàn vì trạng thái giờ là MATCH!
    }

    @Test
    @DisplayName("run() - Ghi ReconciliationLog khi kết quả không MATCH")
    void run_mismatch_savesReconciliationLog() {
        Trip trip = makeSingleTrip(new BigDecimal("15000"));

        when(tripRepository.findCompletedTripsInPeriod(any(), any())).thenReturn(List.of(trip));
        when(ticketRepository.findAllByIds(any())).thenReturn(List.of());
        when(fareRuleRepository.findAllActive()).thenReturn(List.of());
        when(allocationStrategy.allocate(any(), any(), any()))
                .thenReturn(Map.of(operatorId, Money.of(new BigDecimal("14950"))));

        service.run(6, 2026, ranBy);

        verify(settlementRepository).saveLog(any());
    }

    @Test
    @DisplayName("run() - ConflictException khi period đã tồn tại")
    void run_periodExists_throwsConflict() {
        when(settlementRepository.existsByPeriod("2026-06")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.run(6, 2026, ranBy));
        verify(tripRepository, never()).findCompletedTripsInPeriod(any(), any());
    }

    @Test
    @DisplayName("run() - BusinessRuleException khi còn anomaly chưa giải quyết")
    void run_hasUnresolvedAnomalies_throwsBusinessRule() {
        when(anomalyRepository.countUnresolvedInPeriod(any(), any())).thenReturn(3L);

        assertThrows(BusinessRuleException.class, () -> service.run(6, 2026, ranBy));
        verify(tripRepository, never()).findCompletedTripsInPeriod(any(), any());
    }

    @Test
    @DisplayName("run() - BusinessRuleException khi không có trip nào trong kỳ")
    void run_noTrips_throwsBusinessRule() {
        when(tripRepository.findCompletedTripsInPeriod(any(), any())).thenReturn(List.of());

        assertThrows(BusinessRuleException.class, () -> service.run(6, 2026, ranBy));
        verify(allocationStrategy, never()).allocate(any(), any(), any());
    }

    @Test
    @DisplayName("confirm() - Chuyển DRAFT → CONFIRMED thành công")
    void confirm_draft_confirmsSuccessfully() {
        Settlement draft = Settlement.create(
                "2026-06", Money.of(BigDecimal.ZERO), BigDecimal.ZERO, ranBy);
        when(settlementRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

        Settlement result = service.confirm(draft.getId(), UUID.randomUUID());

        assertEquals(SettlementStatus.CONFIRMED, result.getStatus());
        assertNotNull(result.getConfirmedAt());
        verify(settlementRepository).save(draft);
    }

    @Test
    @DisplayName("confirm() - BusinessRuleException khi settlement không ở trạng thái DRAFT")
    void confirm_nonDraft_throwsBusinessRule() {
        Settlement draft = Settlement.create(
                "2026-06", Money.of(BigDecimal.ZERO), BigDecimal.ZERO, ranBy);
        draft.confirm();
        when(settlementRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

        assertThrows(BusinessRuleException.class,
                () -> service.confirm(draft.getId(), UUID.randomUUID()));
    }

    private Trip makeSingleTrip(BigDecimal fareAmount) {
        return Trip.from(
                UUID.randomUUID(), null, null, operatorId,
                null, Instant.now(),
                null, Instant.now(),
                new BigDecimal("10"), fareAmount,
                FareMode.BUS, TicketTypeUsed.SINGLE_TRIP
        );
    }
}