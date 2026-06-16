package com.metro.afc.fareRuleCalculation;

import com.metro.afc.fare.domain.model.FarePassPrice;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRule.PassDurationType;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.ticket.domain.enums.PassScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FareRule")
class FareRuleCalculateTest {

    private static final UUID ACTOR = UUID.randomUUID();

    /**
     * baseFare=8000, ratePerKm=850, min=8000, max=30000
     */
    private FareRule metroRule;

    /**
     * BUS rule với pass prices: DAILY, WEEKLY, MONTHLY 1/2 tháng × SINGLE/MULTI
     */
    private FareRule busRule;

    @BeforeEach
    void setUp() {
        metroRule = FareRule.create(
                "HN_METRO_STANDARD", FareMode.METRO,
                new BigDecimal("8000"),
                new BigDecimal("850"),
                new BigDecimal("8000"),
                new BigDecimal("30000"),
                List.of(
                        FarePassPrice.of(PassDurationType.DAILY,   null, null, new BigDecimal("40000")),
                        FarePassPrice.of(PassDurationType.WEEKLY,  null, null, new BigDecimal("160000")),
                        FarePassPrice.of(PassDurationType.MONTHLY, 1,    null, new BigDecimal("200000")),
                        FarePassPrice.of(PassDurationType.MONTHLY, 3,    null, new BigDecimal("590000"))
                ),
                LocalDate.of(2025, 7, 1), null, ACTOR
        );

        busRule = FareRule.create(
                "HN_BUS_STANDARD", FareMode.BUS,
                new BigDecimal("3000"),
                new BigDecimal("450"),
                new BigDecimal("3000"),
                new BigDecimal("30000"),
                List.of(
                        FarePassPrice.of(PassDurationType.DAILY,   null, null,                        new BigDecimal("30000")),
                        FarePassPrice.of(PassDurationType.WEEKLY,  null, null,                        new BigDecimal("120000")),
                        FarePassPrice.of(PassDurationType.MONTHLY, 1,    PassScope.SINGLE_ROUTE,      new BigDecimal("100000")),
                        FarePassPrice.of(PassDurationType.MONTHLY, 1,    PassScope.MULTI_ROUTE,       new BigDecimal("200000")),
                        FarePassPrice.of(PassDurationType.MONTHLY, 2,    PassScope.SINGLE_ROUTE,      new BigDecimal("270000")),
                        FarePassPrice.of(PassDurationType.MONTHLY, 2,    PassScope.MULTI_ROUTE,       new BigDecimal("550000"))
                ),
                LocalDate.of(2025, 7, 1), null, ACTOR
        );
    }

    // ── calculateFare ─────────────────────────────────────────────

    @Nested
    @DisplayName("calculateFare")
    class CalculateFare {

        @Test
        @DisplayName("Normal distance returns baseFare + ratePerKm × dist")
        void normalDistance_returnsLinearFare() {
            Money result = metroRule.calculateFare(new BigDecimal("12.5"));
            assertMoney("18625.00", result);
        }

        @Test
        @DisplayName("Zero distance returns minPrice (equals baseFare here)")
        void zeroDistance_returnsMin() {
            Money result = metroRule.calculateFare(BigDecimal.ZERO);
            assertMoney("8000.00", result);
        }

        @Test
        @DisplayName("Very short distance returns linear fare above minPrice")
        void shortDistance_aboveMin() {
            Money result = metroRule.calculateFare(new BigDecimal("0.001"));
            assertMoney("8000.85", result);
        }

        @Test
        @DisplayName("Long distance clamps down to maxPrice")
        void longDistance_clampsToMax() {
            Money result = metroRule.calculateFare(new BigDecimal("100"));
            assertMoney("30000.00", result);
        }

        @Test
        @DisplayName("Distance producing exactly maxPrice returns maxPrice")
        void exactMaxBoundary_returnsMax() {
            Money result = metroRule.calculateFare(new BigDecimal("25.882352942"));
            assertMoney("30000.00", result);
        }

        @ParameterizedTest(name = "dist={0} → expected={1}")
        @CsvSource({
                "5.0,   12250.00",
                "10.0,  16500.00",
                "25.0,  29250.00",
                "26.0,  30000.00",
        })
        @DisplayName("Parametrized distance → expected fare")
        void parametrizedDistances(String dist, String expected) {
            Money result = metroRule.calculateFare(new BigDecimal(dist));
            assertMoney(expected, result);
        }

        @Test
        @DisplayName("baseFare < minPrice clamps short trip up to minPrice")
        void baseFareBelowMin_shortTrip_clampsToMin() {
            FareRule rule = FareRule.create(
                    "TEST_CLAMP", FareMode.METRO,
                    new BigDecimal("5000"),
                    new BigDecimal("850"),
                    new BigDecimal("8000"),
                    new BigDecimal("30000"),
                    List.of(),
                    LocalDate.of(2025, 7, 1), null, ACTOR
            );
            Money result = rule.calculateFare(BigDecimal.ZERO);
            assertMoney("8000.00", result);
        }
    }

    // ── lookupPassPrice ───────────────────────────────────────────

    @Nested
    @DisplayName("lookupPassPrice")
    class LookupPassPrice {

        @Test
        @DisplayName("METRO DAILY trả đúng giá")
        void metro_daily_returnsCorrectPrice() {
            Money result = metroRule.lookupPassPrice(PassDurationType.DAILY, null, null);
            assertMoney("40000", result);
        }

        @Test
        @DisplayName("METRO WEEKLY trả đúng giá")
        void metro_weekly_returnsCorrectPrice() {
            Money result = metroRule.lookupPassPrice(PassDurationType.WEEKLY, null, null);
            assertMoney("160000", result);
        }

        @Test
        @DisplayName("METRO MONTHLY 1 tháng trả đúng giá")
        void metro_monthly1_returnsCorrectPrice() {
            Money result = metroRule.lookupPassPrice(PassDurationType.MONTHLY, 1, null);
            assertMoney("200000", result);
        }

        @Test
        @DisplayName("METRO MONTHLY 3 tháng trả đúng giá")
        void metro_monthly3_returnsCorrectPrice() {
            Money result = metroRule.lookupPassPrice(PassDurationType.MONTHLY, 3, null);
            assertMoney("590000", result);
        }

        @Test
        @DisplayName("BUS DAILY trả đúng giá")
        void bus_daily_returnsCorrectPrice() {
            Money result = busRule.lookupPassPrice(PassDurationType.DAILY, null, null);
            assertMoney("30000", result);
        }

        @Test
        @DisplayName("BUS MONTHLY 1 tháng SINGLE_ROUTE trả đúng giá")
        void bus_monthly1_singleRoute_returnsCorrectPrice() {
            Money result = busRule.lookupPassPrice(PassDurationType.MONTHLY, 1, PassScope.SINGLE_ROUTE);
            assertMoney("100000", result);
        }

        @Test
        @DisplayName("BUS MONTHLY 1 tháng MULTI_ROUTE trả đúng giá")
        void bus_monthly1_multiRoute_returnsCorrectPrice() {
            Money result = busRule.lookupPassPrice(PassDurationType.MONTHLY, 1, PassScope.MULTI_ROUTE);
            assertMoney("200000", result);
        }

        @Test
        @DisplayName("BUS MONTHLY 2 tháng SINGLE_ROUTE trả đúng giá")
        void bus_monthly2_singleRoute_returnsCorrectPrice() {
            Money result = busRule.lookupPassPrice(PassDurationType.MONTHLY, 2, PassScope.SINGLE_ROUTE);
            assertMoney("270000", result);
        }

        @Test
        @DisplayName("Key không tồn tại throws BusinessRuleException")
        void missingKey_throwsException() {
            assertThatThrownBy(() ->
                    metroRule.lookupPassPrice(PassDurationType.MONTHLY, 6, null))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    // ── Lifecycle ────────────────────────────────────────────────

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
                    .isEqualTo(LocalDate.of(2025, 12, 31));
        }

        @Test
        @DisplayName("newVersion() increments version number")
        void newVersion_incrementsVersion() {
            FareRule v2 = metroRule.newVersion(
                    new BigDecimal("9000"), new BigDecimal("900"),
                    new BigDecimal("9000"), new BigDecimal("35000"),
                    List.of(FarePassPrice.of(PassDurationType.MONTHLY, 1, null, new BigDecimal("220000"))),
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
                    List.of(),
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
                    new BigDecimal("30000"),
                    new BigDecimal("8000"),
                    List.of(),
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
                    List.of(),
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