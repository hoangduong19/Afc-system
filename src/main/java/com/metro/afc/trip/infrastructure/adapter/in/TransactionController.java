package com.metro.afc.trip.infrastructure.adapter.in;

import com.metro.afc.card.application.port.out.CardRepository;
import com.metro.afc.card.domain.model.Card;
import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.operator.domain.model.Operator;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.shared.infrastructure.exception.NotFoundException;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import com.metro.afc.trip.application.dto.TripResponse;
import com.metro.afc.trip.application.port.out.TripRepository;
import com.metro.afc.trip.domain.Trip;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TripRepository tripRepository;
    private final CardRepository cardRepository;
    private final OperatorRepository operatorRepository;
    private final StationRepository stationRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('TRIP_READ')")
    public ResponseEntity<Page<TripResponse>> findAll(
            @RequestParam(required = false) String cardUid,
            @RequestParam(required = false) String operatorCode,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant toDate,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID cardId = cardUid != null
                ? cardRepository.findByCardUid(cardUid.toUpperCase())
                .map(Card::getId).orElse(null)
                : null;

        UUID operatorId = operatorCode != null
                ? operatorRepository.findByCode(operatorCode.toUpperCase())
                .map(Operator::getId).orElse(null)
                : null;

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("tapInAt").descending());

        Page<TripResponse> result = tripRepository
                .findWithFilters(cardId, operatorId, fromDate, toDate, pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIP_READ')")
    public ResponseEntity<TripResponse> findById(@PathVariable UUID id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.TRIP_NOT_FOUND));
        return ResponseEntity.ok(toResponse(trip));
    }

    private TripResponse toResponse(Trip trip) {
        String cardUid = trip.getCardId() != null
                ? cardRepository.findById(trip.getCardId())
                .map(Card::getCardUid).orElse(null)
                : null;
        String operatorCode = trip.getOperatorId() != null
                ? operatorRepository.findById(trip.getOperatorId())
                .map(Operator::getCode).orElse(null)
                : null;
        String tapInCode = trip.getTapInStationId() != null
                ? stationRepository.findById(trip.getTapInStationId())
                .map(Station::getCode).orElse(null)
                : null;
        String tapOutCode = trip.getTapOutStationId() != null
                ? stationRepository.findById(trip.getTapOutStationId())
                .map(Station::getCode).orElse(null)
                : null;

        return new TripResponse(
                trip.getId(),
                trip.getExternalTransactionId(),
                trip.getCardId(), cardUid,
                trip.getOperatorId(), operatorCode,
                tapInCode, trip.getTapInAt(),
                tapOutCode, trip.getTapOutAt(),
                trip.getDistanceKm(), trip.getFareAmount(),
                trip.getTicketTypeUsed() != null
                        ? trip.getTicketTypeUsed().name() : null,
                trip.getCreatedAt()
        );
    }
}