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