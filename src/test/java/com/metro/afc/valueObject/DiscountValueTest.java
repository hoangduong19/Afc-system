package com.metro.afc.valueObject;

import com.metro.afc.fare.domain.model.enums.fareRuleDiscount.DiscountType;
import com.metro.afc.shared.domain.valueobject.DiscountValue;
import com.metro.afc.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("DiscountValue Value Object")
class DiscountValueTest {

    @Test
    @DisplayName("PERCENT 50 applied correctly")
    void apply_percent50_returnsHalfPrice() {
        DiscountValue discount = new DiscountValue(DiscountType.PERCENT, new BigDecimal("50"));
        Money original = Money.of(new BigDecimal("18625"));
        Money result   = discount.apply(original);
        assertEquals(new BigDecimal("9312.50"), result.getAmount());
    }

    @Test
    @DisplayName("PERCENT 100 returns zero")
    void apply_percent100_returnsZero() {
        DiscountValue discount = new DiscountValue(DiscountType.PERCENT, new BigDecimal("100"));
        Money original = Money.of(new BigDecimal("18625"));
        Money result   = discount.apply(original);
        assertEquals(new BigDecimal("0.00"), result.getAmount());
    }

    @Test
    @DisplayName("PERCENT 0 returns original")
    void apply_percent0_returnsOriginal() {
        DiscountValue discount = new DiscountValue(DiscountType.PERCENT, new BigDecimal("0"));
        Money original = Money.of(new BigDecimal("18625"));
        Money result   = discount.apply(original);
        assertEquals(new BigDecimal("18625.00"), result.getAmount());
    }

    @Test
    @DisplayName("FIXED amount applied correctly")
    void apply_fixedAmount_returnsSubtracted() {
        DiscountValue discount = new DiscountValue(DiscountType.FIXED, new BigDecimal("5000"));
        Money original = Money.of(new BigDecimal("18625"));
        Money result   = discount.apply(original);
        assertEquals(new BigDecimal("13625.00"), result.getAmount());
    }

    @Test
    @DisplayName("FIXED amount greater than fare returns zero")
    void apply_fixedGreaterThanFare_returnsZero() {
        DiscountValue discount = new DiscountValue(DiscountType.FIXED, new BigDecimal("50000"));
        Money original = Money.of(new BigDecimal("18625"));
        Money result   = discount.apply(original);
        assertEquals(new BigDecimal("0.00"), result.getAmount());
    }

    @Test
    @DisplayName("PERCENT greater than 100 throws exception")
    void create_percentOver100_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new DiscountValue(DiscountType.PERCENT, new BigDecimal("101")));
    }

    @Test
    @DisplayName("Null type throws exception")
    void create_nullType_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new DiscountValue(null, new BigDecimal("50")));
    }

    @Test
    @DisplayName("Null value throws exception")
    void create_nullValue_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new DiscountValue(DiscountType.PERCENT, null));
    }

    @Test
    @DisplayName("Negative value throws exception")
    void create_negativeValue_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new DiscountValue(DiscountType.PERCENT, new BigDecimal("-1")));
    }
}
