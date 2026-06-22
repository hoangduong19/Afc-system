package com.metro.afc.trip.application.port.in;

import com.metro.afc.passenger.dto.PassengerTripResponse;

import java.util.List;
import java.util.UUID;

public interface TripUseCase {
    List<PassengerTripResponse> findByUserId(UUID userId);
}