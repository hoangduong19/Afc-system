package com.metro.afc.fare.infrastructure.adapter.out.fareRuleAuditLog;

import com.metro.afc.fare.domain.model.FareRuleAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FareRuleAuditLogJpaRepository
        extends JpaRepository<FareRuleAuditLog, UUID> {
    List<FareRuleAuditLog> findByFareRuleId(UUID fareRuleId);
}