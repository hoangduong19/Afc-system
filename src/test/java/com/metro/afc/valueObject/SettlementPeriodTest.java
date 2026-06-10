package com.metro.afc.valueObject;

import com.metro.afc.settlement.domain.valueObject.SettlementPeriod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SettlementPeriod VO")
class SettlementPeriodTest {

    @Test
    @DisplayName("format returns correct string")
    void format_returnsCorrectString() {
        SettlementPeriod period = new SettlementPeriod(6, 2026);
        assertEquals("2026-06", period.format());
    }

    @Test
    @DisplayName("start returns first day of month")
    void start_returnsFirstDay() {
        SettlementPeriod period = new SettlementPeriod(6, 2026);
        assertEquals(LocalDate.of(2026, 6, 1), period.start());
    }

    @Test
    @DisplayName("end returns last day of month")
    void end_returnsLastDay() {
        SettlementPeriod period = new SettlementPeriod(6, 2026);
        assertEquals(LocalDate.of(2026, 6, 30), period.end());
    }

    @Test
    @DisplayName("invalid month throws exception")
    void invalidMonth_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new SettlementPeriod(13, 2026));
    }

    @Test
    @DisplayName("invalid year throws exception")
    void invalidYear_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new SettlementPeriod(6, 2019));
    }

    @Test
    @DisplayName("february end returns 28 or 29")
    void february_returnsCorrectEnd() {
        assertEquals(LocalDate.of(2026, 2, 28),
                new SettlementPeriod(2, 2026).end());
        assertEquals(LocalDate.of(2024, 2, 29),
                new SettlementPeriod(2, 2024).end());
    }
}
