package com.metro.afc.card.infrastructure.adapter.out.messaging;

import com.metro.afc.card.domain.events.CardStatusChangedEvent;
import com.metro.afc.shared.infrastructure.config.RabbitMQConfig;
import com.metro.afc.shared.messaging.CardStatusMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardEventPublisher {

    private final AmqpTemplate amqpTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(CardStatusChangedEvent event) {
        CardStatusMessage message = new CardStatusMessage(
                event.cardId(),
                null,
                event.fromStatus() != null ? event.fromStatus().name() : null,
                event.toStatus().name(),
                event.reason(),
                event.changedBy(),
                Instant.now()
        );

        try {
            amqpTemplate.convertAndSend(
                    RabbitMQConfig.AFC_EXCHANGE,
                    RabbitMQConfig.CARD_STATUS_CHANGED,
                    message
            );
            log.info("Published card status event: cardId={}, status={}",
                    event.cardId(), event.toStatus());
        } catch (Exception e) {
            log.error("Failed to publish card status event: cardId={}",
                    event.cardId(), e);
        }
    }
}