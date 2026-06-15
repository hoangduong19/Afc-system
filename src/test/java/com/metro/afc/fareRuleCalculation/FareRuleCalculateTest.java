package com.metro.afc.fareRuleCalculation;

import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.ticket.domain.enums.PassScope;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FareRule")
class FareRuleCalculateTest {

    // ── Fixtures ──────────────────────────────────────────────────

    private static final UUID ACTOR = UUID.randomUUID();

    /**
     * baseFare=8000, ratePerKm=850, min=8000, max=30000
     * Formula: fare = max(min, min(max, baseFare + ratePerKm × dist))
     */
    private FareRule metroRule;

    /**
     * BUS rule: monthlySinglePrice=100_000, monthlyMultiPrice=200_000
     */
    private FareRule busRule;

    @BeforeEach
    void setUp() {
        metroRule = FareRule.create(
                "HN_METRO_STANDARD", FareMode.METRO,
                new BigDecimal("8000"),  // baseFare
                new BigDecimal("850"),   // ratePerKm
                new BigDecimal("8000"),  // minPrice
                new BigDecimal("30000"), // maxPrice
                null,                    // monthlySinglePrice — not needed for single-trip
                null,                    // monthlyMultiPrice
                LocalDate.of(2025, 7, 1),
                null,
                ACTOR
        );

        busRule = FareRule.create(
                "HN_BUS_STANDARD", FareMode.BUS,
                new BigDecimal("3000"),
                new BigDecimal("450"),
                new BigDecimal("3000"),
                new BigDecimal("30000"),
                new BigDecimal("100000"), // monthlySinglePrice
                new BigDecimal("200000"), // monthlyMultiPrice
                LocalDate.of(2025, 7, 1),
                null,
                ACTOR
        );
    }

    // ── calculateFare ─────────────────────────────────────────────

    @Nested
    @DisplayName("calculateFare")
    class CalculateFare {

        @Test
        @DisplayName("Normal distance returns baseFare + ratePerKm × dist")
        void normalDistance_returnsLinearFare() {
            // 8000 + 12.5 × 850 = 18625.00
            Money result = metroRule.calculateFare(new BigDecimal("12.5"));
            assertMoney("18625.00", result);
        }

        @Test
        @DisplayName("Zero distance returns minPrice (equals baseFare here)")
        void zeroDistance_returnsMin() {
            // 8000 + 0 × 850 = 8000 = minPrice
            Money result = metroRule.calculateFare(BigDecimal.ZERO);
            assertMoney("8000.00", result);
        }

        @Test
        @DisplayName("Very short distance clamps up to minPrice")
        void shortDistance_clampsToMin() {
            // Need fare < minPrice: impossible with baseFare == minPrice.
            // This test documents the boundary: distance 0.001 → 8000.85 > min → no clamp.
            // Real clamp-to-min only possible if baseFare < minPrice (different rule config).
            // With current fixture, minPrice is always hit at dist=0.
            Money result = metroRule.calculateFare(new BigDecimal("0.001"));
            // 8000 + 0.001×850 = 8000.85 > 8000 — returns linear fare
            assertMoney("8000.85", result);
        }

        @Test
        @DisplayName("Long distance clamps down to maxPrice")
        void longDistance_clampsToMax() {
            // 8000 + 100 × 850 = 93000 > 30000 → clamp to 30000
            Money result = metroRule.calculateFare(new BigDecimal("100"));
            assertMoney("30000.00", result);
        }

        @Test
        @DisplayName("Distance producing exactly maxPrice returns maxPrice")
        void exactMaxBoundary_returnsMax() {
            // Solve: 8000 + d × 850 = 30000 → d = 22000/850 ≈ 25.882352941...
            // Accumulated > max → clamped
            Money result = metroRule.calculateFare(new BigDecimal("25.882352942"));
            assertMoney("30000.00", result);
        }

        @Test
        @DisplayName("Distance at exact max boundary (d = 22000/850) stays at maxPrice")
        void atExactMaxBoundary_returnsMax() {
            // 8000 + (22000/850) × 850 = 8000 + 22000 = 30000 exact
            BigDecimal exactDist = new BigDecimal("22000").divide(new BigDecimal("850"), 10, java.math.RoundingMode.HALF_UP);
            Money result = metroRule.calculateFare(exactDist);
            assertMoney("30000.00", result);
        }

        @ParameterizedTest(name = "dist={0} → expected={1}")
        @CsvSource({
                "5.0,   12250.00",   // 8000 + 5×850
                "10.0,  16500.00",   // 8000 + 10×850
                "25.0,  29250.00",   // 8000 + 25×850 < 30000 → no clamp
                "26.0,  30000.00",   // 8000 + 26×850 = 30100 > 30000 → clamp
        })
        @DisplayName("Parametrized distance → expected fare")
        void parametrizedDistances(String dist, String expected) {
            Money result = metroRule.calculateFare(new BigDecimal(dist));
            assertMoney(expected, result);
        }

        @Test
        @DisplayName("FareRule with baseFare < minPrice clamps short trip up to minPrice")
        void baseFareBelowMin_shortTrip_clampsToMin() {
            // Create a rule where baseFare < minPrice to exercise the clamp-up path
            FareRule rule = FareRule.create(
                    "TEST_CLAMP", FareMode.METRO,
                    new BigDecimal("5000"),  // baseFare
                    new BigDecimal("850"),
                    new BigDecimal("8000"),  // minPrice > baseFare
                    new BigDecimal("30000"),
                    null, null,
                    LocalDate.of(2025, 7, 1), null, ACTOR
            );
            // 5000 + 0 × 850 = 5000 < 8000 → clamp to 8000
            Money result = rule.calculateFare(BigDecimal.ZERO);
            assertMoney("8000.00", result);
        }
    }

    // ── calculateMonthlyPassPrice ─────────────────────────────────

    @Nested
    @DisplayName("calculateMonthlyPassPrice")
    class CalculateMonthlyPassPrice {

        @Test
        @DisplayName("BUS SINGLE_ROUTE 30-day pass returns monthlySinglePrice as-is")
        void bus_singleRoute_30days_returnsBasePrice() {
            Money result = busRule.calculateMonthlyPassPrice(PassScope.SINGLE_ROUTE, 30);
            assertMoney("100000", result);
        }

        @Test
        @DisplayName("BUS MULTI_ROUTE 30-day pass returns monthlyMultiPrice as-is")
        void bus_multiRoute_30days_returnsBasePrice() {
            Money result = busRule.calculateMonthlyPassPrice(PassScope.MULTI_ROUTE, 30);
            assertMoney("200000", result);
        }

        @Test
        @DisplayName("BUS SINGLE_ROUTE 15-day pass returns half price (pro-rated)")
        void bus_singleRoute_15days_returnsProrated() {
            // 100000 × 15 / 30 = 50000
            Money result = busRule.calculateMonthlyPassPrice(PassScope.SINGLE_ROUTE, 15);
            assertMoney("50000", result);
        }

        @Test
        @DisplayName("BUS MULTI_ROUTE 7-day pass returns pro-rated price (HALF_UP)")
        void bus_multiRoute_7days_returnsProrated() {
            // 200000 × 7 / 30 = 46666.67 → HALF_UP → 46667
            Money result = busRule.calculateMonthlyPassPrice(PassScope.MULTI_ROUTE, 7);
            assertMoney("46667", result);
        }

        @Test
        @DisplayName("METRO null scope 30-day pass returns monthlySinglePrice")
        void metro_nullScope_30days_returnsSinglePrice() {
            FareRule metroWithMonthly = FareRule.create(
                    "HN_METRO_MONTHLY", FareMode.METRO,
                    new BigDecimal("8000"), new BigDecimal("850"),
                    new BigDecimal("8000"), new BigDecimal("30000"),
                    new BigDecimal("150000"), null,
                    LocalDate.of(2025, 7, 1), null, ACTOR
            );
            Money result = metroWithMonthly.calculateMonthlyPassPrice(null, 30);
            assertMoney("150000", result);
        }

        @Test
        @DisplayName("BUS with null scope throws BusinessRuleException")
        void bus_nullScope_throwsException() {
            assertThatThrownBy(() -> busRule.calculateMonthlyPassPrice(null, 30))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("METRO with non-null scope throws BusinessRuleException")
        void metro_withScope_throwsException() {
            FareRule metroRule2 = FareRule.create(
                    "HN_METRO_2", FareMode.METRO,
                    new BigDecimal("8000"), new BigDecimal("850"),
                    new BigDecimal("8000"), new BigDecimal("30000"),
                    new BigDecimal("150000"), null,
                    LocalDate.of(2025, 7, 1), null, ACTOR
            );
            assertThatThrownBy(() -> metroRule2.calculateMonthlyPassPrice(PassScope.SINGLE_ROUTE, 30))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    // ── Lifecycle / state behavior ────────────────────────────────

    @Nested
    @DisplayName("Lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("Newly created rule is ACTIVE")
        void create_statusIsActive() {
            assertThat(metroRule.isActive()).isTrue();
        }

        @Test
        @DisplayName("disable() marks rule INACTIVE")
        void disable_marksInactive() {
            metroRule.disable("No longer valid", ACTOR);
            assertThat(metroRule.isActive()).isFalse();
        }

        @Test
        @DisplayName("disable() on already-INACTIVE rule throws BusinessRuleException")
        void disable_alreadyInactive_throws() {
            metroRule.disable("first", ACTOR);
            assertThatThrownBy(() -> metroRule.disable("second", ACTOR))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("closeVersion() sets effectiveTo and marks INACTIVE")
        void closeVersion_setsEffectiveToAndInactive() {
            LocalDate newFrom = LocalDate.of(2026, 1, 1);
            metroRule.closeVersion(newFrom);

            assertThat(metroRule.isActive()).isFalse();
            assertThat(metroRule.getEffectiveTo())
                    .isEqualTo(LocalDate.of(2025, 12, 31)); // newFrom - 1 day
        }

        @Test
        @DisplayName("newVersion() increments version number")
        void newVersion_incrementsVersion() {
            FareRule v2 = metroRule.newVersion(
                    new BigDecimal("9000"), new BigDecimal("900"),
                    new BigDecimal("9000"), new BigDecimal("35000"),
                    null, null,
                    LocalDate.of(2026, 1, 1), null, "Price adjustment", ACTOR
            );
            assertThat(v2.getVersion()).isEqualTo(2);
        }

        @Test
        @DisplayName("newVersion() inherits code and mode from parent")
        void newVersion_inheritsCodeAndMode() {
            FareRule v2 = metroRule.newVersion(
                    new BigDecimal("9000"), new BigDecimal("900"),
                    new BigDecimal("9000"), new BigDecimal("35000"),
                    null, null,
                    LocalDate.of(2026, 1, 1), null, "Price adjustment", ACTOR
            );
            assertThat(v2.getCode()).isEqualTo(metroRule.getCode());
            assertThat(v2.getMode()).isEqualTo(metroRule.getMode());
        }

        @Test
        @DisplayName("create() with minPrice > maxPrice throws IllegalArgumentException")
        void create_invalidPriceRange_throws() {
            assertThatThrownBy(() -> FareRule.create(
                    "INVALID", FareMode.METRO,
                    new BigDecimal("8000"), new BigDecimal("850"),
                    new BigDecimal("30000"), // minPrice
                    new BigDecimal("8000"),  // maxPrice < minPrice
                    null, null,
                    LocalDate.of(2025, 7, 1), null, ACTOR
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("create() with equal minPrice and maxPrice is allowed")
        void create_equalMinMax_allowed() {
            assertThatCode(() -> FareRule.create(
                    "FLAT", FareMode.METRO,
                    new BigDecimal("8000"), new BigDecimal("0"),
                    new BigDecimal("8000"),
                    new BigDecimal("8000"),
                    null, null,
                    LocalDate.of(2025, 7, 1), null, ACTOR
            )).doesNotThrowAnyException();
        }

    }

    // ── Helper ────────────────────────────────────────────────────

    private void assertMoney(String expected, Money actual) {
        assertThat(actual.getAmount().stripTrailingZeros())
                .isEqualByComparingTo(new BigDecimal(expected).stripTrailingZeros());
    }
}