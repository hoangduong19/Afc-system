package com.metro.afc.trip.domain.events.handler;

import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.events.FareCorrectedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FareCorrectedEventHandler {

    private final TripRepository tripRepository;

    @TransactionalEventListener(phase =
            TransactionPhase.BEFORE_COMMIT)
    public void handle(FareCorrectedEvent event) {
        tripRepository.findById(event.tripId())
                .ifPresent(trip -> {
                    trip.correctFare(event.correctedFare());
                    tripRepository.save(trip);
                });
    }
}