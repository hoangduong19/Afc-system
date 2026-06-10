package com.metro.afc.settlement.infrastructure.adapter.out.revenueShareRuleAuditLog;

import com.metro.afc.settlement.application.port.out.RevenueShareRuleAuditLogRepository;
import com.metro.afc.settlement.domain.RevenueShareRuleAuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevenueShareRuleAuditLogRepositoryImpl
        implements RevenueShareRuleAuditLogRepository {

    private final RevenueShareRuleAuditJpaRepository jpa;

    @Override
    public void save(RevenueShareRuleAuditLog auditLog) {
        jpa.save(auditLog);
    }
}