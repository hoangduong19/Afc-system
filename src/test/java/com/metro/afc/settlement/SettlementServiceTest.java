package com.metro.afc.settlement;

import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.settlement.application.SettlementService;
import com.metro.afc.settlement.application.dto.settlement.OperatorTripData;
import com.metro.afc.settlement.application.port.out.SettlementRepository;
import com.metro.afc.settlement.domain.CompanyShare;
import com.metro.afc.settlement.domain.Settlement;
import com.metro.afc.settlement.domain.enums.settlement.ReconcileStatus;
import com.metro.afc.settlement.domain.enums.settlement.SettlementStatus;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ConflictException;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.Trip;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SettlementService")
class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;
    @Mock private TripRepository tripRepository;
    @Mock private OperatorRepository operatorRepository;

    @InjectMocks
    private SettlementService settlementService;

    private final UUID operatorHURC      = UUID.randomUUID();
    private final UUID operatorTRANSERCO = UUID.randomUUID();
    private final UUID ranBy             = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(settlementRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));
        when(settlementRepository.saveShare(any()))
                .thenAnswer(i -> i.getArgument(0));
        when(settlementRepository.saveLog(any()))
                .thenAnswer(i -> i.getArgument(0));
        when(settlementRepository.existsByPeriod(any()))
                .thenReturn(false);
    }

    // ── Helpers ──────────────────────────────────────────────────

    private Trip mockTrip(UUID operatorId,
                          BigDecimal distanceKm,
                          BigDecimal fareAmount) {
        Trip trip = mock(Trip.class);
        when(trip.getOperatorId()).thenReturn(operatorId);
        when(trip.getDistanceKm()).thenReturn(distanceKm);
        when(trip.getFareAmount()).thenReturn(fareAmount);
        return trip;
    }

    private void assertMoney(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                "Expected " + expected + " but was " + actual);
    }

    // ── run — success ────────────────────────────────────────────

    @Test
    @DisplayName("run creates settlement with correct period")
    void run_createsSettlement_correctPeriod() {
        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of(
                        mockTrip(operatorHURC, new BigDecimal("10"),
                                new BigDecimal("50000"))
                ));

        Settlement result = settlementService.run(6, 2026, ranBy);

        assertEquals("2026-06", result.getPeriod());
        assertEquals(SettlementStatus.DRAFT, result.getStatus());
    }

    @Test
    @DisplayName("run calculates correct totalExpected")
    void run_calculatesTotalExpected() {
        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of(
                        mockTrip(operatorHURC,
                                new BigDecimal("7.5"),  new BigDecimal("14375")),
                        mockTrip(operatorTRANSERCO,
                                new BigDecimal("12.4"), new BigDecimal("8580"))
                ));

        Settlement result = settlementService.run(6, 2026, ranBy);

        assertMoney(new BigDecimal("22955"),
                result.getTotalExpected().getAmount());
    }

    @Test
    @DisplayName("run single operator gets 100% share")
    void run_singleOperator_gets100Percent() {
        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of(
                        mockTrip(operatorHURC,
                                new BigDecimal("10"), new BigDecimal("100000")),
                        mockTrip(operatorHURC,
                                new BigDecimal("5"),  new BigDecimal("50000"))
                ));

        Settlement result = settlementService.run(6, 2026, ranBy);

        assertEquals(ReconcileStatus.MATCH,
                result.getReconciliationStatus());
        assertMoney(BigDecimal.ZERO, result.getDiffAmount());
    }

    @Test
    @DisplayName("run two operators MATCH reconciliation")
    void run_twoOperators_matchReconciliation() {
        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of(
                        mockTrip(operatorHURC,
                                new BigDecimal("15"), new BigDecimal("75000")),
                        mockTrip(operatorTRANSERCO,
                                new BigDecimal("5"),  new BigDecimal("25000"))
                ));

        Settlement result = settlementService.run(6, 2026, ranBy);

        assertEquals(ReconcileStatus.MATCH,
                result.getReconciliationStatus());
        assertMoney(BigDecimal.ZERO, result.getDiffAmount());
    }

    @Test
    @DisplayName("run saves settlement to repository")
    void run_savesCalled() {
        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of(
                        mockTrip(operatorHURC,
                                new BigDecimal("10"), new BigDecimal("50000"))
                ));

        settlementService.run(6, 2026, ranBy);

        verify(settlementRepository, atLeast(2))
                .save(any(Settlement.class));
    }

    @Test
    @DisplayName("run saves company shares")
    void run_savesCompanyShares() {
        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of(
                        mockTrip(operatorHURC,
                                new BigDecimal("15"), new BigDecimal("75000")),
                        mockTrip(operatorTRANSERCO,
                                new BigDecimal("5"),  new BigDecimal("25000"))
                ));

        settlementService.run(6, 2026, ranBy);

        verify(settlementRepository, times(2))
                .saveShare(any(CompanyShare.class));
    }

    @Test
    @DisplayName("run does not save log when MATCH")
    void run_match_noLogSaved() {
        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of(
                        mockTrip(operatorHURC,
                                new BigDecimal("15"), new BigDecimal("75000")),
                        mockTrip(operatorTRANSERCO,
                                new BigDecimal("5"),  new BigDecimal("25000"))
                ));

        settlementService.run(6, 2026, ranBy);

        verify(settlementRepository, never())
                .saveLog(any());
    }

    @Test
    @DisplayName("run trips with null fare handled gracefully")
    void run_nullFareAmount_treatedAsZero() {
        Trip tripWithNullFare = mock(Trip.class);
        when(tripWithNullFare.getOperatorId()).thenReturn(operatorHURC);
        when(tripWithNullFare.getDistanceKm())
                .thenReturn(new BigDecimal("5"));
        when(tripWithNullFare.getFareAmount()).thenReturn(null);

        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of(tripWithNullFare));

        assertDoesNotThrow(() ->
                settlementService.run(6, 2026, ranBy));
    }

    @Test
    @DisplayName("run trips with null operator skipped")
    void run_nullOperatorId_skipped() {
        Trip tripWithNullOperator = mock(Trip.class);
        when(tripWithNullOperator.getOperatorId()).thenReturn(null);
        when(tripWithNullOperator.getDistanceKm())
                .thenReturn(new BigDecimal("5"));
        when(tripWithNullOperator.getFareAmount())
                .thenReturn(new BigDecimal("10000"));

        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of(tripWithNullOperator));

        Settlement result = settlementService.run(6, 2026, ranBy);

        assertNotNull(result);
        verify(settlementRepository, never())
                .saveShare(any());
    }

    // ── run — exceptions ─────────────────────────────────────────

    @Test
    @DisplayName("run period already exists throws ConflictException")
    void run_periodExists_throwsConflict() {
        when(settlementRepository.existsByPeriod("2026-06"))
                .thenReturn(true);

        assertThrows(ConflictException.class, () ->
                settlementService.run(6, 2026, ranBy));
    }

    @Test
    @DisplayName("run no trips throws BusinessRuleException")
    void run_noTrips_throwsBusinessRule() {
        when(tripRepository.findCompletedTripsInPeriod(any(), any()))
                .thenReturn(List.of());

        assertThrows(BusinessRuleException.class, () ->
                settlementService.run(6, 2026, ranBy));
    }

    // ── confirm ──────────────────────────────────────────────────

    @Test
    @DisplayName("confirm DRAFT MATCH settlement succeeds")
    void confirm_draftMatch_succeeds() {
        Settlement settlement = Settlement.create(
                "2026-06", Money.of(new BigDecimal("100000")),
                new BigDecimal("100"), ranBy);

        List<CompanyShare> shares = settlement.allocateShares(
                List.of(new OperatorTripData(
                        operatorHURC, new BigDecimal("20"), 10)),
                new BigDecimal("20"));
        settlement.reconcile(shares);

        when(settlementRepository.findById(settlement.getId()))
                .thenReturn(Optional.of(settlement));

        Settlement result = settlementService
                .confirm(settlement.getId(), ranBy);

        assertEquals(SettlementStatus.CONFIRMED, result.getStatus());
        assertNotNull(result.getConfirmedAt());
    }

    @Test
    @DisplayName("confirm DRAFT WARNING settlement succeeds")
    void confirm_draftWarning_succeeds() {
        Settlement settlement = mock(Settlement.class);
        when(settlement.getStatus())
                .thenReturn(SettlementStatus.DRAFT);
        when(settlement.getReconciliationStatus())
                .thenReturn(ReconcileStatus.WARNING);
        when(settlementRepository.findById(any()))
                .thenReturn(Optional.of(settlement));
        when(settlementRepository.save(any()))
                .thenReturn(settlement);

        assertDoesNotThrow(() ->
                settlementService.confirm(UUID.randomUUID(), ranBy));

        verify(settlement).confirm();
    }

    @Test
    @DisplayName("confirm MISMATCH throws BusinessRuleException")
    void confirm_mismatch_throwsException() {
        Settlement settlement = mock(Settlement.class);
        when(settlement.getStatus())
                .thenReturn(SettlementStatus.DRAFT);
        when(settlement.getReconciliationStatus())
                .thenReturn(ReconcileStatus.MISMATCH);
        when(settlementRepository.findById(any()))
                .thenReturn(Optional.of(settlement));

        assertThrows(BusinessRuleException.class, () ->
                settlementService.confirm(UUID.randomUUID(), ranBy));
    }

    @Test
    @DisplayName("confirm already CONFIRMED throws BusinessRuleException")
    void confirm_alreadyConfirmed_throwsException() {
        Settlement settlement = mock(Settlement.class);
        when(settlement.getStatus())
                .thenReturn(SettlementStatus.CONFIRMED);
        when(settlementRepository.findById(any()))
                .thenReturn(Optional.of(settlement));

        assertThrows(BusinessRuleException.class, () ->
                settlementService.confirm(UUID.randomUUID(), ranBy));
    }

    @Test
    @DisplayName("confirm not found throws NotFoundException")
    void confirm_notFound_throwsNotFoundException() {
        when(settlementRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                settlementService.confirm(UUID.randomUUID(), ranBy));
    }
}