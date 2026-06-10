package com.metro.afc.settlement.application.port.out;

import com.metro.afc.settlement.domain.RevenueShareRule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RevenueShareRuleRepository {
    RevenueShareRule save(RevenueShareRule rule);
    Optional<RevenueShareRule> findById(UUID id);
    Optional<RevenueShareRule> findActiveByOperatorId(UUID operatorId);
    List<RevenueShareRule> findAllByOperatorId(UUID operatorId);
    boolean existsActiveByOperatorId(UUID operatorId);
}