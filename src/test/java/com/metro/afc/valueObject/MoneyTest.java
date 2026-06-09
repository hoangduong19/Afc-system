package com.metro.afc.valueObject;

import com.metro.afc.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Money Value Object")
class MoneyTest {

    @Test
    @DisplayName("Create with valid amount")
    void create_validAmount_success() {
        Money money = Money.of(new BigDecimal("8000.00"));
        assertEquals(new BigDecimal("8000.00"), money.getAmount());
    }

    @Test
    @DisplayName("Create with null throws exception")
    void create_null_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> Money.of(null));
    }

    @Test
    @DisplayName("Create with negative throws exception")
    void create_negative_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> Money.of(new BigDecimal("-1")));
    }

    @Test
    @DisplayName("Add two Money values")
    void add_twoValues_returnsSum() {
        Money a = Money.of(new BigDecimal("8000"));
        Money b = Money.of(new BigDecimal("2000"));
        assertEquals(new BigDecimal("10000.00"), a.add(b).getAmount());
    }

    @Test
    @DisplayName("Multiply by factor")
    void multiply_byFactor_returnsProduct() {
        Money money = Money.of(new BigDecimal("18625"));
        Money result = money.multiply(new BigDecimal("0.5"));
        assertEquals(new BigDecimal("9312.50"), result.getAmount());
    }

    @Test
    @DisplayName("isLessThan returns true when less")
    void isLessThan_whenLess_returnsTrue() {
        Money a = Money.of(new BigDecimal("5000"));
        Money b = Money.of(new BigDecimal("8000"));
        assertTrue(a.isLessThan(b));
    }

    @Test
    @DisplayName("isGreaterThan returns true when greater")
    void isGreaterThan_whenGreater_returnsTrue() {
        Money a = Money.of(new BigDecimal("35000"));
        Money b = Money.of(new BigDecimal("30000"));
        assertTrue(a.isGreaterThan(b));
    }
}