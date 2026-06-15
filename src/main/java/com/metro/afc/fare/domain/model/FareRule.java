package com.metro.afc.fare.domain.model;

import com.metro.afc.fare.domain.event.fareRule.FareRuleCreatedEvent;
import com.metro.afc.fare.domain.event.fareRule.FareRuleDisabledEvent;
import com.metro.afc.fare.domain.event.fareRule.FareRuleUpdatedEvent;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRule.FareStatus;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.ticket.domain.enums.PassScope;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fare_rules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FareRule extends AbstractAggregateRoot<FareRule> {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FareMode mode;

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "base_fare", nullable = false, precision = 15, scale = 2))
    private Money baseFare;

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "rate_per_km", nullable = false, precision = 15, scale = 2))
    private Money ratePerKm;

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "min_price", nullable = false, precision = 15, scale = 2))
    private Money minPrice;

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "max_price", nullable = false, precision = 15, scale = 2))
    private Money maxPrice;

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "monthly_single_price", precision = 15, scale = 2))
    private Money monthlySinglePrice;   // BUS SINGLE_ROUTE, METRO, ANY

    @Embedded
    @AttributeOverride(name = "amount",
            column = @Column(name = "monthly_multi_price", precision = 15, scale = 2))
    private Money monthlyMultiPrice;    // BUS MULTI_ROUTE only, null cho các mode khác

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FareStatus status;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "created_by", nullable = false, columnDefinition = "uuid")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ── Factory ──────────────────────────────────────────────────

    public static FareRule create(String code, FareMode mode,
                                  BigDecimal baseFare, BigDecimal ratePerKm,
                                  BigDecimal minPrice, BigDecimal maxPrice,
                                  BigDecimal monthlySinglePrice, BigDecimal monthlyMultiPrice,
                                  LocalDate effectiveFrom, LocalDate effectiveTo,
                                  UUID createdBy) {
        validatePriceRange(minPrice, maxPrice);
        FareRule rule      = new FareRule();
        rule.id            = UUID.randomUUID();
        rule.code          = code.trim().toUpperCase();
        rule.mode          = mode;
        rule.baseFare      = Money.of(baseFare);
        rule.ratePerKm     = Money.of(ratePerKm);
        rule.minPrice      = Money.of(minPrice);
        rule.maxPrice      = Money.of(maxPrice);
        rule.monthlySinglePrice = Money.ofNullable(monthlySinglePrice);
        rule.monthlyMultiPrice  = Money.ofNullable(monthlyMultiPrice);
        rule.effectiveFrom = effectiveFrom;
        rule.effectiveTo   = effectiveTo;
        rule.status        = FareStatus.ACTIVE;
        rule.version       = 1;
        rule.createdBy     = createdBy;
        rule.registerEvent(new FareRuleCreatedEvent(rule.id, rule.snapshot(), createdBy));
        return rule;
    }

    // ── Domain behavior ──────────────────────────────────────────

    public void closeVersion(LocalDate newVersionEffectiveFrom) {
        this.effectiveTo = newVersionEffectiveFrom.minusDays(1);
        this.status      = FareStatus.INACTIVE;
    }

    public FareRule newVersion(BigDecimal baseFare, BigDecimal ratePerKm,
                               BigDecimal minPrice, BigDecimal maxPrice,
                               BigDecimal monthlySinglePrice, BigDecimal monthlyMultiPrice,
                               LocalDate effectiveFrom, LocalDate effectiveTo, String reason,
                               UUID createdBy) {
        validatePriceRange(minPrice, maxPrice);
        String oldSnapshot = this.snapshot();

        FareRule next      = new FareRule();
        next.id            = UUID.randomUUID();
        next.code          = this.code;
        next.mode          = this.mode;
        next.baseFare      = Money.of(baseFare);
        next.ratePerKm     = Money.of(ratePerKm);
        next.minPrice      = Money.of(minPrice);
        next.maxPrice      = Money.of(maxPrice);
        next.monthlySinglePrice = Money.ofNullable(monthlySinglePrice);
        next.monthlyMultiPrice  = Money.ofNullable(monthlyMultiPrice);
        next.effectiveFrom = effectiveFrom;
        next.effectiveTo   = effectiveTo;
        next.status        = FareStatus.ACTIVE;
        next.version       = this.version + 1;
        next.createdBy     = createdBy;
        next.registerEvent(new FareRuleUpdatedEvent(
                next.id, oldSnapshot, next.snapshot(), reason, createdBy
        ));
        return next;
    }

    public void disable(String reason, UUID disabledBy) {
        if (this.status == FareStatus.INACTIVE)
            throw new BusinessRuleException(
                    ErrorCode.FARE_RULE_INACTIVE,
                    "Fare rule is already inactive"
            );
        String oldSnapshot = snapshot();
        this.status = FareStatus.INACTIVE;
        this.registerEvent(new FareRuleDisabledEvent(this.id, oldSnapshot, reason, disabledBy));
    }

    public boolean isActive() { return this.status == FareStatus.ACTIVE; }

    public Money calculateFare(BigDecimal distanceKm) {
        Money fare = baseFare.add(ratePerKm.multiply(distanceKm));
        if (fare.isLessThan(minPrice))    return minPrice;
        if (fare.isGreaterThan(maxPrice)) return maxPrice;
        return fare;
    }

    public Money calculateMonthlyPassPrice(PassScope scope, int durationDays) {
        BigDecimal basePrice = resolveBaseMonthlyPrice(scope);

        if (durationDays != 30) {
            basePrice = basePrice
                    .multiply(BigDecimal.valueOf(durationDays))
                    .divide(BigDecimal.valueOf(30), 0, RoundingMode.HALF_UP);
        }

        return Money.of(basePrice);
    }

    private BigDecimal resolveBaseMonthlyPrice(PassScope scope) {
        if (this.mode == FareMode.BUS) {
            if (scope == null)
                throw new BusinessRuleException(
                        ErrorCode.INVALID_PASS_SCOPE,
                        "BUS monthly pass requires scope");
            return switch (scope) {
                case SINGLE_ROUTE -> {
                    if (monthlySinglePrice == null)
                        throw new BusinessRuleException(
                                ErrorCode.FARE_RULE_MISCONFIGURED,
                                "monthlySinglePrice not configured for this rule");
                    yield monthlySinglePrice.getAmount();
                }
                case MULTI_ROUTE -> {
                    if (monthlyMultiPrice == null)
                        throw new BusinessRuleException(
                                ErrorCode.FARE_RULE_MISCONFIGURED,
                                "monthlyMultiPrice not configured for this rule");
                    yield monthlyMultiPrice.getAmount();
                }
            };
        }
        // METRO, ANY — scope phải null
        if (scope != null)
            throw new BusinessRuleException(
                    ErrorCode.INVALID_PASS_SCOPE,
                    "Only BUS monthly pass can have scope");
        if (monthlySinglePrice == null)
            throw new BusinessRuleException(
                    ErrorCode.FARE_RULE_MISCONFIGURED,
                    "monthlySinglePrice not configured for this rule");
        return monthlySinglePrice.getAmount();
    }

    private static void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice.compareTo(maxPrice) > 0)
            throw new IllegalArgumentException("minPrice must be <= maxPrice");
    }

    private String snapshot() {
        return String.format(
                "{\"id\":\"%s\",\"code\":\"%s\",\"mode\":\"%s\"," +
                        "\"baseFare\":%s,\"ratePerKm\":%s," +
                        "\"minPrice\":%s,\"maxPrice\":%s," +
                        "\"monthlySinglePrice\":%s,\"monthlyMultiPrice\":%s," +
                        "\"version\":%d,\"status\":\"%s\"," +
                        "\"effectiveFrom\":\"%s\",\"effectiveTo\":%s}",
                id, code, mode,
                baseFare.getAmount(), ratePerKm.getAmount(),
                minPrice.getAmount(), maxPrice.getAmount(),
                monthlySinglePrice != null ? monthlySinglePrice.getAmount() : "null",
                monthlyMultiPrice  != null ? monthlyMultiPrice.getAmount()  : "null",
                version, status, effectiveFrom,
                effectiveTo != null ? "\"" + effectiveTo + "\"" : "null"
        );
    }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }
}