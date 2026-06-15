package com.metro.afc.settlement;

import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.settlement.application.SettlementService;
import com.metro.afc.settlement.application.port.out.SettlementRepository;
import com.metro.afc.settlement.domain.Settlement;
import com.metro.afc.settlement.domain.enums.settlement.SettlementStatus;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.ticket.application.port.out.TicketRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementService (Orchestration Flow)")
class SettlementServiceTest {

    @Mock SettlementRepository settlementRepository;
    @Mock TripRepository tripRepository;
    @Mock TripAnomalyRepository anomalyRepository;
    @Mock TicketRepository ticketRepository;
    @Mock FareRuleRepository fareRuleRepository;

    @InjectMocks
    SettlementService service;

    private final UUID ranBy = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lenient().when(settlementRepository.existsByPeriod(anyString())).thenReturn(false);
        lenient().when(anomalyRepository.countUnresolvedInPeriod(any(), any())).thenReturn(0L);
        lenient().when(settlementRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    @DisplayName("run() - Happy Path: Gom đủ dữ liệu, gọi Domain xử lý và lưu xuống DB")
    void run_validData_delegatesToDomainAndSaves() {
        // Arrange
        Trip singleTrip = makeSingleTrip(UUID.randomUUID(), new BigDecimal("15000"));
        when(tripRepository.findCompletedTripsInPeriod(any(), any())).thenReturn(List.of(singleTrip));
        when(ticketRepository.findAllByIds(any())).thenReturn(List.of());
        when(fareRuleRepository.findAllActive()).thenReturn(List.of());

        // Act
        Settlement result = service.run(6, 2026, ranBy);

        // Assert
        assertNotNull(result);
        assertEquals("2026-06", result.getPeriod());
        assertEquals(SettlementStatus.DRAFT, result.getStatus());

        // Xác minh orchestration: Đã lưu settlement và share
        verify(settlementRepository, times(1)).save(result);
        verify(settlementRepository, atLeastOnce()).saveShare(any());
    }

    @Test
    @DisplayName("run() - Chặn ngoại lệ: Period đã tồn tại (Conflict)")
    void run_periodExists_throwsConflictException() {
        when(settlementRepository.existsByPeriod("2026-06")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.run(6, 2026, ranBy));
        verify(tripRepository, never()).findCompletedTripsInPeriod(any(), any());
    }

    @Test
    @DisplayName("run() - Chặn ngoại lệ: Còn anomaly chưa giải quyết")
    void run_hasUnresolvedAnomalies_throwsBusinessRuleException() {
        when(anomalyRepository.countUnresolvedInPeriod(any(), any())).thenReturn(5L);

        assertThrows(BusinessRuleException.class, () -> service.run(6, 2026, ranBy));
    }

    @Test
    @DisplayName("run() - Chặn ngoại lệ: Không có trip nào trong kỳ")
    void run_noTrips_throwsBusinessRuleException() {
        when(tripRepository.findCompletedTripsInPeriod(any(), any())).thenReturn(List.of());

        assertThrows(BusinessRuleException.class, () -> service.run(6, 2026, ranBy));
    }

    @Test
    @DisplayName("confirm() - Chuyển trạng thái thành công")
    void confirm_draftSettlement_confirmsSuccessfully() {
        // Arrange
        Settlement draft = Settlement.create("2026-06", Money.of(BigDecimal.ZERO), BigDecimal.ZERO, ranBy);
        when(settlementRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

        // Act
        Settlement result = service.confirm(draft.getId(), UUID.randomUUID());

        // Assert
        assertEquals(SettlementStatus.CONFIRMED, result.getStatus());
        assertNotNull(result.getConfirmedAt());
        verify(settlementRepository).save(draft);
    }

    private Trip makeSingleTrip(UUID operatorId, BigDecimal fareAmount) {
        return Trip.from(
                UUID.randomUUID(), null, null, operatorId, null, null, Instant.now(),
                null, null, Instant.now(), new BigDecimal("10"), fareAmount,
                FareMode.BUS, PaymentMethod.TICKET, TicketTypeUsed.SINGLE_TRIP, TripStatus.COMPLETED, null
        );
    }
}