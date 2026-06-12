package com.metro.afc.trip.infrastructure.scheduler;

import com.metro.afc.trip.application.port.out.TripAnomalyRepository;
import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.Trip;
import com.metro.afc.trip.domain.TripAnomaly;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalySeverity;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalyType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncompleteTripDetector {

    private final TripRepository tripRepository;
    private final TripAnomalyRepository anomalyRepository;

    @Scheduled(cron = "0 0 * * * *") // mỗi giờ
    @Transactional
    public void detect() {
        Instant threshold = Instant.now()
                .minus(2, ChronoUnit.HOURS);

        List<Trip> stuckTrips = tripRepository
                .findInProgressBefore(threshold);

        for (Trip trip : stuckTrips) {
            // Tránh tạo duplicate anomaly
            if (anomalyRepository.existsByTripIdAndType(
                    trip.getId(), AnomalyType.INCOMPLETE_TRIP))
                continue;

            String stationCode = trip.getTapInStationId() != null
                    ? trip.getTapInStationId().toString()
                    : "UNKNOWN";

            String description = String.format(
                    "Incomplete trip: tap-in at %s (%s) but no " +
                            "tap-out detected after 2 hours. " +
                            "Card may be stuck in system.",
                    stationCode,
                    trip.getTapInAt()
                            .atZone(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ofPattern("HH:mm"))
            );

            TripAnomaly anomaly = TripAnomaly.create(
                    trip.getId(),
                    AnomalyType.INCOMPLETE_TRIP,
                    AnomalySeverity.ERROR,
                    description
            );

            anomalyRepository.save(anomaly);
            log.warn("INCOMPLETE_TRIP: trip={}", trip.getId());
        }
    }
}