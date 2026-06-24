package com.metro.afc.settlement.domain;

import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.settlement.application.dto.settlement.v2.TicketRevenueData;
import com.metro.afc.settlement.application.dto.settlement.v2.TripContribution;
import com.metro.afc.settlement.domain.enums.settlement.ReconcileStatus;
import com.metro.afc.settlement.domain.enums.settlement.SettlementStatus;
import com.metro.afc.settlement.domain.events.settlement.SettlementConfirmedDomainEvent;
import com.metro.afc.settlement.domain.settlementAllocation.AllocationStrategy;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.ticket.domain.Ticket;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    @Column(name = "formula_code", length = 30)
    private String formulaCode;

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "total_expected", precision = 15, scale = 2))
    private Money totalExpected;

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "total_actual", precision = 15, scale = 2))
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

    public record SettlementResult(Settlement settlement, List<CompanyShare> shares) {}

    // ── Factory ──────────────────────────────────────────────────────────────

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

    public static SettlementResult calculateAndSettle(
            String period,
            List<Trip> trips,
            Map<UUID, Ticket> ticketMap,
            List<Ticket> soldTickets,
            List<FareRule> activeRules,
            AllocationStrategy strategy,
            BigDecimal toleranceThreshold,
            UUID ranBy) {

        Settlement s = new Settlement();
        s.id                 = UUID.randomUUID();
        s.period             = period;
        s.status             = SettlementStatus.DRAFT;
        s.formulaCode        = strategy.formulaCode();
        s.toleranceThreshold = toleranceThreshold;
        s.ranBy              = ranBy;

        // 1. Data prep — thuộc aggregate
        Map<UUID, Money> singleTripShares  = s.calculateSingleTripShares(trips);
        List<TicketRevenueData> monthlyData = s.aggregateMonthlyTicketData(trips, ticketMap);

        s.totalExpected = soldTickets.stream()
                .map(Ticket::getPrice)
                .reduce(Money.of(BigDecimal.ZERO), Money::add);

        Map<UUID, BigDecimal> operatorTotalKm   = s.calculateOperatorTotalKm(trips);
        Map<UUID, Integer>    operatorTripCount  = s.calculateOperatorTripCount(trips);

        // 2. Delegate allocation — aggregate không biết công thức
        Map<UUID, Money> totalMap = strategy.allocate(singleTripShares, monthlyData, activeRules);

        // 3. Build CompanyShare
        List<CompanyShare> shares = totalMap.entrySet().stream()
                .map(e -> CompanyShare.of(
                        s.id,
                        e.getKey(),
                        operatorTotalKm.getOrDefault(e.getKey(), BigDecimal.ZERO),
                        operatorTripCount.getOrDefault(e.getKey(), 0),
                        e.getValue(),
                        e.getValue(),
                        Money.of(BigDecimal.ZERO),
                        Money.of(BigDecimal.ZERO),
                        Money.of(BigDecimal.ZERO)
                ))
                .toList();

        // 4. Reconcile
        s.reconcile(shares);

        return new SettlementResult(s, shares);
    }

    // ── Private data prep — vẫn thuộc aggregate ──────────────────────────────

    private Map<UUID, Money> calculateSingleTripShares(List<Trip> trips) {
        return trips.stream()
                .filter(t -> t.getTicketTypeUsed() == TicketTypeUsed.SINGLE_TRIP
                        && t.getOperatorId() != null
                        && t.getFareAmount() != null)
                .collect(groupingBy(
                        Trip::getOperatorId,
                        Collectors.reducing(Money.of(BigDecimal.ZERO),
                                t -> Money.of(t.getFareAmount()), Money::add)
                ));
    }

    private List<TicketRevenueData> aggregateMonthlyTicketData(
            List<Trip> trips, Map<UUID, Ticket> ticketMap) {
        return trips.stream()
                .filter(t -> t.getTicketTypeUsed() == TicketTypeUsed.MONTHLY_PASS
                        && t.getTicketId() != null)
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
                                    op.getValue().stream()
                                            .map(t -> t.getDistanceKm() != null
                                                    ? t.getDistanceKm() : BigDecimal.ZERO)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            ))
                            .toList();
                    return new TicketRevenueData(
                            e.getKey(), ticket.getPrice(),
                            ticket.getMode(), ticket.getScope(), contribs);
                })
                .toList();
    }

    private Map<UUID, BigDecimal> calculateOperatorTotalKm(List<Trip> trips) {
        return trips.stream()
                .filter(t -> t.getOperatorId() != null && t.getDistanceKm() != null)
                .collect(groupingBy(Trip::getOperatorId,
                        Collectors.reducing(BigDecimal.ZERO, Trip::getDistanceKm, BigDecimal::add)));
    }

    private Map<UUID, Integer> calculateOperatorTripCount(List<Trip> trips) {
        return trips.stream()
                .filter(t -> t.getOperatorId() != null)
                .collect(groupingBy(Trip::getOperatorId, Collectors.summingInt(t -> 1)));
    }

    // ── Domain behavior ───────────────────────────────────────────────────────

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