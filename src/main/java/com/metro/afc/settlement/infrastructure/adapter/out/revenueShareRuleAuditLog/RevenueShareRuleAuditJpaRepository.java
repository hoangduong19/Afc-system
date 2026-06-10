package com.metro.afc.settlement.infrastructure.adapter.out.revenueShareRuleAuditLog;

import com.metro.afc.settlement.domain.RevenueShareRuleAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RevenueShareRuleAuditJpaRepository
        extends JpaRepository<RevenueShareRuleAuditLog, UUID> {
}