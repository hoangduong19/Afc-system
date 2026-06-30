package com.metro.afc.trip.application;

import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.fare.application.FareCalculationService;
import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.operator.domain.model.Operator;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import com.metro.afc.ticket.application.port.out.TicketRepository;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.trip.application.dto.BatchIngestResponse;
import com.metro.afc.trip.application.dto.TransactionBatchRequest;
import com.metro.afc.trip.application.dto.TransactionItemRequest;
import com.metro.afc.trip.application.port.out.TripAnomalyRepository;
import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.Trip;
import com.metro.afc.trip.domain.TripAnomaly;
import com.metro.afc.trip.domain.enums.trip.TicketTypeUsed;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionIngestionService {

    private static final int CHUNK_SIZE = 1000; // an toàn dù input hiện tại max 500/message

    private final TripRepository         tripRepository;
    private final TripAnomalyRepository  anomalyRepository;
    private final StationRepository      stationRepository;
    private final OperatorRepository     operatorRepository;
    private final FareCalculationService fareCalculationService;
    private final FareRuleRepository     fareRuleRepository;
    private final TicketRepository       ticketRepository;
    private final CardRepository         cardRepository;

    public BatchIngestResponse ingest(TransactionBatchRequest req) {
        List<TransactionItemRequest> items = req.transactions();

        Map<String, Station> stationByCode = stationRepository.findAll().stream()
                .collect(Collectors.toMap(Station::getCode, s -> s));

        Map<String, UUID> operatorIdByCode = operatorRepository.findAll().stream()
                .collect(Collectors.toMap(Operator::getCode, Operator::getId));

        Map<FareMode, FareRule> fareRuleByMode = fareRuleRepository.findAllActive().stream()
                .collect(Collectors.toMap(FareRule::getMode, fr -> fr));

        int totalCount = items.size();
        int success = 0, skipped = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        List<List<TransactionItemRequest>> chunks = partition(items, CHUNK_SIZE);

        for (int i = 0; i < chunks.size(); i++) {
            List<TransactionItemRequest> chunk = chunks.get(i);
            try {
                ChunkResult result = ingestChunk(chunk, stationByCode, operatorIdByCode, fareRuleByMode);
                success += result.success();
                skipped += result.skipped();
                failed  += result.failed();
                errors.addAll(result.errors());
            } catch (Exception e) {
                failed += chunk.size();
                String msg = "Chunk #" + i + " failed entirely (" + chunk.size() + " items): " + e.getMessage();
                errors.add(msg);
                log.error(msg, e);
            }
        }

        log.info("Ingest finished: total={}, success={}, skipped={}, failed={}",
                totalCount, success, skipped, failed);

        return new BatchIngestResponse(totalCount, success, skipped, failed, errors);
    }

    @Transactional
    public ChunkResult ingestChunk(List<TransactionItemRequest> chunk,
                                   Map<String, Station> stationByCode,
                                   Map<String, UUID> operatorIdByCode,
                                   Map<FareMode, FareRule> fareRuleByMode) {

        Set<String> cardUids = chunk.stream()
                .map(TransactionItemRequest::cardUid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, UUID> cardIdByUid = cardUids.isEmpty()
                ? Map.of()
                : cardRepository.findByCardUidIn(cardUids).stream()
                .collect(Collectors.toMap(Card::getCardUid, Card::getId));

        List<UUID> transactionIds = chunk.stream()
                .map(TransactionItemRequest::transactionId)
                .toList();

        Set<UUID> existingTransactionIds =
                tripRepository.findExistingExternalTransactionIds(transactionIds);

        Set<UUID> ticketIds = chunk.stream()
                .map(TransactionItemRequest::ticketId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Ticket> ticketById = ticketIds.isEmpty()
                ? Map.of()
                : ticketRepository.findAllByIds(ticketIds).stream()
                .collect(Collectors.toMap(Ticket::getId, t -> t));

        int success = 0, skipped = 0, failed = 0;
        List<String> errors = new ArrayList<>();
        List<Trip> tripsToSave = new ArrayList<>();
        List<Ticket> ticketsToUpdate = new ArrayList<>();
        List<TripAnomaly> anomaliesToSave = new ArrayList<>();

        for (TransactionItemRequest item : chunk) {
            try {
                if (existingTransactionIds.contains(item.transactionId())) {
                    skipped++;
                    continue;
                }

                Trip trip = buildTrip(item, stationByCode, operatorIdByCode, cardIdByUid);
                tripsToSave.add(trip);

                markTicketUsedIfApplicable(item, ticketById, ticketsToUpdate);
                checkFareMismatch(trip, item, stationByCode, fareRuleByMode, anomaliesToSave);

                success++;
            } catch (Exception e) {
                failed++;
                errors.add(item.transactionId() + ": " + e.getMessage());
                log.error("Failed to ingest {}: {}", item.transactionId(), e.getMessage());
            }
        }

        if (!tripsToSave.isEmpty()) tripRepository.saveAll(tripsToSave);
        if (!ticketsToUpdate.isEmpty()) ticketRepository.saveAll(ticketsToUpdate);
        if (!anomaliesToSave.isEmpty()) anomalyRepository.saveAll(anomaliesToSave);

        return new ChunkResult(success, skipped, failed, errors);
    }

    private Trip buildTrip(TransactionItemRequest item,
                           Map<String, Station> stationByCode,
                           Map<String, UUID> operatorIdByCode,
                           Map<String, UUID> cardIdByUid) {

        UUID tapInStationId = Optional.ofNullable(stationByCode.get(item.tapInStationCode()))
                .map(Station::getId).orElse(null);

        UUID tapOutStationId = item.tapOutStationCode() != null
                ? Optional.ofNullable(stationByCode.get(item.tapOutStationCode()))
                .map(Station::getId).orElse(null)
                : null;

        UUID operatorId = operatorIdByCode.get(item.operatorCode());

        UUID cardId = item.cardUid() != null
                ? cardIdByUid.get(item.cardUid())
                : null;

        return Trip.from(
                item.transactionId(),
                cardId,
                item.ticketId(),
                operatorId,
                tapInStationId,
                item.tapInAt(),
                tapOutStationId,
                item.tapOutAt(),
                item.distanceKm(),
                item.fareAmount(),
                item.mode(),
                item.ticketType()
        );
    }

    private void markTicketUsedIfApplicable(TransactionItemRequest item,
                                            Map<UUID, Ticket> ticketById,
                                            List<Ticket> ticketsToUpdate) {
        if (item.ticketId() == null) return;
        if (item.ticketType() != TicketTypeUsed.SINGLE_TRIP) return;

        Ticket ticket = ticketById.get(item.ticketId());
        if (ticket != null) {
            ticket.markUsed();
            ticketsToUpdate.add(ticket);
        }
    }

    private void checkFareMismatch(Trip trip,
                                   TransactionItemRequest item,
                                   Map<String, Station> stationByCode,
                                   Map<FareMode, FareRule> fareRuleByMode,
                                   List<TripAnomaly> anomaliesToSave) {
        if (item.fareAmount() == null) return;
        if (item.tapOutStationCode() == null) return;

        Station tapInStation = stationByCode.get(item.tapInStationCode());
        Station tapOutStation = stationByCode.get(item.tapOutStationCode());
        FareRule fareRule = fareRuleByMode.get(item.mode());

        if (tapInStation == null || tapOutStation == null || fareRule == null) return;

        BigDecimal expected = fareCalculationService.calculateRaw(
                tapInStation, tapOutStation, fareRule, item.distanceKm());

        com.metro.afc.trip.domain.service.FareMismatchDetector.detect(trip, expected)
                .ifPresent(anomaly -> {
                    anomaliesToSave.add(anomaly);
                    log.warn("FARE_MISMATCH: trip={}, description={}",
                            trip.getId(), anomaly.getDescription());
                });
    }

    private record ChunkResult(int success, int skipped, int failed, List<String> errors) {}

    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return result;
    }
}