package com.metro.afc.settlement.domain;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.settlement.application.dto.settlement.v2.FareParams;
import com.metro.afc.settlement.application.dto.settlement.v2.TicketRevenueData;
import com.metro.afc.settlement.application.dto.settlement.v2.TripContribution;
import com.metro.afc.settlement.domain.enums.settlement.ReconcileStatus;
import com.metro.afc.settlement.domain.enums.settlement.SettlementStatus;
import com.metro.afc.settlement.domain.events.settlement.SettlementConfirmedDomainEvent;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.ticket.domain.enums.PassScope;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

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

        Map<UUID, Money> revenueMap = new HashMap<>(singleTripShares);

        for (TicketRevenueData ticket : monthlyTickets) {
            if (isSingleRoute(ticket)) {
                allocateDirect(ticket, revenueMap);
            } else {
                allocateProportional(ticket, fareParams, revenueMap);
            }
        }

        return revenueMap.entrySet().stream()
                .map(e -> CompanyShare.of(
                        this.id,
                        e.getKey(),
                        operatorTotalKm.getOrDefault(e.getKey(), BigDecimal.ZERO),
                        operatorTripCount.getOrDefault(e.getKey(), 0),
                        e.getValue(),
                        e.getValue(),
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
            FareParams p = fareParams.getOrDefault(tc.mode(), FareParams.BUS_DEFAULT);
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