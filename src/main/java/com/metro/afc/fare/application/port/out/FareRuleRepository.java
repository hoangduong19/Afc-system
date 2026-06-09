package com.metro.afc.fare.application.port.out;

import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.fare.domain.model.enums.fareRule.FareStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FareRuleRepository {
    Optional<FareRule> findById(UUID id);
    Optional<FareRule> findActiveByCode(String code);
    Optional<FareRule> findActiveByMode(FareMode mode);
    List<FareRule> findAllActive();
    List<FareRule> findAll();
    boolean existsByCodeAndStatus(String code, FareStatus status);
    FareRule save(FareRule fareRule);
}
