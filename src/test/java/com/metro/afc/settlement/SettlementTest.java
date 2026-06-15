package com.metro.afc.settlement;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.settlement.application.dto.settlement.v2.FareParams;
import com.metro.afc.settlement.application.dto.settlement.v2.TicketRevenueData;
import com.metro.afc.settlement.application.dto.settlement.v2.TripContribution;
import com.metro.afc.settlement.domain.CompanyShare;
import com.metro.afc.settlement.domain.Settlement;
import com.metro.afc.settlement.domain.enums.settlement.ReconcileStatus;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.ticket.domain.enums.PassScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Settlement Domain")
class SettlementTest {

    private final UUID operatorA = UUID.randomUUID();
    private final UUID operatorB = UUID.randomUUID();

    private static final Map<FareMode, FareParams> FARE_PARAMS = Map.of(
            FareMode.METRO, FareParams.METRO_DEFAULT,
            FareMode.BUS,   FareParams.BUS_DEFAULT
    );

    private static final Map<UUID, BigDecimal> EMPTY_KM    = Map.of();
    private static final Map<UUID, Integer>    EMPTY_COUNT = Map.of();

    private Settlement createSettlement(BigDecimal totalExpected) {
        return Settlement.create(
                "2026-06",
                Money.of(totalExpected),
                new BigDecimal("100"),
                UUID.randomUUID()
        );
    }

    private void assertMoney(BigDecimal expected, Money actual) {
        assertEquals(0, expected.compareTo(actual.getAmount()),
                "Expected " + expected + " but got " + actual.getAmount());
    }

    private CompanyShare shareOf(List<CompanyShare> shares, UUID operatorId) {
        return shares.stream()
                .filter(s -> s.getOperatorId().equals(operatorId))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No share for operator " + operatorId));
    }

    // ── Pool 1: Vé lượt ──────────────────────────────────────────────

    @Test
    @DisplayName("pool1 — vé lượt direct: 2 operators nhận đúng fare amount")
    void pool1_singleTrip_directAllocation() {
        Settlement s = createSettlement(new BigDecimal("175000"));

        Map<UUID, Money> singleTripShares = Map.of(
                operatorA, Money.of(new BigDecimal("125000")),
                operatorB, Money.of(new BigDecimal("50000"))
        );

        List<CompanyShare> shares = s.allocateShares(
                singleTripShares, List.of(), FARE_PARAMS, EMPTY_KM, EMPTY_COUNT);

        assertMoney(new BigDecimal("125000"),
                shareOf(shares, operatorA).getShareAmount());
        assertMoney(new BigDecimal("50000"),
                shareOf(shares, operatorB).getShareAmount());
    }

    @Test
    @DisplayName("pool1 — không có vé lượt: không tạo share")
    void pool1_noSingleTrip_noShare() {
        Settlement s = createSettlement(new BigDecimal("0"));

        List<CompanyShare> shares = s.allocateShares(
                Map.of(), List.of(), FARE_PARAMS, EMPTY_KM, EMPTY_COUNT);

        assertTrue(shares.isEmpty());
    }

    // ── Pool 2: Vé tháng METRO ───────────────────────────────────────

    @Test
    @DisplayName("pool2 — metro monthly pass: 100% về operator metro")
    void pool2_metroMonthly_directToMetroOperator() {
        Settlement s = createSettlement(new BigDecimal("200000"));

        TicketRevenueData metroTicket = new TicketRevenueData(
                UUID.randomUUID(),
                Money.of(new BigDecimal("200000")),
                FareMode.METRO,
                null,
                List.of(new TripContribution(
                        operatorA, FareMode.METRO, 20, new BigDecimal("100")))
        );

        List<CompanyShare> shares = s.allocateShares(
                Map.of(), List.of(metroTicket), FARE_PARAMS, EMPTY_KM, EMPTY_COUNT);

        assertEquals(1, shares.size());
        assertMoney(new BigDecimal("200000"),
                shareOf(shares, operatorA).getShareAmount());
    }

    // ── Pool 2: Vé tháng BUS SINGLE_ROUTE ───────────────────────────

    @Test
    @DisplayName("pool2 — bus single route: 100% về operator của tuyến đó")
    void pool2_busSingleRoute_directToOperator() {
        Settlement s = createSettlement(new BigDecimal("140000"));

        TicketRevenueData singleRouteTicket = new TicketRevenueData(
                UUID.randomUUID(),
                Money.of(new BigDecimal("140000")),
                FareMode.BUS,
                PassScope.SINGLE_ROUTE,
                List.of(new TripContribution(
                        operatorB, FareMode.BUS, 15, new BigDecimal("75")))
        );

        List<CompanyShare> shares = s.allocateShares(
                Map.of(), List.of(singleRouteTicket), FARE_PARAMS, EMPTY_KM, EMPTY_COUNT);

        assertEquals(1, shares.size());
        assertMoney(new BigDecimal("140000"),
                shareOf(shares, operatorB).getShareAmount());
    }

    @Test
    @DisplayName("pool2 — contributions rỗng: không tạo share")
    void pool2_emptyContributions_noShare() {
        Settlement s = createSettlement(new BigDecimal("140000"));

        TicketRevenueData ticket = new TicketRevenueData(
                UUID.randomUUID(),
                Money.of(new BigDecimal("140000")),
                FareMode.BUS,
                PassScope.SINGLE_ROUTE,
                List.of()
        );

        List<CompanyShare> shares = s.allocateShares(
                Map.of(), List.of(ticket), FARE_PARAMS, EMPTY_KM, EMPTY_COUNT);

        assertTrue(shares.isEmpty());
    }

    // ── Pool 3: Vé tháng BUS MULTI_ROUTE ────────────────────────────

    @Test
    @DisplayName("pool3 — bus multi route: proportional theo (ai×GiaMoCua + bi×Gia1km)")
    void pool3_busMultiRoute_proportionalAllocation() {
        Settlement s = createSettlement(new BigDecimal("280000"));

        TicketRevenueData ticket = new TicketRevenueData(
                UUID.randomUUID(),
                Money.of(new BigDecimal("280000")),
                FareMode.BUS, PassScope.MULTI_ROUTE,
                List.of(
                        new TripContribution(operatorA, FareMode.BUS, 10,
                                new BigDecimal("50")),
                        new TripContribution(operatorB, FareMode.BUS, 5,
                                new BigDecimal("15"))
                )
        );

        List<CompanyShare> shares = s.allocateShares(
                Map.of(), List.of(ticket), FARE_PARAMS, EMPTY_KM, EMPTY_COUNT);

        BigDecimal shareA = shareOf(shares, operatorA).getShareAmount().getAmount();
        BigDecimal shareB = shareOf(shares, operatorB).getShareAmount().getAmount();

        // Tổng = 280000 (Money scale 2 đảm bảo không mất tiền)
        BigDecimal total = shareA.add(shareB);
        assertEquals(0, new BigDecimal("280000").compareTo(total),
                "Total should be exactly 280000, got: " + total);

        // A lớn hơn B (nhiều trips và km hơn)
        assertTrue(shareA.compareTo(shareB) > 0);
    }

    @Test
    @DisplayName("pool3 — multi route: tổng share = ticket price")
    void pool3_totalShareEqualsTicketPrice() {
        Settlement s = createSettlement(new BigDecimal("280000"));

        TicketRevenueData ticket = new TicketRevenueData(
                UUID.randomUUID(),
                Money.of(new BigDecimal("280000")),
                FareMode.BUS,
                PassScope.MULTI_ROUTE,
                List.of(
                        new TripContribution(operatorA, FareMode.BUS, 8,
                                new BigDecimal("40")),
                        new TripContribution(operatorB, FareMode.BUS, 12,
                                new BigDecimal("60"))
                )
        );

        List<CompanyShare> shares = s.allocateShares(
                Map.of(), List.of(ticket), FARE_PARAMS, EMPTY_KM, EMPTY_COUNT);

        BigDecimal total = shares.stream()
                .map(sh -> sh.getShareAmount().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, new BigDecimal("280000").compareTo(total));
    }

    @Test
    @DisplayName("pool3 — zero weight (0 trips, 0 km): không tạo share")
    void pool3_zeroWeight_noShare() {
        Settlement s = createSettlement(new BigDecimal("280000"));

        TicketRevenueData ticket = new TicketRevenueData(
                UUID.randomUUID(),
                Money.of(new BigDecimal("280000")),
                FareMode.BUS,
                PassScope.MULTI_ROUTE,
                List.of(
                        new TripContribution(operatorA, FareMode.BUS, 0,
                                BigDecimal.ZERO),
                        new TripContribution(operatorB, FareMode.BUS, 0,
                                BigDecimal.ZERO)
                )
        );

        List<CompanyShare> shares = s.allocateShares(
                Map.of(), List.of(ticket), FARE_PARAMS, EMPTY_KM, EMPTY_COUNT);

        assertTrue(shares.isEmpty());
    }

    // ── Pool 3: ANY multimodal ───────────────────────────────────────

    @Test
    @DisplayName("pool3 — multimodal ANY: dùng đúng fare params theo mode của từng operator")
    void pool3_multimodal_correctFareParamsPerMode() {
        Settlement s = createSettlement(new BigDecimal("500000"));

        // weight A (METRO): 5×8000 + 10×850 = 48500
        // weight B (BUS):  10×3000 + 20×450 = 39000
        // total: 87500
        // A: 48500/87500 × 500000 = 277142.857... → scale 2 → 277142.86
        // B: 39000/87500 × 500000 = 222857.142... → scale 2 → 222857.14
        TicketRevenueData ticket = new TicketRevenueData(
                UUID.randomUUID(),
                Money.of(new BigDecimal("500000")),
                FareMode.ANY, null,
                List.of(
                        new TripContribution(operatorA, FareMode.METRO, 5,
                                new BigDecimal("10")),
                        new TripContribution(operatorB, FareMode.BUS,  10,
                                new BigDecimal("20"))
                )
        );

        List<CompanyShare> shares = s.allocateShares(
                Map.of(), List.of(ticket), FARE_PARAMS, EMPTY_KM, EMPTY_COUNT);

        BigDecimal shareA = shareOf(shares, operatorA).getShareAmount().getAmount();
        BigDecimal shareB = shareOf(shares, operatorB).getShareAmount().getAmount();

        // Tổng = 500000
        assertEquals(0, new BigDecimal("500000").compareTo(shareA.add(shareB)));

        // METRO operator lấy nhiều hơn BUS
        assertTrue(shareA.compareTo(shareB) > 0,
                "METRO operator should get more due to higher opening price");

        // Kiểm tra chính xác với scale 2 (match Money.multiply behavior)
        BigDecimal expectedA = new BigDecimal("48500")
                .divide(new BigDecimal("87500"), 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("500000"))
                .setScale(2, RoundingMode.HALF_UP); // ← scale 2, không phải 0
        assertEquals(0, expectedA.compareTo(shareA));
    }

    // ── Mix pools ────────────────────────────────────────────────────

    @Test
    @DisplayName("mix — pool1 + pool2 + pool3: cộng dồn đúng cho từng operator")
    void mix_allPools_accumulatesCorrectly() {
        Settlement s = createSettlement(new BigDecimal("520000"));

        // Pool 1: A = 100000
        Map<UUID, Money> singleTripShares = Map.of(
                operatorA, Money.of(new BigDecimal("100000"))
        );

        // Pool 2: B = 140000 (SINGLE_ROUTE)
        TicketRevenueData singleRoute = new TicketRevenueData(
                UUID.randomUUID(),
                Money.of(new BigDecimal("140000")),
                FareMode.BUS, PassScope.SINGLE_ROUTE,
                List.of(new TripContribution(operatorB, FareMode.BUS, 10,
                        new BigDecimal("50")))
        );

        // Pool 3: 280000 split A/B
        // weight A: 10×3000 + 40×450 = 48000
        // weight B:  5×3000 + 20×450 = 24000
        // total: 72000
        // A: 48000/72000 × 280000 = 186666.67
        // B: 24000/72000 × 280000 =  93333.33
        TicketRevenueData multiRoute = new TicketRevenueData(
                UUID.randomUUID(),
                Money.of(new BigDecimal("280000")),
                FareMode.BUS, PassScope.MULTI_ROUTE,
                List.of(
                        new TripContribution(operatorA, FareMode.BUS, 10,
                                new BigDecimal("40")),
                        new TripContribution(operatorB, FareMode.BUS,  5,
                                new BigDecimal("20"))
                )
        );

        List<CompanyShare> shares = s.allocateShares(
                singleTripShares, List.of(singleRoute, multiRoute),
                FARE_PARAMS, EMPTY_KM, EMPTY_COUNT);

        BigDecimal shareA = shareOf(shares, operatorA).getShareAmount().getAmount();
        BigDecimal shareB = shareOf(shares, operatorB).getShareAmount().getAmount();

        // Tổng = 100000 + 140000 + 280000 = 520000
        assertEquals(0, new BigDecimal("520000").compareTo(shareA.add(shareB)));

        // A: 100000 + 186666.67 = 286666.67
        // B: 140000 +  93333.33 = 233333.33
        assertTrue(shareA.compareTo(shareB) > 0);
    }

    // ── reconcile() ──────────────────────────────────────────────────

    @Test
    @DisplayName("reconcile — diff = 0: MATCH")
    void reconcile_exactMatch_statusMatch() {
        Settlement s = createSettlement(new BigDecimal("200000"));

        List<CompanyShare> shares = List.of(
                CompanyShare.of(s.getId(), operatorA, BigDecimal.ZERO, 0,
                        Money.of(new BigDecimal("200000")),
                        Money.of(new BigDecimal("200000")),
                        Money.of(BigDecimal.ZERO))
        );

        s.reconcile(shares);

        assertEquals(ReconcileStatus.MATCH, s.getReconciliationStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(s.getDiffAmount()));
    }

    @Test
    @DisplayName("reconcile — diff <= tolerance (100đ): WARNING")
    void reconcile_withinTolerance_statusWarning() {
        Settlement s = createSettlement(new BigDecimal("200000"));

        List<CompanyShare> shares = List.of(
                CompanyShare.of(s.getId(), operatorA, BigDecimal.ZERO, 0,
                        Money.of(new BigDecimal("199950")),
                        Money.of(new BigDecimal("199950")),
                        Money.of(BigDecimal.ZERO))
        );

        s.reconcile(shares);

        assertEquals(ReconcileStatus.WARNING, s.getReconciliationStatus());
    }

    @Test
    @DisplayName("reconcile — diff > tolerance: MISMATCH")
    void reconcile_exceedsTolerance_statusMismatch() {
        Settlement s = createSettlement(new BigDecimal("200000"));

        List<CompanyShare> shares = List.of(
                CompanyShare.of(s.getId(), operatorA, BigDecimal.ZERO, 0,
                        Money.of(new BigDecimal("199000")),
                        Money.of(new BigDecimal("199000")),
                        Money.of(BigDecimal.ZERO))
        );

        s.reconcile(shares);

        assertEquals(ReconcileStatus.MISMATCH, s.getReconciliationStatus());
    }
}