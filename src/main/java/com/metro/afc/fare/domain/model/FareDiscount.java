package com.metro.afc.fare.domain.model;

import com.metro.afc.fare.domain.event.fareRuleDiscount.FareDiscountCreatedEvent;
import com.metro.afc.fare.domain.event.fareRuleDiscount.FareDiscountDisabledEvent;
import com.metro.afc.fare.domain.event.fareRuleDiscount.FareDiscountUpdatedEvent;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.DiscountStatus;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.DiscountType;
import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.PassengerType;
import com.metro.afc.shared.domain.valueobject.DiscountValue;
import com.metro.afc.shared.domain.valueobject.Money;
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
@Table(name = "fare_discounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FareDiscount extends AbstractAggregateRoot<FareDiscount> {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_type", nullable = false, length = 50)
    private PassengerType passengerType;

    @Embedded
    private DiscountValue discountValue;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountStatus status;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "created_by", nullable = false, columnDefinition = "uuid")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static FareDiscount create(PassengerType passengerType,
                                      DiscountType discountType,
                                      BigDecimal value,
                                      LocalDate effectiveFrom,
                                      LocalDate effectiveTo,
                                      UUID createdBy) {
        FareDiscount d  = new FareDiscount();
        d.id            = UUID.randomUUID();
        d.passengerType = passengerType;
        d.discountValue = new DiscountValue(discountType, value);
        d.effectiveFrom = effectiveFrom;
        d.effectiveTo   = effectiveTo;
        d.status        = DiscountStatus.ACTIVE;
        d.version       = 1;
        d.createdBy     = createdBy;
        d.createdAt     = Instant.now();
        d.registerEvent(new FareDiscountCreatedEvent(d.id, d.snapshot(), createdBy));
        return d;
    }

    public void closeVersion(LocalDate newVersionEffectiveFrom) {
        this.effectiveTo = newVersionEffectiveFrom.minusDays(1);
        this.status      = DiscountStatus.INACTIVE;
    }

    public FareDiscount newVersion(DiscountType discountType, BigDecimal value,
                                   LocalDate effectiveFrom, LocalDate effectiveTo,
                                   UUID createdBy) {
        String oldSnapshot = this.snapshot();

        FareDiscount next  = new FareDiscount();
        next.id            = UUID.randomUUID();
        next.passengerType = this.passengerType;
        next.discountValue = new DiscountValue(discountType, value);
        next.effectiveFrom = effectiveFrom;
        next.effectiveTo   = effectiveTo;
        next.status        = DiscountStatus.ACTIVE;
        next.version       = this.version + 1;
        next.createdBy     = createdBy;
        next.registerEvent(new FareDiscountUpdatedEvent(
                next.id, oldSnapshot, next.snapshot(), createdBy
        ));
        return next;
    }

    public void disable(UUID disabledBy) {
        if (this.status == DiscountStatus.INACTIVE)
            throw new BusinessRuleException(
                    ErrorCode.FARE_DISCOUNT_INACTIVE,
                    "Fare discount is already inactive"
            );
        String old  = snapshot();
        this.status = DiscountStatus.INACTIVE;
        this.registerEvent(new FareDiscountDisabledEvent(id, old, disabledBy));
    }

    public Money applyTo(Money originalFare) {
        return discountValue.apply(originalFare);
    }

    public boolean isActive() { return status == DiscountStatus.ACTIVE; }

    private String snapshot() {
        return String.format(
                "{\"id\":\"%s\",\"passengerType\":\"%s\",\"discountType\":\"%s\"," +
                        "\"discountValue\":%s,\"effectiveFrom\":\"%s\"," +
                        "\"effectiveTo\":%s,\"status\":\"%s\"}",
                id, passengerType,
                discountValue.getDiscountType(), discountValue.getValue(),
                effectiveFrom,
                effectiveTo != null ? "\"" + effectiveTo + "\"" : "null",
                status
        );
    }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }
}