package com.metro.afc.settlement.application.port.out;

import com.metro.afc.settlement.domain.CompanyShare;
import com.metro.afc.settlement.domain.ReconciliationLog;
import com.metro.afc.settlement.domain.Settlement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepository {
    Settlement save(Settlement settlement);
    CompanyShare saveShare(CompanyShare share);
    ReconciliationLog saveLog(ReconciliationLog log);
    Optional<Settlement> findById(UUID id);
    List<ReconciliationLog> findLogsBySettlementId(UUID settlementId);
    boolean existsByPeriod(String period);
    List<Settlement> findAll();
    List<CompanyShare> findSharesBySettlementId(UUID settlementId);
}