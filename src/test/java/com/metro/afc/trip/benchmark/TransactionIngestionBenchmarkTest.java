package com.metro.afc.trip.benchmark;

import com.metro.afc.trip.application.TransactionIngestionService;
import com.metro.afc.trip.application.dto.BatchIngestResponse;
import com.metro.afc.trip.application.dto.TransactionBatchRequest;
import com.metro.afc.trip.application.dto.TransactionItemRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Testcontainers
@SpringBootTest
class TransactionIngestionBenchmarkTest {
    private static final List<String> summaryRows = new ArrayList<>();
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

        // Ghi đè spring.data.redis.url thay vì host/port riêng lẻ,
        // vì application.yml dùng "url:" nên nó luôn được ưu tiên hơn host/port.
        registry.add("spring.data.redis.url",
                () -> "redis://" + redis.getHost() + ":" + redis.getMappedPort(6379));

        registry.add("spring.rabbitmq.addresses",
                () -> rabbitmq.getHost() + ":" + rabbitmq.getAmqpPort());
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

    // static: dữ liệu seed được chia sẻ giữa các instance test (mỗi @Test 1 instance ở PER_METHOD)
    private static volatile boolean seeded = false;
    private static List<String> stationCodes;
    private static List<String> operatorCodes;
    private static List<String> cardUids;

    // Chạy trước MỖI test method (đúng behavior mặc định của JUnit5),
    // nhưng chỉ thực sự seed dữ liệu 1 LẦN DUY NHẤT nhờ cờ static "seeded".
    // Cách này tránh được lỗi "Mapped port..." vì không đụng tới @TestInstance(PER_CLASS).
    @BeforeEach
    synchronized void setup() {
        if (!seeded) {
            UUID routeId = seeder.seedOperatorAndRoute();
            stationCodes = seeder.seedStations(30, routeId);
            operatorCodes = seeder.seedOperators(3);
            cardUids = seeder.seedCards(50_000);
            seeder.seedFareRules();
            seeded = true;
        }
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

        summaryRows.add(String.format("| %,10d | %8d ms | %10.2f txn/s | success=%d skipped=%d failed=%d |",
                count, elapsedMs, throughput, res.success(), res.skipped(), res.failed()));

        Assertions.assertEquals(0, res.failed());
    }

    @AfterAll
    static void printSummary() {
        log.info("\n========== BENCHMARK SUMMARY ==========\n" +
                "| N (txn)    | Elapsed    | Throughput      | Result                        |\n" +
                "|------------|------------|------------------|-------------------------------|\n" +
                String.join("\n", summaryRows) +
                "\n========================================");
    }
}