package com.metro.afc.fare.infrastructure.messaging;

import com.metro.afc.fare.domain.event.fareRule.FareRuleDisabledEvent;
import com.metro.afc.fare.domain.event.fareRule.FareRuleUpdatedEvent;
import com.metro.afc.shared.infrastructure.config.RabbitMQConfig;
import com.metro.afc.shared.messaging.FareRuleMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class FareRuleEventPublisher {

    private final AmqpTemplate amqpTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FareRuleUpdatedEvent event) {
        publish(event.newFareRuleId(), "UPDATED",
                event.reason(), event.updatedBy());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FareRuleDisabledEvent event) {
        publish(event.fareRuleId(), "DISABLED",
                event.reason(), event.disabledBy());
    }

    private void publish(UUID fareRuleId, String changeType,
                         String reason, UUID changedBy) {
        FareRuleMessage message = new FareRuleMessage(
                fareRuleId, null, changeType, reason, changedBy, Instant.now()
        );

        String routingKey = changeType.equals("UPDATED")
                ? RabbitMQConfig.FARE_RULE_UPDATED
                : RabbitMQConfig.FARE_RULE_DISABLED;

        try {
            amqpTemplate.convertAndSend(
                    RabbitMQConfig.AFC_EXCHANGE,
                    routingKey,
                    message
            );
            log.info("Published fare rule event: id={}, type={}",
                    fareRuleId, changeType);
        } catch (Exception e) {
            log.error("Failed to publish fare rule event: id={}",
                    fareRuleId, e);
        }
    }
}