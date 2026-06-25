package com.metro.afc.fare.infrastructure.adapter.out.farerule;

import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRule.FareStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FareRuleRepositoryImpl implements FareRuleRepository {

    private final FareRuleJpaRepository jpa;

    @Override
    public Optional<FareRule> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<FareRule> findActiveByCode(String code) {
        return jpa.findByCodeAndStatus(code, FareStatus.ACTIVE);
    }

    @Override
    public Optional<FareRule> findActiveByMode(FareMode mode) {
        return jpa.findByModeAndStatus(mode, FareStatus.ACTIVE);
    }

    @Override
    public List<FareRule> findAllActive() {
        return jpa.findAllByStatus(FareStatus.ACTIVE);
    }

    @Override
    public List<FareRule> findAll() {
        return jpa.findAll();
    }

    @Override
    public boolean existsByCodeAndStatus(String code, FareStatus status) {
        return jpa.existsByCodeAndStatus(code, status);
    }

    @Override
    public List<FareRule> findActiveAtDate(LocalDate date) {
        return jpa.findActiveAtDate(date);
    }

    @Override
    public FareRule save(FareRule fareRule) {
        return jpa.save(fareRule);
    }
}