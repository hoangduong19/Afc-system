package com.metro.afc.trip.dlq;

import com.metro.afc.shared.infrastructure.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test độc lập verify hạ tầng DLQ + retry của transaction.batch.queue,
 * KHÔNG đụng tới TransactionIngestionService thật — tránh phải sửa code tay mỗi lần.
 */
@Testcontainers
class TransactionBatchDlqTest {

    @Container
    static RabbitMQContainer rabbitmq =
            new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Test
    void systemError_shouldRetry3Times_thenLandInDlq() throws InterruptedException {
        CachingConnectionFactory connectionFactory =
                new CachingConnectionFactory(rabbitmq.getHost(), rabbitmq.getAmqpPort());
        connectionFactory.setUsername(rabbitmq.getAdminUsername());
        connectionFactory.setPassword(rabbitmq.getAdminPassword());

        RabbitAdmin admin = new RabbitAdmin(connectionFactory);

        // Khai báo topology GIỐNG HỆT RabbitMQConfig thật
        admin.declareExchange(new org.springframework.amqp.core.TopicExchange(RabbitMQConfig.AFC_EXCHANGE));
        admin.declareExchange(new org.springframework.amqp.core.DirectExchange(RabbitMQConfig.TRANSACTION_BATCH_DLX));

        org.springframework.amqp.core.Queue queue = org.springframework.amqp.core.QueueBuilder
                .durable(RabbitMQConfig.TRANSACTION_BATCH_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQConfig.TRANSACTION_BATCH_DLX)
                .withArgument("x-dead-letter-routing-key", RabbitMQConfig.TRANSACTION_BATCH_DLQ_ROUTING)
                .build();
        admin.declareQueue(queue);

        org.springframework.amqp.core.Queue dlq = org.springframework.amqp.core.QueueBuilder
                .durable(RabbitMQConfig.TRANSACTION_BATCH_DLQ).build();
        admin.declareQueue(dlq);

        admin.declareBinding(org.springframework.amqp.core.BindingBuilder
                .bind(queue).to(new org.springframework.amqp.core.TopicExchange(RabbitMQConfig.AFC_EXCHANGE))
                .with(RabbitMQConfig.TRANSACTION_BATCH_KEY));

        admin.declareBinding(org.springframework.amqp.core.BindingBuilder
                .bind(dlq).to(new org.springframework.amqp.core.DirectExchange(RabbitMQConfig.TRANSACTION_BATCH_DLX))
                .with(RabbitMQConfig.TRANSACTION_BATCH_DLQ_ROUTING));

        // Retry interceptor GIỐNG application.yml: max-attempts=3, initial=2000, multiplier=2.0
        AtomicInteger attemptCount = new AtomicInteger(0);
        CountDownLatch dlqLatch = new CountDownLatch(1);

        RetryOperationsInterceptor retryInterceptor = RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(2000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer()) // tương đương default-requeue-rejected: false
                .build();

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(RabbitMQConfig.TRANSACTION_BATCH_QUEUE);
        container.setAdviceChain(retryInterceptor);
        container.setMessageListener((MessageListener) message -> {
            attemptCount.incrementAndGet();
            throw new RuntimeException("SIMULATED SYSTEM ERROR - test DLQ");
        });
        container.start();

        // Listener phụ để bắt message khi nó rơi vào DLQ
        SimpleMessageListenerContainer dlqContainer = new SimpleMessageListenerContainer(connectionFactory);
        dlqContainer.setQueueNames(RabbitMQConfig.TRANSACTION_BATCH_DLQ);
        dlqContainer.setMessageListener((MessageListener) message -> dlqLatch.countDown());
        dlqContainer.start();

        // Publish 1 message giả
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new SimpleMessageConverter());
        template.convertAndSend(RabbitMQConfig.AFC_EXCHANGE, RabbitMQConfig.TRANSACTION_BATCH_KEY,
                "{\"transactions\":[]}");

        // Tổng thời gian tối đa: 2s + 4s + 8s = 14s, cho dư thời gian buffer
        boolean landedInDlq = dlqLatch.await(20, TimeUnit.SECONDS);

        container.stop();
        dlqContainer.stop();
        connectionFactory.destroy();

        assertTrue(landedInDlq, "Message phải xuất hiện trong DLQ sau khi retry hết 3 lần");
        assertEquals(3, attemptCount.get(), "Listener phải được gọi đúng 3 lần trước khi bị reject");
    }
}