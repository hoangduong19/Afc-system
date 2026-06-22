package com.metro.afc.shared.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String AFC_EXCHANGE = "afc.exchange";

    // Routing keys
    public static final String CARD_STATUS_CHANGED  = "card.status.changed";
    public static final String FARE_RULE_UPDATED    = "fare.rule.updated";
    public static final String FARE_RULE_DISABLED   = "fare.rule.disabled";

    // Queues
    public static final String CARD_STATUS_QUEUE    = "card.status.queue";
    public static final String FARE_RULE_QUEUE      = "fare.rule.queue";

    // Blacklist
    public static final String BLACKLIST_ADDED   = "blacklist.added";
    public static final String BLACKLIST_REMOVED = "blacklist.removed";
    public static final String BLACKLIST_QUEUE   = "blacklist.queue";

    // Ticket
    public static final String TICKET_CREATED = "ticket.created";
    public static final String TICKET_QUEUE   = "ticket.queue";
    public static final String TICKET_UNLINKED       = "ticket.unlinked";
    public static final String TICKET_UNLINKED_QUEUE = "ticket.unlinked.queue";

    // Settlement
    public static final String SETTLEMENT_CONFIRMED       = "settlement.confirmed";
    public static final String SETTLEMENT_CONFIRMED_QUEUE = "settlement.confirmed.queue";

    // Station & Route
    public static final String STATION_SYNCED = "station.synced";
    public static final String ROUTE_SYNCED   = "route.synced";
    public static final String STATION_SYNC_QUEUE = "station.sync.queue";
    public static final String ROUTE_SYNC_QUEUE   = "route.sync.queue";

    // Operator
    public static final String OPERATOR_CREATED = "operator.created";
    public static final String OPERATOR_UPDATED = "operator.updated";
    public static final String OPERATOR_EVENT_QUEUE = "operator.event.queue";

    // Dev Test
    public static final String SYNC_CARD_ALL     = "sync.card.all";
    public static final String SYNC_TICKET_ALL   = "sync.ticket.all";
    public static final String SYNC_OPERATOR_ALL = "sync.operator.all";

    public static final String SYNC_CARD_QUEUE     = "sync.card.queue";
    public static final String SYNC_TICKET_QUEUE   = "sync.ticket.queue";
    public static final String SYNC_OPERATOR_QUEUE = "sync.operator.queue";

    @Bean
    public Queue syncCardQueue() {
        return QueueBuilder.durable(SYNC_CARD_QUEUE).build();
    }

    @Bean
    public Queue syncTicketQueue() {
        return QueueBuilder.durable(SYNC_TICKET_QUEUE).build();
    }

    @Bean
    public Queue syncOperatorQueue() {
        return QueueBuilder.durable(SYNC_OPERATOR_QUEUE).build();
    }

    @Bean
    public Binding syncCardBinding() {
        return BindingBuilder.bind(syncCardQueue())
                .to(afcExchange()).with(SYNC_CARD_ALL);
    }

    @Bean
    public Binding syncTicketBinding() {
        return BindingBuilder.bind(syncTicketQueue())
                .to(afcExchange()).with(SYNC_TICKET_ALL);
    }

    @Bean
    public Binding syncOperatorBinding() {
        return BindingBuilder.bind(syncOperatorQueue())
                .to(afcExchange()).with(SYNC_OPERATOR_ALL);
    }

    // ////

    @Bean
    public TopicExchange afcExchange() {
        return new TopicExchange(AFC_EXCHANGE);
    }

    @Bean
    public Queue cardStatusQueue() {
        return QueueBuilder.durable(CARD_STATUS_QUEUE).build();
    }

    @Bean
    public Queue blacklistQueue() {
        return QueueBuilder.durable(BLACKLIST_QUEUE).build();
    }


    @Bean
    public Queue fareRuleQueue() {
        return QueueBuilder.durable(FARE_RULE_QUEUE).build();
    }

    @Bean public Queue ticketQueue() {
        return QueueBuilder.durable(TICKET_QUEUE).build();
    }

    @Bean
    public Queue ticketUnlinkedQueue() {
        return QueueBuilder.durable(TICKET_UNLINKED_QUEUE).build();
    }

    @Bean
    public Queue settlementConfirmedQueue() {
        return QueueBuilder.durable(SETTLEMENT_CONFIRMED_QUEUE).build();
    }

    @Bean public Queue stationSyncQueue() {
        return QueueBuilder.durable(STATION_SYNC_QUEUE).build();
    }

    @Bean public Queue routeSyncQueue() {
        return QueueBuilder.durable(ROUTE_SYNC_QUEUE).build();
    }

    @Bean
    public Queue operatorEventQueue() {
        return QueueBuilder.durable(OPERATOR_EVENT_QUEUE).build();
    }


    @Bean
    public Binding cardStatusBinding() {
        return BindingBuilder
                .bind(cardStatusQueue())
                .to(afcExchange())
                .with(CARD_STATUS_CHANGED);
    }

    @Bean
    public Binding fareRuleBinding() {
        return BindingBuilder
                .bind(fareRuleQueue())
                .to(afcExchange())
                .with("fare.rule.*");
    }

    @Bean
    public Binding blacklistBinding() {
        return BindingBuilder
                .bind(blacklistQueue())
                .to(afcExchange())
                .with("blacklist.*");
    }

    @Bean public Binding ticketBinding() {
        return BindingBuilder.bind(ticketQueue())
                .to(afcExchange()).with(TICKET_CREATED);
    }

    @Bean
    public Binding ticketUnlinkedBinding() {
        return BindingBuilder
                .bind(ticketUnlinkedQueue())
                .to(afcExchange())
                .with(TICKET_UNLINKED);
    }

    @Bean
    public Binding settlementConfirmedBinding() {
        return BindingBuilder
                .bind(settlementConfirmedQueue())
                .to(afcExchange())
                .with(SETTLEMENT_CONFIRMED);
    }

    @Bean public Binding stationSyncBinding() {
        return BindingBuilder.bind(stationSyncQueue())
                .to(afcExchange()).with(STATION_SYNCED);
    }

    @Bean public Binding routeSyncBinding() {
        return BindingBuilder.bind(routeSyncQueue())
                .to(afcExchange()).with(ROUTE_SYNCED);
    }

    @Bean
    public Binding operatorEventBinding() {
        return BindingBuilder
                .bind(operatorEventQueue())
                .to(afcExchange())
                .with("operator.*");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}