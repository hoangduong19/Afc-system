package com.metro.afc.trip.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metro.afc.operator.application.port.out.OperatorRepository;
import com.metro.afc.operator.domain.event.OperatorCreatedEvent;
import com.metro.afc.operator.domain.event.OperatorUpdatedEvent;
import com.metro.afc.operator.domain.model.Operator;
import com.metro.afc.station.application.port.out.StationRepository;
import com.metro.afc.station.domain.model.Station;
import com.metro.afc.trip.domain.events.StationCatalogChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NetworkCatalogService {

    private static final String STATIONS_KEY = "catalog:stations:active";
    private static final String OPERATORS_KEY = "catalog:operators:active";
    private static final Duration TTL = Duration.ofHours(24);

    private final StationRepository stationRepository;
    private final OperatorRepository operatorRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper redisObjectMapper;

    @SneakyThrows
    public Map<String, Station> getStationByCodeMap() {
        String cached = redisTemplate.opsForValue().get(STATIONS_KEY);
        if (cached != null) {
            return redisObjectMapper.readValue(cached, new TypeReference<Map<String, Station>>() {});
        }

        Map<String, Station> fresh = stationRepository.findAll().stream()
                .collect(Collectors.toMap(Station::getCode, s -> s));

        redisTemplate.opsForValue().set(STATIONS_KEY, redisObjectMapper.writeValueAsString(fresh), TTL);
        return fresh;
    }

    @SneakyThrows
    public Map<String, UUID> getOperatorIdByCodeMap() {
        String cached = redisTemplate.opsForValue().get(OPERATORS_KEY);
        if (cached != null) {
            return redisObjectMapper.readValue(cached, new TypeReference<Map<String, UUID>>() {});
        }

        Map<String, UUID> fresh = operatorRepository.findAll().stream()
                .collect(Collectors.toMap(Operator::getCode, Operator::getId));

        redisTemplate.opsForValue().set(OPERATORS_KEY, redisObjectMapper.writeValueAsString(fresh), TTL);
        return fresh;
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStationCatalogChanged(StationCatalogChangedEvent event) {
        log.info("Received StationCatalogChangedEvent. Evicting stations cache.");
        redisTemplate.delete(STATIONS_KEY);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOperatorCreated(OperatorCreatedEvent event) {
        evictOperatorsCache();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOperatorUpdated(OperatorUpdatedEvent event) {
        evictOperatorsCache();
    }

    public void evictStationsCache() {
        redisTemplate.delete(STATIONS_KEY);
    }

    public void evictOperatorsCache() {
        redisTemplate.delete(OPERATORS_KEY);
    }

    public void evictAll() {
        redisTemplate.delete(List.of(STATIONS_KEY, OPERATORS_KEY));
    }
}