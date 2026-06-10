package com.metro.afc.settlement.application.port.out;

import com.metro.afc.settlement.domain.RevenueShareRuleAuditLog;

public interface RevenueShareRuleAuditLogRepository {
    void save(RevenueShareRuleAuditLog auditLog);
}