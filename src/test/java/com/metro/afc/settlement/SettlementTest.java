package com.metro.afc.settlement;

import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.settlement.domain.CompanyShare;
import com.metro.afc.settlement.domain.Settlement;
import com.metro.afc.settlement.domain.Settlement.SettlementResult;
import com.metro.afc.settlement.domain.enums.settlement.ReconcileStatus;
import com.metro.afc.settlement.domain.settlementAllocation.AllocationStrategy;
import com.metro.afc.settlement.domain.settlementAllocation.Qd3316AllocationStrategy;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.PassScope;
import com.metro.afc.trip.domain.Trip;
import com.metro.afc.trip.domain.enums.trip.PaymentMethod;
import com.metro.afc.trip.domain.enums.trip.TicketTypeUsed;
import com.metro.afc.trip.domain.enums.trip.TripStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Settlement Domain Logic (QĐ 3316)")
class SettlementTest {

    private final UUID operatorA  = UUID.randomUUID();
    private final UUID operatorB  = UUID.randomUUID();
    private final UUID ranBy      = UUID.randomUUID();
    private final BigDecimal tolerance = new BigDecimal("100");

    // Dùng real implementation — unit test domain logic, không mock công thức
    private final AllocationStrategy strategy = new Qd3316AllocationStrategy();

    // ── Pool 1 ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("1. Pool 1: Phân bổ Vé Lượt (Single Trip)")
    class SingleTripAllocation {

        @Test
        @DisplayName("Tiền vé lượt chảy trực tiếp về nhà xe vận hành chuyến đó")
        void singleTrips_directAllocation() {
            Trip tripA = makeTrip(operatorA, TicketTypeUsed.SINGLE_TRIP, null, "125000", "10", FareMode.BUS);
            Trip tripB = makeTrip(operatorB, TicketTypeUsed.SINGLE_TRIP, null, "50000",  "5",  FareMode.BUS);

            SettlementResult result = Settlement.calculateAndSettle(
                    "2026-06", List.of(tripA, tripB), Map.of(), List.of(),
                    strategy, tolerance, ranBy);

            assertEquals("175000.00", result.settlement().getTotalExpected().getAmount().toPlainString());
            assertEquals("125000.00", findShare(result.shares(), operatorA).getShareAmount().getAmount().toPlainString());
            assertEquals("50000.00",  findShare(result.shares(), operatorB).getShareAmount().getAmount().toPlainString());
            assertEquals(ReconcileStatus.MATCH, result.settlement().getReconciliationStatus());
        }

        @Test
        @DisplayName("Nhiều chuyến cùng 1 operator được cộng dồn")
        void multipleTrips_sameOperator_accumulated() {
            Trip trip1 = makeTrip(operatorA, TicketTypeUsed.SINGLE_TRIP, null, "50000", "5", FareMode.BUS);
            Trip trip2 = makeTrip(operatorA, TicketTypeUsed.SINGLE_TRIP, null, "75000", "8", FareMode.BUS);

            SettlementResult result = Settlement.calculateAndSettle(
                    "2026-06", List.of(trip1, trip2), Map.of(), List.of(),
                    strategy, tolerance, ranBy);

            assertEquals(1, result.shares().size());
            assertEquals("125000.00", findShare(result.shares(), operatorA).getShareAmount().getAmount().toPlainString());
        }
    }

    // ── Pool 2 ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("2. Pool 2: Phân bổ trực tiếp (METRO / SINGLE_ROUTE)")
    class DirectMonthlyAllocation {

        @Test
        @DisplayName("Vé tháng METRO phân bổ 100% doanh thu về 1 nhà xe")
        void metroMonthly_directAllocation() {
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = mockTicket(ticketId, "200000", FareMode.METRO, null);

            Trip trip1 = makeTrip(operatorA, TicketTypeUsed.MONTHLY_PASS, ticketId, null, "10", FareMode.METRO);
            Trip trip2 = makeTrip(operatorA, TicketTypeUsed.MONTHLY_PASS, ticketId, null, "15", FareMode.METRO);

            SettlementResult result = Settlement.calculateAndSettle(
                    "2026-06", List.of(trip1, trip2), Map.of(ticketId, ticket), List.of(),
                    strategy, tolerance, ranBy);

            assertEquals("200000.00", result.settlement().getTotalExpected().getAmount().toPlainString());
            assertEquals(1, result.shares().size());
            assertEquals("200000.00", findShare(result.shares(), operatorA).getShareAmount().getAmount().toPlainString());
        }

        @Test
        @DisplayName("Vé tháng BUS SINGLE_ROUTE phân bổ trực tiếp về 1 nhà xe")
        void busSingleRoute_directAllocation() {
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = mockTicket(ticketId, "150000", FareMode.BUS, PassScope.SINGLE_ROUTE);

            Trip trip = makeTrip(operatorA, TicketTypeUsed.MONTHLY_PASS, ticketId, null, "20", FareMode.BUS);

            SettlementResult result = Settlement.calculateAndSettle(
                    "2026-06", List.of(trip), Map.of(ticketId, ticket), List.of(),
                    strategy, tolerance, ranBy);

            assertEquals("150000.00", findShare(result.shares(), operatorA).getShareAmount().getAmount().toPlainString());
        }
    }

    // ── Pool 3 ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("3. Pool 3: Phân bổ theo Tỷ lệ (MULTI_ROUTE)")
    class ProportionalMonthlyAllocation {

        @Test
        @DisplayName("Doanh thu chia tỷ lệ: (Số chuyến × GiaMoCua) + (Số km × Gia1km)")
        void multiRoute_proportionalAllocation() {
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = mockTicket(ticketId, "280000", FareMode.BUS, PassScope.MULTI_ROUTE);

            // A: 1 chuyến, 50 km → weight = (1×3000) + (50×450) = 25500
            // B: 1 chuyến, 15 km → weight = (1×3000) + (15×450) = 9750
            Trip tripA = makeTrip(operatorA, TicketTypeUsed.MONTHLY_PASS, ticketId, null, "50", FareMode.BUS);
            Trip tripB = makeTrip(operatorB, TicketTypeUsed.MONTHLY_PASS, ticketId, null, "15", FareMode.BUS);

            FareRule rule = mockFareRule(FareMode.BUS, "3000", "450");

            SettlementResult result = Settlement.calculateAndSettle(
                    "2026-06", List.of(tripA, tripB), Map.of(ticketId, ticket), List.of(rule),
                    strategy, tolerance, ranBy);

            BigDecimal shareA = findShare(result.shares(), operatorA).getShareAmount().getAmount();
            BigDecimal shareB = findShare(result.shares(), operatorB).getShareAmount().getAmount();

            // Tổng bảo toàn
            assertEquals(0, new BigDecimal("280000").compareTo(shareA.add(shareB)));
            // A đi xa hơn → hưởng nhiều hơn
            assertTrue(shareA.compareTo(shareB) > 0);
        }

        @Test
        @DisplayName("Tổng share bằng đúng giá vé gốc (không mất tiền)")
        void proportional_totalShareEqualsTicketPrice() {
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = mockTicket(ticketId, "300000", FareMode.BUS, PassScope.MULTI_ROUTE);

            Trip tripA = makeTrip(operatorA, TicketTypeUsed.MONTHLY_PASS, ticketId, null, "30", FareMode.BUS);
            Trip tripB = makeTrip(operatorB, TicketTypeUsed.MONTHLY_PASS, ticketId, null, "20", FareMode.BUS);

            FareRule rule = mockFareRule(FareMode.BUS, "3000", "450");

            SettlementResult result = Settlement.calculateAndSettle(
                    "2026-06", List.of(tripA, tripB), Map.of(ticketId, ticket), List.of(rule),
                    strategy, tolerance, ranBy);

            BigDecimal total = result.shares().stream()
                    .map(s -> s.getShareAmount().getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            assertEquals(0, new BigDecimal("300000").compareTo(total));
        }
    }

    // ── Reconciliation ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("4. Đối soát (Reconciliation Status)")
    class ReconciliationStatus {

        @Test
        @DisplayName("MATCH khi tổng share bằng đúng totalExpected")
        void match_whenShareEqualsExpected() {
            Trip trip = makeTrip(operatorA, TicketTypeUsed.SINGLE_TRIP, null, "100000", "10", FareMode.BUS);

            SettlementResult result = Settlement.calculateAndSettle(
                    "2026-06", List.of(trip), Map.of(), List.of(),
                    strategy, tolerance, ranBy);

            assertEquals(ReconcileStatus.MATCH, result.settlement().getReconciliationStatus());
        }

        @Test
        @DisplayName("MISMATCH khi độ lệch vượt ngưỡng tolerance")
        void mismatch_whenDiffExceedsTolerance() {
            Settlement s = Settlement.create(
                    "2026-06", Money.of(new BigDecimal("200000")), new BigDecimal("100"), ranBy);

            // diff = 200000 - 199000 = 1000 > tolerance 100 → MISMATCH
            List<CompanyShare> shares = List.of(
                    CompanyShare.of(s.getId(), operatorA,
                            BigDecimal.ZERO, 0,
                            Money.of(new BigDecimal("199000")),
                            Money.of(new BigDecimal("199000")),
                            Money.of(BigDecimal.ZERO),
                            Money.of(BigDecimal.ZERO),
                            Money.of(BigDecimal.ZERO))
            );

            s.reconcile(shares);

            assertEquals(ReconcileStatus.MISMATCH, s.getReconciliationStatus());
        }

        @Test
        @DisplayName("WARNING khi độ lệch nằm trong ngưỡng tolerance (>0 và <=100)")
        void warning_whenDiffWithinTolerance() {
            Settlement s = Settlement.create(
                    "2026-06", Money.of(new BigDecimal("200000")), new BigDecimal("100"), ranBy);

            // diff = 200000 - 199950 = 50 ≤ tolerance 100 → WARNING
            List<CompanyShare> shares = List.of(
                    CompanyShare.of(s.getId(), operatorA,
                            BigDecimal.ZERO, 0,
                            Money.of(new BigDecimal("199950")),
                            Money.of(new BigDecimal("199950")),
                            Money.of(BigDecimal.ZERO),
                            Money.of(BigDecimal.ZERO),
                            Money.of(BigDecimal.ZERO))
            );

            s.reconcile(shares);

            assertEquals(ReconcileStatus.WARNING, s.getReconciliationStatus());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CompanyShare findShare(List<CompanyShare> shares, UUID operatorId) {
        return shares.stream()
                .filter(s -> s.getOperatorId().equals(operatorId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing share for operator " + operatorId));
    }

    private Trip makeTrip(UUID operatorId, TicketTypeUsed type, UUID ticketId,
                          String fare, String distance, FareMode mode) {
        return Trip.from(
                UUID.randomUUID(), null, ticketId, operatorId, null, null, Instant.now(),
                null, null, Instant.now(),
                new BigDecimal(distance),
                fare != null ? new BigDecimal(fare) : null,
                mode, PaymentMethod.TICKET, type, TripStatus.COMPLETED, null
        );
    }

    private Ticket mockTicket(UUID id, String price, FareMode mode, PassScope scope) {
        Ticket t = mock(Ticket.class);
        when(t.getId()).thenReturn(id);
        when(t.getPrice()).thenReturn(Money.of(new BigDecimal(price)));
        when(t.getMode()).thenReturn(mode);
        when(t.getScope()).thenReturn(scope);
        return t;
    }

    private FareRule mockFareRule(FareMode mode, String base, String rate) {
        FareRule r = mock(FareRule.class);
        when(r.getMode()).thenReturn(mode);
        when(r.getBaseFare()).thenReturn(Money.of(new BigDecimal(base)));
        when(r.getRatePerKm()).thenReturn(Money.of(new BigDecimal(rate)));
        return r;
    }
}