package com.metro.afc.trip.domain.service;

import com.metro.afc.trip.domain.Trip;
import com.metro.afc.trip.domain.TripAnomaly;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalySeverity;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalyType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

// trip/domain/service/FareMismatchDetector.java
public class FareMismatchDetector {

    private static final BigDecimal THRESHOLD =
            new BigDecimal("0.05");
    private static final BigDecimal ERROR_THRESHOLD =
            new BigDecimal("0.20");

    public static Optional<TripAnomaly> detect(
            Trip trip, BigDecimal expectedFare) {

        if (trip.getFareAmount() == null
                || expectedFare == null
                || expectedFare.compareTo(BigDecimal.ZERO) == 0)
            return Optional.empty();

        BigDecimal diff = trip.getFareAmount()
                .subtract(expectedFare).abs();

        BigDecimal diffPercent = diff.divide(
                expectedFare, 6, RoundingMode.HALF_UP);

        if (diffPercent.compareTo(THRESHOLD) <= 0)
            return Optional.empty();

        AnomalySeverity severity =
                diffPercent.compareTo(ERROR_THRESHOLD) > 0
                        ? AnomalySeverity.ERROR
                        : AnomalySeverity.WARNING;

        String description = String.format(
                "Fare mismatch: expected %s, received %s. " +
                        "Deviation: %.1f%% > 5%% threshold.",
                expectedFare,
                trip.getFareAmount(),
                diffPercent.multiply(BigDecimal.valueOf(100))
        );

        return Optional.of(TripAnomaly.create(
                trip.getId(),
                AnomalyType.FARE_MISMATCH,
                severity,
                description
        ));
    }
}