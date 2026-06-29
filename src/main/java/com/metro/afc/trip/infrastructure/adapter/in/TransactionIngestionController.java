package com.metro.afc.trip.infrastructure.adapter.in;

import com.metro.afc.trip.application.TransactionIngestionService;
import com.metro.afc.trip.application.dto.BatchIngestResponse;
import com.metro.afc.trip.application.dto.ExternalTransactionBatchRequest;
import com.metro.afc.trip.application.dto.TransactionBatchRequest;
import com.metro.afc.trip.application.dto.TransactionItemRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionIngestionController {

    private final TransactionIngestionService ingestionService;

    @PostMapping("/batch")
    public ResponseEntity<BatchIngestResponse> ingest(
            @Valid @RequestBody ExternalTransactionBatchRequest request) {

        List<TransactionItemRequest> mapped = request.transactions()
                .stream()
                .map(item -> new TransactionItemRequest(
                        item.transactionId(),
                        item.cardUid(),
                        item.ticketId(),
                        item.operatorCode(),
                        item.lineCode(),
                        item.tapInStationCode(),
                        item.tapInAt(),
                        item.tapOutStationCode(),
                        item.tapOutAt(),
                        item.distanceKm(),
                        item.fareAmount(),
                        item.mode(),
                        item.ticketType()
                )).toList();

        return ResponseEntity.ok(
                ingestionService.ingest(new TransactionBatchRequest(mapped)));
    }
}