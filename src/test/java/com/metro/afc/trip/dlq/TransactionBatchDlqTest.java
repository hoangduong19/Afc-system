package com.metro.afc.trip.dlq;

import com.metro.afc.shared.infrastructure.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

        AtomicInteger attemptCount = new AtomicInteger(0);
        CountDownLatch dlqLatch = new CountDownLatch(1);

        RetryOperationsInterceptor retryInterceptor = RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(2000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(RabbitMQConfig.TRANSACTION_BATCH_QUEUE);
        container.setAdviceChain(retryInterceptor);
        container.setMessageListener((MessageListener) message -> {
            int attempt = attemptCount.incrementAndGet();
            // THÊM LOG Ở ĐÂY: in ra mỗi lần listener bị gọi lại (retry)
            log.warn(">>> [RETRY {}/3] Nhận message trên transaction.batch.queue, giả lập lỗi hệ thống...", attempt);
            throw new RuntimeException("SIMULATED SYSTEM ERROR - test DLQ");
        });
        container.start();

        SimpleMessageListenerContainer dlqContainer = new SimpleMessageListenerContainer(connectionFactory);
        dlqContainer.setQueueNames(RabbitMQConfig.TRANSACTION_BATCH_DLQ);
        dlqContainer.setMessageListener((MessageListener) message -> {
            // THÊM LOG Ở ĐÂY: xác nhận message đã rơi vào DLQ, kèm số lần đã retry trước đó
            log.info(">>> [DLQ] Message đã bị chuyển vào {} sau {} lần retry thất bại",
                    RabbitMQConfig.TRANSACTION_BATCH_DLQ, attemptCount.get());
            dlqLatch.countDown();
        });
        dlqContainer.start();

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new SimpleMessageConverter());
        log.info(">>> Publish message giả lên {} với routing key {}",
                RabbitMQConfig.AFC_EXCHANGE, RabbitMQConfig.TRANSACTION_BATCH_KEY);
        template.convertAndSend(RabbitMQConfig.AFC_EXCHANGE, RabbitMQConfig.TRANSACTION_BATCH_KEY,
                "{\"transactions\":[]}");

        boolean landedInDlq = dlqLatch.await(20, TimeUnit.SECONDS);

        container.stop();
        dlqContainer.stop();
        connectionFactory.destroy();

        assertTrue(landedInDlq, "Message phải xuất hiện trong DLQ sau khi retry hết 3 lần");
        assertEquals(3, attemptCount.get(), "Listener phải được gọi đúng 3 lần trước khi bị reject");
    }
}