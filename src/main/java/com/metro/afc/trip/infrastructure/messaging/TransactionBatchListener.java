package com.metro.afc.trip.infrastructure.messaging;

import com.metro.afc.shared.infrastructure.config.RabbitMQConfig;
import com.metro.afc.trip.application.TransactionIngestionService;
import com.metro.afc.trip.application.dto.BatchIngestResponse;
import com.metro.afc.trip.application.dto.ExternalTransactionBatchRequest;
import com.metro.afc.trip.application.dto.TransactionBatchRequest;
import com.metro.afc.trip.application.dto.TransactionItemRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionBatchListener {

    private final TransactionIngestionService ingestionService;

    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_BATCH_QUEUE)
    public void handle(ExternalTransactionBatchRequest request) {
        log.info("Received batch: {} transactions",
                request.transactions().size());

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

        BatchIngestResponse response = ingestionService.ingest(
                new TransactionBatchRequest(mapped));

        log.info("Batch processed: success={}, skipped={}, failed={}",
                response.success(), response.skipped(), response.failed());
    }
}