package com.metro.afc.fare.infrastructure.adapter.out.fareRuleAuditLog;

import com.metro.afc.fare.application.port.out.FareRuleAuditLogRepository;
import com.metro.afc.fare.domain.model.FareRuleAuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FareRuleAuditLogRepositoryImpl implements FareRuleAuditLogRepository {

    private final FareRuleAuditLogJpaRepository jpa;

    @Override
    public FareRuleAuditLog save(FareRuleAuditLog log) {
        return jpa.save(log);
    }

    @Override
    public List<FareRuleAuditLog> findByFareRuleId(UUID fareRuleId) {
        return jpa.findByFareRuleId(fareRuleId);
    }
}