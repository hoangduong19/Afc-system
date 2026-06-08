package com.metro.afc.blacklist.infrastructure.messaging;

import com.metro.afc.blacklist.domain.events.BlacklistAddedEvent;
import com.metro.afc.blacklist.domain.events.BlacklistRemovedEvent;
import com.metro.afc.shared.infrastructure.config.RabbitMQConfig;
import com.metro.afc.shared.messaging.BlacklistMessage;
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
public class BlacklistEventPublisher {

    private final AmqpTemplate amqpTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(BlacklistAddedEvent event) {
        BlacklistMessage message = new BlacklistMessage(
                event.blacklistId(),
                event.cardId(),
                "ADDED",
                event.reason(),
                event.addedBy(),
                Instant.now()
        );
        try {
            amqpTemplate.convertAndSend(
                    RabbitMQConfig.AFC_EXCHANGE,
                    RabbitMQConfig.BLACKLIST_ADDED,
                    message
            );
            log.info("Published blacklist added: cardId={}", event.cardId());
        } catch (Exception e) {
            log.error("Failed to publish blacklist added: cardId={}", event.cardId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(BlacklistRemovedEvent event) {
        BlacklistMessage message = new BlacklistMessage(
                event.blacklistId(),
                event.cardId(),
                "REMOVED",
                null,
                event.removedBy(),
                Instant.now()
        );
        try {
            amqpTemplate.convertAndSend(
                    RabbitMQConfig.AFC_EXCHANGE,
                    RabbitMQConfig.BLACKLIST_REMOVED,
                    message
            );
            log.info("Published blacklist removed: cardId={}", event.cardId());
        } catch (Exception e) {
            log.error("Failed to publish blacklist removed: cardId={}", event.cardId(), e);
        }
    }
}
