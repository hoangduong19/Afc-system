package com.metro.afc.settlement.domain;

import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.settlement.application.dto.settlement.v2.TicketRevenueData;
import com.metro.afc.settlement.application.dto.settlement.v2.TripContribution;
import com.metro.afc.settlement.domain.enums.settlement.ReconcileStatus;
import com.metro.afc.settlement.domain.enums.settlement.SettlementStatus;
import com.metro.afc.settlement.domain.events.settlement.SettlementConfirmedDomainEvent;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.enums.PassScope;
import com.metro.afc.trip.domain.Trip;
import com.metro.afc.trip.domain.enums.trip.TicketTypeUsed;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends AbstractAggregateRoot<Settlement> {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 7, unique = true)
    private String period;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "total_expected",
                    precision = 15, scale = 2))
    private Money totalExpected;

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "total_actual",
                    precision = 15, scale = 2))
    private Money totalActual;

    @Column(name = "diff_amount", precision = 15, scale = 2)
    private BigDecimal diffAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "reconciliation_status", length = 20)
    private ReconcileStatus reconciliationStatus;

    @Column(name = "tolerance_threshold", precision = 15, scale = 2)
    private BigDecimal toleranceThreshold;

    @Column(name = "ran_by", nullable = false, columnDefinition = "uuid")
    private UUID ranBy;

    @Column(name = "ran_at", nullable = false, updatable = false)
    private Instant ranAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    // ── Factory ──────────────────────────────────────────────────

    public static Settlement create(String period, Money totalExpected,
                                    BigDecimal toleranceThreshold,
                                    UUID ranBy) {
        Settlement s         = new Settlement();
        s.id                 = UUID.randomUUID();
        s.period             = period;
        s.status             = SettlementStatus.DRAFT;
        s.totalExpected      = totalExpected;
        s.toleranceThreshold = toleranceThreshold;
        s.ranBy              = ranBy;
        return s;
    }

    private record FareParams(BigDecimal openingPrice, BigDecimal pricePerKm) {}

    // Payload record to return both the aggregate and its generated shares to the service
    public record SettlementResult(Settlement settlement, List<CompanyShare> shares) {}

    // ── Factory & Core Domain Logic ────────────────────────────────────

    public static SettlementResult calculateAndSettle(
            String period,
            List<Trip> trips,
            Map<UUID, Ticket> ticketMap,
            List<FareRule> activeRules,
            BigDecimal toleranceThreshold,
            UUID ranBy) {

        Settlement s = new Settlement();
        s.id = UUID.randomUUID();
        s.period = period;
        s.status = SettlementStatus.DRAFT;
        s.toleranceThreshold = toleranceThreshold;
        s.ranBy = ranBy;

        // 1. Calculate Expected Revenues
        Map<UUID, Money> singleTripShares = s.calculateSingleTripShares(trips);
        Money singleTripTotal = singleTripShares.values().stream().reduce(Money.of(BigDecimal.ZERO), Money::add);

        List<TicketRevenueData> monthlyData = s.aggregateMonthlyTicketData(trips, ticketMap);
        Money monthlyTotal = monthlyData.stream().map(TicketRevenueData::ticketPrice).reduce(Money.of(BigDecimal.ZERO), Money::add);

        s.totalExpected = singleTripTotal.add(monthlyTotal);

        // 2. Perform Allocations
        Map<FareMode, FareParams> fareParams = s.extractFareParams(activeRules);
        Map<UUID, BigDecimal> operatorTotalKm = s.calculateOperatorTotalKm(trips);
        Map<UUID, Integer> operatorTripCount = s.calculateOperatorTripCount(trips);

        List<CompanyShare> generatedShares = s.allocateShares(
                singleTripShares, monthlyData, fareParams, operatorTotalKm, operatorTripCount);

        // 3. Reconcile
        s.reconcile(generatedShares);

        return new SettlementResult(s, generatedShares);
    }

    // ── Private Internal Grouping Behaviors ────────────────────────────

    private Map<UUID, Money> calculateSingleTripShares(List<Trip> trips) {
        return trips.stream()
                .filter(t -> t.getTicketTypeUsed() == TicketTypeUsed.SINGLE_TRIP && t.getOperatorId() != null && t.getFareAmount() != null)
                .collect(groupingBy(
                        Trip::getOperatorId,
                        Collectors.reducing(Money.of(BigDecimal.ZERO), t -> Money.of(t.getFareAmount()), Money::add)
                ));
    }

    private List<TicketRevenueData> aggregateMonthlyTicketData(List<Trip> trips, Map<UUID, Ticket> ticketMap) {
        return trips.stream()
                .filter(t -> t.getTicketTypeUsed() == TicketTypeUsed.MONTHLY_PASS && t.getTicketId() != null)
                .collect(groupingBy(Trip::getTicketId))
                .entrySet().stream()
                .filter(e -> ticketMap.containsKey(e.getKey()))
                .map(e -> {
                    Ticket ticket = ticketMap.get(e.getKey());
                    List<TripContribution> contribs = e.getValue().stream()
                            .filter(t -> t.getOperatorId() != null)
                            .collect(groupingBy(Trip::getOperatorId))
                            .entrySet().stream()
                            .map(op -> new TripContribution(
                                    op.getKey(),
                                    op.getValue().get(0).getTransportMode(),
                                    op.getValue().size(),
                                    op.getValue().stream().map(t -> t.getDistanceKm() != null ? t.getDistanceKm() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add)
                            ))
                            .toList();
                    return new TicketRevenueData(e.getKey(), ticket.getPrice(), ticket.getMode(), ticket.getScope(), contribs);
                })
                .toList();
    }

    private Map<FareMode, FareParams> extractFareParams(List<FareRule> rules) {
        Map<FareMode, FareParams> params = new EnumMap<>(FareMode.class);
        rules.forEach(r -> params.put(r.getMode(), new FareParams(r.getBaseFare().getAmount(), r.getRatePerKm().getAmount())));
        params.putIfAbsent(FareMode.METRO, new FareParams(new BigDecimal("8000"), new BigDecimal("850")));
        params.putIfAbsent(FareMode.BUS, new FareParams(new BigDecimal("3000"), new BigDecimal("450")));
        return params;
    }

    private Map<UUID, BigDecimal> calculateOperatorTotalKm(List<Trip> trips) {
        return trips.stream().filter(t -> t.getOperatorId() != null && t.getDistanceKm() != null)
                .collect(groupingBy(Trip::getOperatorId, Collectors.reducing(BigDecimal.ZERO, Trip::getDistanceKm, BigDecimal::add)));
    }

    private Map<UUID, Integer> calculateOperatorTripCount(List<Trip> trips) {
        return trips.stream().filter(t -> t.getOperatorId() != null)
                .collect(groupingBy(Trip::getOperatorId, Collectors.summingInt(t -> 1)));
    }

    // ── Domain behavior ──────────────────────────────────────────

    /**
     * Pool 1: vé lượt → direct (singleTripShares đã group sẵn)
     * Pool 2: METRO hoặc BUS SINGLE_ROUTE → direct 100%
     * Pool 3: BUS MULTI_ROUTE hoặc ANY (multimodal) → proportional theo QĐ 3316 mục 3.4
     *
     * @param singleTripShares  Map<operatorId, totalFare> từ vé lượt
     * @param monthlyTickets    danh sách vé tháng cần phân bổ
     * @param fareParams        GiaMoCua + Gia1km theo FareMode
     * @param operatorTotalKm   tổng km theo operator (tất cả trips) — cho CompanyShare report
     * @param operatorTripCount tổng trips theo operator — cho CompanyShare report
     */
    // ── Domain: phân bổ doanh thu theo QĐ 3316/2025 mục 3.4 ─────────
    //
    //  Pool 1 — vé lượt             : direct (singleTripShares đã group sẵn)
    //  Pool 2 — METRO / SINGLE_ROUTE: direct 100% về 1 operator
    //  Pool 3 — MULTI_ROUTE / ANY   : proportional
    //           DTi = (ai×GiaMoCua_i + bi×Gia1km_i) / Σ(...) × GiaVe

    public List<CompanyShare> allocateShares(
            Map<UUID, Money> singleTripShares,
            List<TicketRevenueData> monthlyTickets,
            Map<FareMode, FareParams> fareParams,
            Map<UUID, BigDecimal> operatorTotalKm,
            Map<UUID, Integer> operatorTripCount) {

        // Pool 1 + Pool 2 → direct
        Map<UUID, Money> directMap = new HashMap<>(singleTripShares);
        // Pool 3 → proportional
        Map<UUID, Money> proportionalMap = new HashMap<>();

        for (TicketRevenueData ticket : monthlyTickets) {
            if (isSingleRoute(ticket)) {
                allocateDirect(ticket, directMap);
            } else {
                allocateProportional(ticket, fareParams, proportionalMap);
            }
        }
        Map<UUID, Money> totalMap = new HashMap<>(directMap);
        proportionalMap.forEach((k, v) -> totalMap.merge(k, v, Money::add));

        Set<UUID> allOperators = totalMap.keySet();
        return allOperators.stream()
                .map(opId -> CompanyShare.of(
                        this.id,
                        opId,
                        operatorTotalKm.getOrDefault(opId, BigDecimal.ZERO),
                        operatorTripCount.getOrDefault(opId, 0),
                        totalMap.get(opId),                                        // expectedRevenue
                        totalMap.get(opId),                                        // shareAmount
                        directMap.getOrDefault(opId, Money.of(BigDecimal.ZERO)),
                        proportionalMap.getOrDefault(opId, Money.of(BigDecimal.ZERO)),
                        Money.of(BigDecimal.ZERO)
                ))
                .toList();
    }

    // METRO (scope null) hoặc BUS SINGLE_ROUTE → direct
    private boolean isSingleRoute(TicketRevenueData ticket) {
        return ticket.mode() == FareMode.METRO
                || ticket.scope() == PassScope.SINGLE_ROUTE;
    }

    // Pool 2: 100% về operator duy nhất
    private void allocateDirect(TicketRevenueData ticket,
                                Map<UUID, Money> result) {
        if (ticket.contributions().isEmpty()) return;
        UUID operatorId = ticket.contributions().get(0).operatorId();
        result.merge(operatorId, ticket.ticketPrice(), Money::add);
    }

    // Pool 3: DTi = (ai×GiaMoCua_i + bi×Gia1km_i) / Σ(...) × GiaVe
    private void allocateProportional(TicketRevenueData ticket,
                                      Map<FareMode, FareParams> fareParams,
                                      Map<UUID, Money> result) {
        Map<UUID, BigDecimal> weights = new LinkedHashMap<>();
        for (TripContribution tc : ticket.contributions()) {
            FareParams p = fareParams.get(tc.mode());

            if (p == null) {
                throw new BusinessRuleException(ErrorCode.FARE_RULE_MISCONFIGURED);
            }
            BigDecimal weight = p.openingPrice()
                    .multiply(BigDecimal.valueOf(tc.tripCount()))
                    .add(p.pricePerKm().multiply(tc.totalKm()));
            weights.merge(tc.operatorId(), weight, BigDecimal::add);
        }

        BigDecimal totalWeight = weights.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) return;

        weights.forEach((opId, w) -> {
            BigDecimal ratio = w.divide(totalWeight, 10, RoundingMode.HALF_UP);
            Money share = ticket.ticketPrice().multiply(ratio);
            result.merge(opId, share, Money::add);
        });
    }

    public void reconcile(List<CompanyShare> shares) {
        Money totalActual = Money.of(shares.stream()
                .map(s -> s.getShareAmount().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        Money totalRounding = Money.of(shares.stream()
                .map(s -> s.getRoundingAdjustment().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        BigDecimal diff = this.totalExpected.getAmount()
                .subtract(totalActual.getAmount())
                .subtract(totalRounding.getAmount())
                .setScale(2, RoundingMode.HALF_UP);

        ReconcileStatus status;
        if (diff.abs().compareTo(BigDecimal.ZERO) == 0)
            status = ReconcileStatus.MATCH;
        else if (diff.abs().compareTo(this.toleranceThreshold) <= 0)
            status = ReconcileStatus.WARNING;
        else
            status = ReconcileStatus.MISMATCH;

        this.totalActual          = totalActual;
        this.diffAmount           = diff;
        this.reconciliationStatus = status;
    }

    public void confirm() {
        this.status      = SettlementStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
        registerEvent(new SettlementConfirmedDomainEvent(this.id));
    }

    @PrePersist
    protected void onCreate() { ranAt = Instant.now(); }
}