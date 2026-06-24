package com.metro.afc.passenger.dto;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.trip.domain.Trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PassengerTripResponse(
        UUID tripId,
        UUID ticketId,
        FareMode mode,
        String tapInStationCode,
        String tapOutStationCode,
        Instant tapInAt,
        Instant tapOutAt,
        BigDecimal distanceKm,
        BigDecimal fareAmount
) {
    public static PassengerTripResponse from(Trip t,
                                             String tapInCode,
                                             String tapOutCode) {
        return new PassengerTripResponse(
                t.getId(),
                t.getTicketId(),
                t.getTransportMode(),
                tapInCode,
                tapOutCode,
                t.getTapInAt(),
                t.getTapOutAt(),
                t.getDistanceKm(),
                t.getFareAmount()
        );
    }
}