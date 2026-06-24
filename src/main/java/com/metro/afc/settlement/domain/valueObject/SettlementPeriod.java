package com.metro.afc.settlement.domain.valueObject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class SettlementPeriod {

    private final int year;
    private final int month;

    public SettlementPeriod(int month, int year) {
        if (month < 1 || month > 12)
            throw new IllegalArgumentException("Month must be 1-12");
        if (year < 2020)
            throw new IllegalArgumentException("Year must be >= 2020");
        this.month = month;
        this.year  = year;
    }

    public LocalDate fromLocalDate() {
        return LocalDate.of(year, month, 1);
    }

    public LocalDate toLocalDate() {
        return fromLocalDate().plusMonths(1).minusDays(1);
    }

    public String format() {
        return String.format("%d-%02d", year, month);
    }

    public LocalDate start() {
        return LocalDate.of(year, month, 1);
    }

    public LocalDate end() {
        return start().withDayOfMonth(start().lengthOfMonth());
    }

    public Instant fromInstant() {
        return start().atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    public Instant toInstant() {
        return end().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}