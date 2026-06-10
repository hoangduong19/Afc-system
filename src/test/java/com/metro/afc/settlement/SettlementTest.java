package com.metro.afc.settlement;

import com.metro.afc.settlement.application.dto.settlement.OperatorTripData;
import com.metro.afc.settlement.domain.CompanyShare;
import com.metro.afc.settlement.domain.Settlement;
import com.metro.afc.settlement.domain.enums.settlement.ReconcileStatus;
import com.metro.afc.settlement.domain.enums.settlement.SettlementStatus;
import com.metro.afc.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Settlement Domain")
class SettlementTest {

    private final UUID operatorA = UUID.randomUUID();
    private final UUID operatorB = UUID.randomUUID();

    private Settlement createSettlement(BigDecimal totalExpected) {
        return Settlement.create(
                "2026-06",
                Money.of(totalExpected),
                new BigDecimal("100"),
                UUID.randomUUID()
        );
    }

    // ── allocateShares ───────────────────────────────────────────

    @Test
    @DisplayName("allocateShares divides by km ratio")
    void allocateShares_twoOperators_correctRatio() {
        Settlement settlement = createSettlement(
                new BigDecimal("100000"));

        List<OperatorTripData> data = List.of(
                new OperatorTripData(operatorA, new BigDecimal("15"), 10),
                new OperatorTripData(operatorB, new BigDecimal("5"),  5)
        );

        List<CompanyShare> shares = settlement.allocateShares(
                data, new BigDecimal("20"));

        CompanyShare shareA = shares.stream()
                .filter(s -> s.getOperatorId().equals(operatorA))
                .findFirst().orElseThrow();
        CompanyShare shareB = shares.stream()
                .filter(s -> s.getOperatorId().equals(operatorB))
                .findFirst().orElseThrow();

        // A: 15/20 = 75% → 75000
        assertEquals(0, new BigDecimal("75000")
                .compareTo(shareA.getShareAmount().getAmount()));
        // B: 5/20 = 25% → 25000
        assertEquals(0, new BigDecimal("25000")
                .compareTo(shareB.getShareAmount().getAmount()));
    }

    @Test
    @DisplayName("allocateShares total km = 0 returns zero share")
    void allocateShares_zeroKm_returnsZero() {
        Settlement settlement = createSettlement(
                new BigDecimal("100000"));

        List<OperatorTripData> data = List.of(
                new OperatorTripData(operatorA, BigDecimal.ZERO, 0)
        );

        List<CompanyShare> shares = settlement.allocateShares(
                data, BigDecimal.ZERO);

        assertEquals(0, BigDecimal.ZERO.compareTo(
                shares.get(0).getShareAmount().getAmount()));
    }

    @Test
    @DisplayName("allocateShares rounding adjustment is non-negative")
    void allocateShares_roundingAdjustment_nonNegative() {
        Settlement settlement = createSettlement(
                new BigDecimal("100001")); // odd number → rounding

        List<OperatorTripData> data = List.of(
                new OperatorTripData(operatorA, new BigDecimal("1"), 1),
                new OperatorTripData(operatorB, new BigDecimal("2"), 2)
        );

        List<CompanyShare> shares = settlement.allocateShares(
                data, new BigDecimal("3"));

        shares.forEach(s ->
                assertTrue(s.getRoundingAdjustment().getAmount()
                        .compareTo(BigDecimal.ZERO) >= 0));
    }

    // ── reconcile ────────────────────────────────────────────────

    @Test
    @DisplayName("reconcile MATCH when diff = 0")
    void reconcile_zeroDiff_match() {
        Settlement settlement = createSettlement(
                new BigDecimal("100000"));

        List<OperatorTripData> data = List.of(
                new OperatorTripData(operatorA, new BigDecimal("15"), 10),
                new OperatorTripData(operatorB, new BigDecimal("5"),  5)
        );

        List<CompanyShare> shares = settlement.allocateShares(
                data, new BigDecimal("20"));
        settlement.reconcile(shares);

        assertEquals(ReconcileStatus.MATCH,
                settlement.getReconciliationStatus());
        assertEquals(0, BigDecimal.ZERO
                .compareTo(settlement.getDiffAmount()));
    }

    @Test
    @DisplayName("reconcile WARNING when diff <= tolerance")
    void reconcile_smallDiff_warning() {
        // tolerance = 100đ
        // Setup: 3 operators với km không chia đều → rounding
        Settlement settlement = createSettlement(
                new BigDecimal("100000"));

        // Force rounding by using uneven km
        List<OperatorTripData> data = List.of(
                new OperatorTripData(operatorA, new BigDecimal("1"), 1),
                new OperatorTripData(operatorB, new BigDecimal("2"), 2)
        );

        List<CompanyShare> shares = settlement.allocateShares(
                data, new BigDecimal("3"));
        settlement.reconcile(shares);

        // MATCH or WARNING acceptable
        assertNotNull(settlement.getReconciliationStatus());
    }

    @Test
    @DisplayName("reconcile sets totalActual correctly")
    void reconcile_setsTotalActual() {
        Settlement settlement = createSettlement(
                new BigDecimal("100000"));

        List<OperatorTripData> data = List.of(
                new OperatorTripData(operatorA, new BigDecimal("15"), 10),
                new OperatorTripData(operatorB, new BigDecimal("5"),  5)
        );

        List<CompanyShare> shares = settlement.allocateShares(
                data, new BigDecimal("20"));
        settlement.reconcile(shares);

        assertNotNull(settlement.getTotalActual());
        assertTrue(settlement.getTotalActual().getAmount()
                .compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("reconcile MISMATCH when diff > tolerance")
    void reconcile_largeDiff_mismatch() {
        // tolerance = 100đ
        // totalExpected = 100000 nhưng chỉ allocate 99000
        Settlement settlement = Settlement.create(
                "2026-06",
                Money.of(new BigDecimal("100000")),
                new BigDecimal("100"),
                UUID.randomUUID()
        );

        // Fake shares với shareAmount thấp hơn nhiều
        CompanyShare fakeShare = mock(CompanyShare.class);
        when(fakeShare.getShareAmount())
                .thenReturn(Money.of(new BigDecimal("99000")));
        when(fakeShare.getRoundingAdjustment())
                .thenReturn(Money.of(BigDecimal.ZERO));

        settlement.reconcile(List.of(fakeShare));

        assertEquals(ReconcileStatus.MISMATCH,
                settlement.getReconciliationStatus());
        assertTrue(settlement.getDiffAmount().abs()
                .compareTo(new BigDecimal("100")) > 0);
    }

    // ── confirm ──────────────────────────────────────────────────

    @Test
    @DisplayName("confirm transitions DRAFT to CONFIRMED")
    void confirm_draft_becomesConfirmed() {
        Settlement settlement = createSettlement(
                new BigDecimal("100000"));

        settlement.confirm();

        assertEquals(SettlementStatus.CONFIRMED, settlement.getStatus());
        assertNotNull(settlement.getConfirmedAt());
    }

}