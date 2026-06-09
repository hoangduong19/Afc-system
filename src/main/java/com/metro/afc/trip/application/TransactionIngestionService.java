package com.metro.afc.trip.application;

import com.metro.afc.blacklist.application.port.out.BlacklistRepository;
import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.card.domain.model.enums.CardStatus;
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
import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.Trip;
import com.metro.afc.trip.domain.enums.TicketTypeUsed;
import com.metro.afc.trip.domain.enums.TripStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionIngestionService {

    private final TripRepository tripRepository;
    private final CardRepository cardRepository;
    private final OperatorRepository operatorRepository;
    private final StationRepository stationRepository;
    private final TicketRepository ticketRepository;
    private final BlacklistRepository blacklistRepository;

    @Transactional
    public BatchIngestResponse ingest(List<TransactionItemRequest> transactions) {
        int success = 0, skipped = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        for (TransactionItemRequest tx : transactions) {
            try {
                // Idempotency check
                if (tripRepository.existsByExternalTransactionId(tx.transactionId())) {
                    skipped++;
                    continue;
                }

                // Lookup card
                Card card = cardRepository.findByCardUid(
                                tx.cardUid().toUpperCase())
                        .orElseThrow(() -> new NotFoundException(
                                ErrorCode.CARD_NOT_FOUND));

                if (card.getStatus() != CardStatus.ACTIVE) {
                    log.warn("Transaction {} rejected: card {} status is {}",
                            tx.transactionId(), tx.cardUid(), card.getStatus());
                    failed++;
                    errors.add(errorPrefix(tx.transactionId()) + "card is " + card.getStatus());
                    continue;
                }

                if (blacklistRepository.existsActiveByCardId(card.getId())) {
                    log.warn("Transaction {} rejected: card {} is blacklisted",
                            tx.transactionId(), tx.cardUid());
                    failed++;
                    errors.add(errorPrefix(tx.transactionId()) + "card is blacklisted");
                    continue;
                }

                // Lookup operator
                Operator operator = operatorRepository
                        .findByCode(tx.operatorCode().toUpperCase())
                        .orElseThrow(() -> new NotFoundException(
                                ErrorCode.OPERATOR_NOT_FOUND));

                // Lookup stations
                Station tapIn = stationRepository
                        .findByCode(tx.tapInStationCode().toUpperCase())
                        .orElseThrow(() -> new NotFoundException(
                                ErrorCode.STATION_NOT_FOUND));

                UUID tapOutStationId = null;
                if (tx.tapOutStationCode() != null) {
                    tapOutStationId = stationRepository
                            .findByCode(tx.tapOutStationCode().toUpperCase())
                            .map(Station::getId)
                            .orElse(null);
                }

                // Save trip
                tripRepository.save(Trip.from(
                        tx.transactionId(),
                        card.getId(), operator.getId(),
                        tapIn.getId(), tx.tapInDeviceId(), tx.tapInAt(),
                        tapOutStationId, tx.tapOutDeviceId(), tx.tapOutAt(),
                        tx.distanceKm(), tx.fareAmount(),
                        tx.paymentMethod(), tx.ticketType(),
                        tx.tripStatus(), tx.debtAmount()
                ));

                if (tx.tripStatus() == TripStatus.COMPLETED
                        && tx.ticketType() == TicketTypeUsed.SINGLE_TRIP) {
                    ticketRepository.findActiveByCardIdAndType(
                                    card.getId(), TicketType.SINGLE_TRIP)
                            .ifPresent(ticket -> {
                                ticket.markUsed();
                                ticketRepository.save(ticket);
                            });
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