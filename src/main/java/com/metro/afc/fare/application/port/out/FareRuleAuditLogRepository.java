package com.metro.afc.fare.application.port.out;

import com.metro.afc.fare.domain.model.FareRuleAuditLog;

import java.util.List;
import java.util.UUID;

public interface FareRuleAuditLogRepository {
    FareRuleAuditLog save(FareRuleAuditLog log);
    List<FareRuleAuditLog> findByFareRuleId(UUID fareRuleId);
}
