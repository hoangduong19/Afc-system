package com.metro.afc.ticket.infrastructure.messaging;

import com.metro.afc.shared.infrastructure.config.RabbitMQConfig;
import com.metro.afc.shared.messaging.TicketMessage;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.events.TicketCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventPublisher {

    private final AmqpTemplate      amqpTemplate;
    private final StationRepository stationRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TicketCreatedEvent event) {
        Ticket t = event.ticket();

        String fromCode = t.getFromStationId() != null
                ? stationRepository.findById(t.getFromStationId())
                .map(Station::getCode).orElse("") : null;
        String toCode = t.getToStationId() != null
                ? stationRepository.findById(t.getToStationId())
                .map(Station::getCode).orElse("") : null;

        TicketMessage message = new TicketMessage(
                t.getId(), t.getType().name(), t.getMode().name(),
                t.getCardId(), t.getUserId(),
                fromCode, toCode,
                t.getPrice().getAmount(),
                t.getValidFrom(), t.getValidTo(),
                t.getPurchasedAt()
        );

        try {
            amqpTemplate.convertAndSend(
                    RabbitMQConfig.AFC_EXCHANGE,
                    RabbitMQConfig.TICKET_CREATED,
                    message
            );
            log.info("Published ticket: id={}, type={}", t.getId(), t.getType());
        } catch (Exception e) {
            log.error("Failed to publish ticket: id={}", t.getId(), e);
        }
    }
}