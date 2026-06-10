package com.metro.afc.ticket.infrastructure.messaging;

import com.metro.afc.shared.infrastructure.config.RabbitMQConfig;
import com.metro.afc.shared.messaging.TicketMessage;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import com.metro.afc.ticket.domain.Ticket;
import com.metro.afc.ticket.domain.events.TicketCreatedEvent;
import com.metro.afc.ticket.domain.events.TicketLinkedToCardEvent;
import com.metro.afc.ticket.domain.events.TicketUnlinkedFromCardEvent;
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
                t.getScope() != null ? t.getScope().name() : null,
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TicketLinkedToCardEvent event) {
        Ticket t = event.ticket();

        TicketMessage message = new TicketMessage(
                t.getId(), t.getType().name(), t.getMode().name(),
                t.getScope() != null ? t.getScope().name() : null,
                t.getCardId(), t.getUserId(),
                null, null,
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
            log.info("Published ticket linked to card: ticketId={}, cardId={}",
                    t.getId(), t.getCardId());
        } catch (Exception e) {
            log.error("Failed to publish ticket linked: ticketId={}", t.getId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TicketUnlinkedFromCardEvent event) {
        try {
            amqpTemplate.convertAndSend(
                    RabbitMQConfig.AFC_EXCHANGE,
                    "ticket.unlinked",
                    event
            );
            log.info("Published ticket unlinked: ticketId={}, cardId={}",
                    event.ticketId(), event.cardId());
        } catch (Exception e) {
            log.error("Failed to publish ticket unlinked: ticketId={}",
                    event.ticketId(), e);
        }
    }
}