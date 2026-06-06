package com.metro.afc.fare.domain.model;

import com.metro.afc.fare.domain.event.FareRuleCreatedEvent;
import com.metro.afc.fare.domain.event.FareRuleDisabledEvent;
import com.metro.afc.fare.domain.event.FareRuleUpdatedEvent;
import com.metro.afc.fare.domain.model.enums.FareMode;
import com.metro.afc.fare.domain.model.enums.FareStatus;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
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

    @Column(name = "base_fare", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseFare;

    @Column(name = "rate_per_km", nullable = false, precision = 15, scale = 2)
    private BigDecimal ratePerKm;

    @Column(name = "min_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxPrice;

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

    public static FareRule create(String code, FareMode mode,
                                  BigDecimal baseFare, BigDecimal ratePerKm,
                                  BigDecimal minPrice, BigDecimal maxPrice,
                                  LocalDate effectiveFrom, LocalDate effectiveTo,
                                  UUID createdBy) {
        FareRule rule      = new FareRule();
        rule.id            = UUID.randomUUID();
        rule.code          = code.trim().toUpperCase();
        rule.mode          = mode;
        rule.baseFare      = baseFare;
        rule.ratePerKm     = ratePerKm;
        rule.minPrice      = minPrice;
        rule.maxPrice      = maxPrice;
        rule.effectiveFrom = effectiveFrom;
        rule.effectiveTo   = effectiveTo;
        rule.status        = FareStatus.ACTIVE;
        rule.version       = 1;
        rule.createdBy     = createdBy;
        rule.registerEvent(new FareRuleCreatedEvent(rule.id, rule.snapshot(), createdBy));
        return rule;
    }

    public void closeVersion(LocalDate newVersionEffectiveFrom) {
        this.effectiveTo = newVersionEffectiveFrom.minusDays(1);
        this.status      = FareStatus.INACTIVE;
    }

    public FareRule newVersion(BigDecimal baseFare, BigDecimal ratePerKm,
                               BigDecimal minPrice, BigDecimal maxPrice,
                               LocalDate effectiveFrom, LocalDate effectiveTo, String reason,
                               UUID createdBy) {
        String oldSnapshot = this.snapshot();

        FareRule next      = new FareRule();
        next.id            = UUID.randomUUID();
        next.code          = this.code;
        next.mode          = this.mode;
        next.baseFare      = baseFare;
        next.ratePerKm     = ratePerKm;
        next.minPrice      = minPrice;
        next.maxPrice      = maxPrice;
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
        if (this.status == FareStatus.INACTIVE) {
            throw new BusinessRuleException(
                    ErrorCode.FARE_RULE_INACTIVE,
                    "Fare rule đã ở trạng thái INACTIVE"
            );
        }
        String oldSnapshot = snapshot();
        this.status = FareStatus.INACTIVE;
        this.registerEvent(new FareRuleDisabledEvent(this.id, oldSnapshot, reason, disabledBy));
    }

    public boolean isActive() {
        return this.status == FareStatus.ACTIVE;
    }

    public BigDecimal calculateFare(BigDecimal distanceKm) {
        BigDecimal fare = baseFare.add(ratePerKm.multiply(distanceKm));
        if (fare.compareTo(minPrice) < 0) return minPrice;
        if (fare.compareTo(maxPrice) > 0) return maxPrice;
        return fare;
    }

    private String snapshot() {
        return String.format(
                "{\"id\":\"%s\",\"code\":\"%s\",\"mode\":\"%s\"," +
                        "\"baseFare\":%s,\"ratePerKm\":%s," +
                        "\"minPrice\":%s,\"maxPrice\":%s," +
                        "\"version\":%d,\"status\":\"%s\"," +
                        "\"effectiveFrom\":\"%s\",\"effectiveTo\":%s}",
                id, code, mode, baseFare, ratePerKm,
                minPrice, maxPrice, version, status,
                effectiveFrom,
                effectiveTo != null ? "\"" + effectiveTo + "\"" : "null"
        );
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}