package com.metro.afc.trip.domain.events;

import java.math.BigDecimal;
import java.util.UUID;

public record FareCorrectedEvent(
        UUID tripId,
        BigDecimal correctedFare
) {}