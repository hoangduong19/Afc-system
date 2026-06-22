package com.metro.afc.operator.infrastructure.messaging;

import com.metro.afc.operator.domain.event.OperatorCreatedEvent;
import com.metro.afc.operator.domain.event.OperatorUpdatedEvent;
import com.metro.afc.operator.domain.model.Operator;
import com.metro.afc.shared.infrastructure.config.RabbitMQConfig;
import com.metro.afc.shared.messaging.OperatorMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OperatorEventPublisher {

    private final AmqpTemplate amqpTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OperatorCreatedEvent event) {
        Operator o = event.operator();
        publish(toMessage(o), RabbitMQConfig.OPERATOR_CREATED, o.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OperatorUpdatedEvent event) {
        Operator o = event.operator();
        publish(toMessage(o), RabbitMQConfig.OPERATOR_UPDATED, o.getId());
    }

    private OperatorMessage toMessage(Operator o) {
        return new OperatorMessage(
                o.getId(), o.getCode(), o.getName(),
                o.getStatus(), o.getMode()
        );
    }

    private void publish(OperatorMessage message, String routingKey, UUID id) {
        try {
            amqpTemplate.convertAndSend(
                    RabbitMQConfig.AFC_EXCHANGE,
                    routingKey,
                    message
            );
        } catch (Exception e) {
            log.error("Failed to publish operator event: id={}", id, e);
        }
    }
}
