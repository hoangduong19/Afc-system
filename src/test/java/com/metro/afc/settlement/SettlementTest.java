package com.metro.afc.settlement;

import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.settlement.domain.CompanyShare;
import com.metro.afc.settlement.domain.Settlement;
import com.metro.afc.settlement.domain.Settlement.SettlementResult;
import com.metro.afc.settlement.domain.enums.settlement.ReconcileStatus;
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

    private final UUID operatorA = UUID.randomUUID();
    private final UUID operatorB = UUID.randomUUID();
    private final UUID ranBy = UUID.randomUUID();
    private final BigDecimal tolerance = new BigDecimal("100");

    @Nested
    @DisplayName("1. Pool 1: Phân bổ Vé Lượt (Single Trip)")
    class SingleTripAllocation {

        @Test
        @DisplayName("Tiền vé lượt chảy trực tiếp về nhà xe vận hành chuyến đó")
        void singleTrips_directAllocation() {
            // Arrange
            Trip tripA = makeTrip(operatorA, TicketTypeUsed.SINGLE_TRIP, null, "125000", "10", FareMode.BUS);
            Trip tripB = makeTrip(operatorB, TicketTypeUsed.SINGLE_TRIP, null, "50000", "5", FareMode.BUS);

            // Act
            SettlementResult result = Settlement.calculateAndSettle(
                    "2026-06", List.of(tripA, tripB), Map.of(), List.of(), tolerance, ranBy);

            // Assert
            assertEquals("175000.00", result.settlement().getTotalExpected().getAmount().toString());

            CompanyShare shareA = findShare(result.shares(), operatorA);
            CompanyShare shareB = findShare(result.shares(), operatorB);

            assertEquals("125000.00", shareA.getShareAmount().getAmount().toString());
            assertEquals("50000.00", shareB.getShareAmount().getAmount().toString());
            assertEquals(ReconcileStatus.MATCH, result.settlement().getReconciliationStatus());
        }
    }

    @Nested
    @DisplayName("2. Pool 2: Phân bổ trực tiếp (METRO / SINGLE_ROUTE)")
    class DirectMonthlyAllocation {

        @Test
        @DisplayName("Vé tháng METRO phân bổ 100% doanh thu về 1 nhà xe")
        void metroMonthly_directAllocation() {
            // Arrange
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = mockTicket(ticketId, "200000", FareMode.METRO, null);

            Trip trip1 = makeTrip(operatorA, TicketTypeUsed.MONTHLY_PASS, ticketId, null, "10", FareMode.METRO);
            Trip trip2 = makeTrip(operatorA, TicketTypeUsed.MONTHLY_PASS, ticketId, null, "15", FareMode.METRO);

            // Act
            SettlementResult result = Settlement.calculateAndSettle(
                    "2026-06", List.of(trip1, trip2), Map.of(ticketId, ticket), List.of(), tolerance, ranBy);

            // Assert
            assertEquals("200000.00", result.settlement().getTotalExpected().getAmount().toString());
            assertEquals(1, result.shares().size());
            assertEquals("200000.00", findShare(result.shares(), operatorA).getShareAmount().getAmount().toString());
        }
    }

    @Nested
    @DisplayName("3. Pool 3: Phân bổ theo Tỷ lệ (MULTI_ROUTE)")
    class ProportionalMonthlyAllocation {

        @Test
        @DisplayName("Doanh thu chia tỷ lệ theo công thức: (Số chuyến * Giá mở cửa) + (Số km * Giá 1km)")
        void multiRoute_proportionalAllocation() {
            // Arrange
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = mockTicket(ticketId, "280000", FareMode.BUS, PassScope.MULTI_ROUTE);

            // Operator A: 10 chuyến, 50 km (Trọng số lớn hơn)
            Trip tripA = makeTrip(operatorA, TicketTypeUsed.MONTHLY_PASS, ticketId, null, "50", FareMode.BUS);
            // Operator B: 5 chuyến, 15 km (Trọng số nhỏ hơn)
            Trip tripB = makeTrip(operatorB, TicketTypeUsed.MONTHLY_PASS, ticketId, null, "15", FareMode.BUS);

            FareRule rule = mockFareRule(FareMode.BUS, "3000", "450");

            // Act
            // Note: In real life, Trip contributions logic groups by operator. We mock trips to simulate grouping.
            // Để operator A có count = 10, ta cần truyền vào 10 object Trip, hoặc vì code hiện tại count dựa theo list.size()
            // Tạm thời đơn giản hóa logic test: A đi 1 chuyến 50km, B đi 1 chuyến 15km
            SettlementResult result = Settlement.calculateAndSettle(
                    "2026-06", List.of(tripA, tripB), Map.of(ticketId, ticket), List.of(rule), tolerance, ranBy);

            // Assert
            BigDecimal shareA = findShare(result.shares(), operatorA).getShareAmount().getAmount();
            BigDecimal shareB = findShare(result.shares(), operatorB).getShareAmount().getAmount();

            // Tổng tiền không đổi
            assertEquals(0, new BigDecimal("280000").compareTo(shareA.add(shareB)));
            // A đi xa hơn nên hưởng nhiều tiền hơn
            assertTrue(shareA.compareTo(shareB) > 0);
        }
    }

    @Nested
    @DisplayName("4. Đối soát (Reconciliation Status)")
    class ReconciliationLimits {

        @Test
        @DisplayName("Hệ thống phát hiện MISMATCH nếu độ lệch vượt quá ngưỡng cho phép")
        void triggersMismatch_whenOverTolerance() {
            Settlement s = Settlement.create("2026-06", Money.of(new BigDecimal("200000")), new BigDecimal("100"), ranBy);

            // shareAmount = 199,000 → diff = 1,000 > tolerance 100 → MISMATCH
            List<CompanyShare> shares = List.of(
                    CompanyShare.of(
                            s.getId(), operatorA,
                            BigDecimal.ZERO, 0,
                            Money.of(new BigDecimal("199000")),  // expectedRevenue
                            Money.of(new BigDecimal("199000")),  // shareAmount
                            Money.of(BigDecimal.ZERO),           // directShare      ← thêm
                            Money.of(BigDecimal.ZERO),           // proportionalShare ← thêm
                            Money.of(BigDecimal.ZERO)            // roundingAdjustment
                    )
            );

            s.reconcile(shares);

            assertEquals(ReconcileStatus.MISMATCH, s.getReconciliationStatus());
        }
    }

    // ── Helper Methods ──────────────────────────────────────────────────

    private CompanyShare findShare(List<CompanyShare> shares, UUID operatorId) {
        return shares.stream()
                .filter(s -> s.getOperatorId().equals(operatorId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing share for operator " + operatorId));
    }

    private Trip makeTrip(UUID operatorId, TicketTypeUsed type, UUID ticketId, String fare, String distance, FareMode mode) {
        return Trip.from(
                UUID.randomUUID(), null, ticketId, operatorId, null, null, Instant.now(),
                null, null, Instant.now(), new BigDecimal(distance),
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