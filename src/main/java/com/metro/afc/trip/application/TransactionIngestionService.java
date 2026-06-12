package com.metro.afc.trip.application;

import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.fare.application.FareCalculationService;
import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.operator.domain.model.Operator;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.trip.application.dto.BatchIngestResponse;
import com.metro.afc.trip.application.dto.TransactionBatchRequest;
import com.metro.afc.trip.application.dto.TransactionItemRequest;
import com.metro.afc.trip.application.port.out.TripAnomalyRepository;
import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.Trip;
import com.metro.afc.trip.domain.enums.trip.TicketTypeUsed;
import com.metro.afc.trip.domain.enums.trip.TripStatus;
import com.metro.afc.trip.domain.service.FareMismatchDetector;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionIngestionService {

    private final TripRepository         tripRepository;
    private final TripAnomalyRepository  anomalyRepository;
    private final StationRepository      stationRepository;
    private final OperatorRepository     operatorRepository;
    private final FareCalculationService fareCalculationService;
    private final FareRuleRepository     fareRuleRepository;
    private final TicketRepository       ticketRepository;
    private final CardRepository         cardRepository;

    @Transactional
    public BatchIngestResponse ingest(TransactionBatchRequest req) {
        int success = 0, skipped = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        for (TransactionItemRequest item : req.transactions()) {
            try {
                if (tripRepository.existsByExternalTransactionId(
                        item.transactionId())) {
                    skipped++;
                    continue;
                }

                Trip trip = buildTrip(item);
                tripRepository.save(trip);

                markTicketUsedIfApplicable(trip, item);
                checkFareMismatch(trip, item);

                success++;

            } catch (Exception e) {
                failed++;
                errors.add(item.transactionId()
                        + ": " + e.getMessage());
                log.error("Failed to ingest {}: {}",
                        item.transactionId(), e.getMessage());
            }
        }

        return new BatchIngestResponse(
                req.transactions().size(),
                success, skipped, failed, errors);
    }

    private Trip buildTrip(TransactionItemRequest item) {
        UUID tapInStationId = stationRepository
                .findByCode(item.tapInStationCode())
                .map(Station::getId).orElse(null);

        UUID tapOutStationId = item.tapOutStationCode() != null
                ? stationRepository
                .findByCode(item.tapOutStationCode())
                .map(Station::getId).orElse(null)
                : null;

        UUID operatorId = operatorRepository
                .findByCode(item.operatorCode())
                .map(Operator::getId).orElse(null);

        UUID cardId = item.cardUid() != null
                ? cardRepository.findByCardUid(item.cardUid())
                .map(Card::getId).orElse(null)
                : null;

        return Trip.from(
                item.transactionId(),
                cardId,
                item.ticketId(),
                operatorId,
                tapInStationId,
                item.tapInDeviceId(),
                item.tapInAt(),
                tapOutStationId,
                item.tapOutDeviceId(),
                item.tapOutAt() != null
                        ? item.tapOutAt() : null,
                item.distanceKm(),
                item.fareAmount(),
                item.paymentMethod(),
                item.ticketType(),
                item.tripStatus(),
                item.debtAmount()
        );
    }

    private void markTicketUsedIfApplicable(Trip trip,
                                            TransactionItemRequest item) {
        if (item.tripStatus() != TripStatus.COMPLETED) return;
        if (item.ticketId() == null) return;
        if (item.ticketType() != TicketTypeUsed.SINGLE_TRIP) return;

        ticketRepository.findById(item.ticketId())
                .ifPresent(ticket -> {
                    ticket.markUsed();
                    ticketRepository.save(ticket);
                });
    }

    private void checkFareMismatch(Trip trip,
                                   TransactionItemRequest item) {
        if (item.tripStatus() != TripStatus.COMPLETED) return;
        if (item.fareAmount() == null) return;
        if (item.tapOutStationCode() == null) return;

        Station tapInStation = stationRepository
                .findByCode(item.tapInStationCode())
                .orElse(null);
        Station tapOutStation = stationRepository
                .findByCode(item.tapOutStationCode())
                .orElse(null);
        FareRule fareRule = fareRuleRepository
                .findActiveByMode(item.mode())
                .orElse(null);

        if (tapInStation == null
                || tapOutStation == null
                || fareRule == null) return;

        BigDecimal expected = fareCalculationService.calculateRaw(
                tapInStation, tapOutStation,
                fareRule, item.distanceKm()
        );

        FareMismatchDetector.detect(trip, expected)
                .ifPresent(anomaly -> {
                    anomalyRepository.save(anomaly);
                    log.warn("FARE_MISMATCH: trip={}, description={}",
                            trip.getId(), anomaly.getDescription());
                });
    }
}