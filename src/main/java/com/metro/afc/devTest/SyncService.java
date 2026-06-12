package com.metro.afc.devTest;

import com.metro.afc.card.infrastructure.adapter.out.persistence.card.CardJpaRepository;
import com.metro.afc.devTest.message.CardSyncMessage;
import com.metro.afc.devTest.message.OperatorSyncMessage;
import com.metro.afc.devTest.message.TicketSyncMessage;
import com.metro.afc.operator.infrastructure.adapter.out.OperatorJpaRepository;
import com.metro.afc.shared.infrastructure.config.RabbitMQConfig;
import com.metro.afc.ticket.infrastructure.adapter.out.TicketJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncService {

    private final CardJpaRepository      cardJpaRepository;
    private final TicketJpaRepository    ticketJpaRepository;
    private final OperatorJpaRepository  operatorJpaRepository;
    private final AmqpTemplate           amqpTemplate;

    public void syncCards() {
        cardJpaRepository.findAll().stream()
                .map(CardSyncMessage::from)
                .forEach(msg -> amqpTemplate.convertAndSend(
                        RabbitMQConfig.AFC_EXCHANGE,
                        RabbitMQConfig.SYNC_CARD_ALL, msg));
    }

    public void syncTickets() {
        ticketJpaRepository.findAll().stream()
                .map(TicketSyncMessage::from)
                .forEach(msg -> amqpTemplate.convertAndSend(
                        RabbitMQConfig.AFC_EXCHANGE,
                        RabbitMQConfig.SYNC_TICKET_ALL, msg));
    }

    public void syncOperators() {
        operatorJpaRepository.findAll().stream()
                .map(OperatorSyncMessage::from)
                .forEach(msg -> amqpTemplate.convertAndSend(
                        RabbitMQConfig.AFC_EXCHANGE,
                        RabbitMQConfig.SYNC_OPERATOR_ALL, msg));
    }


    public void syncAll() {
        syncCards();
        syncTickets();
        syncOperators();
    }
}