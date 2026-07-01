package com.metro.afc.trip.benchmark;

import com.metro.afc.trip.application.TransactionIngestionService;
import com.metro.afc.trip.application.dto.BatchIngestResponse;
import com.metro.afc.trip.application.dto.TransactionBatchRequest;
import com.metro.afc.trip.application.dto.TransactionItemRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

@Slf4j
@Testcontainers
@SpringBootTest
class TransactionIngestionBenchmarkTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("afc_bench")
                    .withUsername("test")
                    .withPassword("test");

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    @Container
    static RabbitMQContainer rabbitmq =
            new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);

        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        registry.add("spring.jpa.properties.hibernate.jdbc.batch_size", () -> "500");
        registry.add("spring.jpa.properties.hibernate.order_inserts", () -> "true");
        registry.add("spring.jpa.properties.hibernate.order_updates", () -> "true");
    }

    @Autowired
    private TransactionIngestionService ingestionService;
    @Autowired
    private TestDataSeeder seeder;

    private List<String> stationCodes;
    private List<String> operatorCodes;
    private List<String> cardUids;

    @BeforeEach
    void setup() {
        UUID routeId = seeder.seedOperatorAndRoute();
        stationCodes = seeder.seedStations(30, routeId);
        operatorCodes = seeder.seedOperators(3);
        cardUids = seeder.seedCards(50_000);
        seeder.seedFareRules();
    }

    @Test
    void benchmark_100k() { runBenchmark(100_000); }

    @Test
    void benchmark_500k() { runBenchmark(500_000); }

    @Test
    void benchmark_1M() { runBenchmark(1_000_000); }

    private void runBenchmark(int count) {
        List<TransactionItemRequest> items =
                FakeTransactionGenerator.generate(count, stationCodes, operatorCodes, cardUids);

        long start = System.nanoTime();
        BatchIngestResponse res = ingestionService.ingest(new TransactionBatchRequest(items));
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        double throughput = res.success() * 1000.0 / elapsedMs;

        log.info("=== BENCHMARK N={} ===", count);
        log.info("Elapsed: {} ms | success={}, skipped={}, failed={}",
                elapsedMs, res.success(), res.skipped(), res.failed());
        log.info("Throughput: {} txn/s", String.format("%.2f", throughput));

        Assertions.assertEquals(0, res.failed());
    }
}