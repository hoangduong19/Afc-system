package com.metro.afc.fareRuleCalculation;

import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FareRule calculateFare")
class FareRuleCalculateTest {

    private FareRule fareRule;

    @BeforeEach
    void setUp() {
        fareRule = FareRule.create(
                "HN_METRO_STANDARD", FareMode.METRO,
                new BigDecimal("8000"),
                new BigDecimal("850"),
                new BigDecimal("8000"),
                new BigDecimal("30000"),
                LocalDate.of(2025, 7, 1), null,
                UUID.randomUUID()
        );
    }

    @Test
    @DisplayName("Normal distance returns correct fare")
    void calculateFare_normalDistance_returnsCorrect() {
        // 8000 + 12.5 × 850 = 18625
        Money result = fareRule.calculateFare(new BigDecimal("12.5"));
        assertEquals(new BigDecimal("18625.00"), result.getAmount());
    }

    @Test
    @DisplayName("Short distance clamps to min price")
    void calculateFare_shortDistance_clampsToMin() {
        // 8000 + 0.1 × 850 = 8085 > minPrice → no clamp
        // use distance 0.0 → 8000 = min
        Money result = fareRule.calculateFare(new BigDecimal("0.0"));
        assertEquals(new BigDecimal("8000.00"), result.getAmount());
    }

    @Test
    @DisplayName("Long distance clamps to max price")
    void calculateFare_longDistance_clampsToMax() {
        // 8000 + 100 × 850 = 93000 > 30000 → clamp to 30000
        Money result = fareRule.calculateFare(new BigDecimal("100"));
        assertEquals(new BigDecimal("30000.00"), result.getAmount());
    }

    @Test
    @DisplayName("Exact min price distance")
    void calculateFare_exactMinDistance_returnsMin() {
        // 8000 + 0 × 850 = 8000 = minPrice
        Money result = fareRule.calculateFare(BigDecimal.ZERO);
        assertEquals(new BigDecimal("8000.00"), result.getAmount());
    }

    @Test
    @DisplayName("Distance producing exact max price")
    void calculateFare_exactMaxDistance_returnsMax() {
        // (30000 - 8000) / 850 = 25.88km → 8000 + 25.88×850 ≈ 30000
        Money result = fareRule.calculateFare(new BigDecimal("25.882352941"));
        assertEquals(new BigDecimal("30000.00"), result.getAmount());
    }
}