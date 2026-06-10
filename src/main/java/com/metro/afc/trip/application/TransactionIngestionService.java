package com.metro.afc.trip.application;

import com.metro.afc.blacklist.application.port.out.BlacklistRepository;
import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.card.domain.model.enums.CardStatus;
import com.metro.afc.fare.application.FareCalculationService;
import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.operator.domain.model.Operator;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.ticket.domain.enums.TicketType;
import com.metro.afc.trip.application.dto.BatchIngestResponse;
import com.metro.afc.trip.application.dto.TransactionItemRequest;
import com.metro.afc.trip.application.port.out.TripAnomalyRepository;
import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.Trip;
import com.metro.afc.trip.domain.TripAnomaly;
import com.metro.afc.trip.domain.enums.trip.TicketTypeUsed;
import com.metro.afc.trip.domain.enums.trip.TripStatus;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalySeverity;
import com.metro.afc.trip.domain.enums.tripAnomaly.AnomalyType;
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

    private final FareCalculationService  fareCalculationService;
    private final TripAnomalyRepository   tripAnomalyRepository;
    private final TripRepository          tripRepository;
    private final CardRepository          cardRepository;
    private final OperatorRepository      operatorRepository;
    private final StationRepository       stationRepository;
    private final TicketRepository        ticketRepository;
    private final BlacklistRepository     blacklistRepository;
    private final FareRuleRepository      fareRuleRepository;

    @Transactional
    public BatchIngestResponse ingest(List<TransactionItemRequest> transactions) {
        int success = 0, skipped = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        for (TransactionItemRequest tx : transactions) {
            try {
                // 1. Idempotency check
                if (tripRepository.existsByExternalTransactionId(
                        tx.transactionId())) {
                    skipped++;
                    continue;
                }

                // 2. Lookup card (nullable — QR ticket không có card)
                Card card = null;
                if (tx.cardUid() != null) {
                    card = cardRepository
                            .findByCardUid(tx.cardUid().toUpperCase())
                            .orElseThrow(() -> new NotFoundException(
                                    ErrorCode.CARD_NOT_FOUND));

                    if (card.getStatus() != CardStatus.ACTIVE) {
                        log.warn("Transaction {} rejected: card {} status is {}",
                                tx.transactionId(), tx.cardUid(), card.getStatus());
                        failed++;
                        errors.add(errorPrefix(tx.transactionId())
                                + "card is " + card.getStatus());
                        continue;
                    }

                    if (blacklistRepository.existsActiveByCardId(card.getId())) {
                        log.warn("Transaction {} rejected: card {} is blacklisted",
                                tx.transactionId(), tx.cardUid());
                        failed++;
                        errors.add(errorPrefix(tx.transactionId())
                                + "card is blacklisted");
                        continue;
                    }
                }

                // 3. Lookup operator
                Operator operator = operatorRepository
                        .findByCode(tx.operatorCode().toUpperCase())
                        .orElseThrow(() -> new NotFoundException(
                                ErrorCode.OPERATOR_NOT_FOUND));

                // 4. Lookup tapIn station
                Station tapIn = stationRepository
                        .findByCode(tx.tapInStationCode().toUpperCase())
                        .orElseThrow(() -> new NotFoundException(
                                ErrorCode.STATION_NOT_FOUND));

                // 5. Lookup tapOut station
                Station toStation = null;
                UUID tapOutStationId = null;
                if (tx.tapOutStationCode() != null) {
                    toStation = stationRepository
                            .findByCode(tx.tapOutStationCode().toUpperCase())
                            .orElse(null);
                    tapOutStationId = toStation != null
                            ? toStation.getId() : null;
                }

                // 6. Lookup fare rule
                FareRule fareRule = fareRuleRepository
                        .findActiveByMode(tx.mode())
                        .orElse(null);

                // 7. Save trip
                Trip savedTrip = tripRepository.save(Trip.from(
                        tx.transactionId(),
                        card != null ? card.getId() : null,
                        tx.ticketId(),
                        operator.getId(),
                        tapIn.getId(), tx.tapInDeviceId(), tx.tapInAt(),
                        tapOutStationId, tx.tapOutDeviceId(), tx.tapOutAt(),
                        tx.distanceKm(), tx.fareAmount(),
                        tx.paymentMethod(), tx.ticketType(),
                        tx.tripStatus(), tx.debtAmount()
                ));

                // 8. Mark ticket USED
                if (tx.tripStatus() == TripStatus.COMPLETED
                        && tx.ticketType() == TicketTypeUsed.SINGLE_TRIP) {
                    if (card != null) {
                        ticketRepository.findActiveByCardIdAndType(
                                        card.getId(), TicketType.SINGLE_TRIP)
                                .ifPresent(ticket -> {
                                    ticket.markUsed();
                                    ticketRepository.save(ticket);
                                });
                    } else if (tx.ticketId() != null) {
                        ticketRepository.findById(tx.ticketId())
                                .ifPresent(ticket -> {
                                    ticket.markUsed();
                                    ticketRepository.save(ticket);
                                });
                    }
                }

                // 9. Fare mismatch detection
                if (tx.fareAmount() != null
                        && toStation != null
                        && fareRule != null
                        && tx.distanceKm() != null) {
                    try {
                        BigDecimal expected = fareCalculationService
                                .calculateRaw(tapIn, toStation,
                                        fareRule, tx.distanceKm());
                        BigDecimal diff = tx.fareAmount()
                                .subtract(expected).abs();
                        BigDecimal threshold = expected
                                .multiply(new BigDecimal("0.05"));

                        if (diff.compareTo(threshold) > 0) {
                            tripAnomalyRepository.save(TripAnomaly.of(
                                    savedTrip.getId(),
                                    AnomalyType.FARE_MISMATCH,
                                    AnomalySeverity.WARNING,
                                    String.format(
                                            "Fare mismatch: reported=%s, " +
                                                    "expected=%s, diff=%s",
                                            tx.fareAmount(), expected, diff)
                            ));
                            log.warn("Fare mismatch: transactionId={}, " +
                                            "reported={}, expected={}",
                                    tx.transactionId(),
                                    tx.fareAmount(), expected);
                        }
                    } catch (Exception e) {
                        log.warn("Could not verify fare for transaction {}: {}",
                                tx.transactionId(), e.getMessage());
                    }
                }

                success++;

            } catch (Exception e) {
                failed++;
                errors.add(errorPrefix(tx.transactionId()) + e.getMessage());
                log.warn("Failed to ingest transaction {}: {}",
                        tx.transactionId(), e.getMessage());
            }
        }

        log.info("Batch ingest: total={}, success={}, skipped={}, failed={}",
                transactions.size(), success, skipped, failed);

        return new BatchIngestResponse(
                transactions.size(), success, skipped, failed, errors
        );
    }

    private String errorPrefix(UUID transactionId) {
        return "transactionId=" + transactionId + ": ";
    }
}