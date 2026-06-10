package com.metro.afc.settlement.infrastructure.adapter.out.revenueShareRule;

import com.metro.afc.settlement.application.port.out.RevenueShareRuleRepository;
import com.metro.afc.settlement.domain.RevenueShareRule;
import com.metro.afc.settlement.domain.enums.RuleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RevenueShareRuleRepositoryImpl implements RevenueShareRuleRepository {

    private final RevenueShareRuleJpaRepository jpa;

    @Override
    public RevenueShareRule save(RevenueShareRule rule) {
        return jpa.save(rule);
    }

    @Override
    public Optional<RevenueShareRule> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<RevenueShareRule> findActiveByOperatorId(UUID operatorId) {
        return jpa.findByOperatorIdAndStatus(operatorId, RuleStatus.ACTIVE);
    }

    @Override
    public List<RevenueShareRule> findAllByOperatorId(UUID operatorId) {
        return jpa.findAllByOperatorId(operatorId);
    }

    @Override
    public boolean existsActiveByOperatorId(UUID operatorId) {
        return jpa.existsByOperatorIdAndStatus(operatorId, RuleStatus.ACTIVE);
    }
}