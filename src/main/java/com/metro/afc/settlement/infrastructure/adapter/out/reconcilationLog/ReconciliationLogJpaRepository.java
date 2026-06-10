package com.metro.afc.settlement.infrastructure.adapter.out.reconcilationLog;

import com.metro.afc.settlement.domain.ReconciliationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReconciliationLogJpaRepository
        extends JpaRepository<ReconciliationLog, UUID> {
    List<ReconciliationLog> findBySettlementId(UUID settlementId);
}