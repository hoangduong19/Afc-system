package com.metro.afc.trip.application;

import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.trip.application.port.in.TripAnomalyUseCase;
import com.metro.afc.trip.application.port.out.TripAnomalyRepository;
import com.metro.afc.trip.domain.TripAnomaly;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalySeverity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripAnomalyService implements TripAnomalyUseCase {

    private final TripAnomalyRepository anomalyRepository;

    @Override
    public Page<TripAnomaly> findAll(AnomalySeverity severity,
                                     Boolean isResolved, Pageable pageable) {
        return anomalyRepository.findAllWithFilters(
                severity, isResolved, pageable);
    }

    @Override
    @Transactional
    public TripAnomaly resolve(UUID id, String notes) {
        TripAnomaly anomaly = anomalyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.ANOMALY_NOT_FOUND));
        anomaly.resolve(notes);
        return anomalyRepository.save(anomaly);
    }
}