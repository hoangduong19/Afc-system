package com.metro.afc.trip.infrastructure.adapter.in;

import com.metro.afc.trip.application.TransactionIngestionService;
import com.metro.afc.trip.application.dto.BatchIngestResponse;
import com.metro.afc.trip.application.dto.TransactionBatchRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionIngestionController {

    private final TransactionIngestionService ingestionService;

    @PostMapping("/batch")
    public ResponseEntity<BatchIngestResponse> ingest(
            @Valid @RequestBody TransactionBatchRequest request) {
        return ResponseEntity.ok(
                ingestionService.ingest(request.transactions())
        );
    }
}
