package com.metro.afc.shared.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
public class Money {

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    protected Money() {}

    public Money(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Amount must be >= 0");
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money ofNullable(BigDecimal amount) {
        return amount == null ? null : new Money(amount);
    }

    public BigDecimal getAmount() { return amount; }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        BigDecimal result = this.amount.subtract(other.amount);
        return new Money(result.max(BigDecimal.ZERO));
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor)
                .setScale(2, RoundingMode.HALF_UP));
    }

    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    @Override
    public String toString() { return amount.toPlainString(); }
}