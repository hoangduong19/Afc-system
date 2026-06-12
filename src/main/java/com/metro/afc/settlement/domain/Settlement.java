package com.metro.afc.settlement.domain;

import com.metro.afc.settlement.application.dto.settlement.OperatorTripData;
import com.metro.afc.settlement.domain.enums.settlement.ReconcileStatus;
import com.metro.afc.settlement.domain.enums.settlement.SettlementStatus;
import com.metro.afc.settlement.domain.events.settlement.SettlementConfirmedDomainEvent;
import com.metro.afc.shared.domain.valueobject.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public List<CompanyShare> allocateShares(
            List<OperatorTripData> operatorData,
            BigDecimal totalSystemKm) {

        List<CompanyShare> shares = new ArrayList<>();

        for (OperatorTripData data : operatorData) {
            BigDecimal kmRatio = totalSystemKm
                    .compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : data.totalKm().divide(
                    totalSystemKm, 6, RoundingMode.HALF_UP);

            Money expectedRevenue = this.totalExpected.multiply(kmRatio);

            Money shareAmount = Money.of(
                    kmRatio.multiply(this.totalExpected.getAmount())
                            .setScale(2, RoundingMode.DOWN)
            );

            Money roundingAdj = Money.of(
                    expectedRevenue.getAmount()
                            .subtract(shareAmount.getAmount())
            );

            shares.add(CompanyShare.of(
                    this.id, data.operatorId(),
                    data.totalKm(), data.tripCount(),
                    expectedRevenue, shareAmount, roundingAdj
            ));
        }

        return shares;
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