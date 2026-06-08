package com.metro.afc.shared.domain.valueobject;

import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.DiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
public class DiscountValue {

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    protected DiscountValue() {}

    public DiscountValue(DiscountType discountType, BigDecimal value) {
        validate(discountType, value);
        this.discountType = discountType;
        this.value        = value;
    }

    private void validate(DiscountType type, BigDecimal value) {
        if (type == null)
            throw new IllegalArgumentException("Discount type is required");
        if (value == null)
            throw new IllegalArgumentException("Discount value is required");
        if (value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Discount value must be >= 0");
        if (type == DiscountType.PERCENT
                && value.compareTo(BigDecimal.valueOf(100)) > 0)
            throw new IllegalArgumentException("Percent discount must be <= 100");
    }

    public Money apply(Money originalPrice) {
        return switch (discountType) {
            case PERCENT -> {
                BigDecimal factor = BigDecimal.ONE.subtract(
                        value.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                );
                yield originalPrice.multiply(factor);
            }
            case FIXED -> {
                BigDecimal discounted = originalPrice.getAmount().subtract(value);
                yield Money.of(discounted.max(BigDecimal.ZERO));
            }
        };
    }

    public DiscountType getDiscountType() { return discountType; }
    public BigDecimal getValue()          { return value; }
}