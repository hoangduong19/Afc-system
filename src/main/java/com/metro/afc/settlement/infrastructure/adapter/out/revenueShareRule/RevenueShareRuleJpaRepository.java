package com.metro.afc.settlement.infrastructure.adapter.out.revenueShareRule;

import com.metro.afc.settlement.domain.RevenueShareRule;
import com.metro.afc.settlement.domain.enums.RuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RevenueShareRuleJpaRepository
        extends JpaRepository<RevenueShareRule, UUID> {

    Optional<RevenueShareRule> findByOperatorIdAndStatus(
            UUID operatorId, RuleStatus status);

    List<RevenueShareRule> findAllByOperatorId(UUID operatorId);

    boolean existsByOperatorIdAndStatus(UUID operatorId, RuleStatus status);
}