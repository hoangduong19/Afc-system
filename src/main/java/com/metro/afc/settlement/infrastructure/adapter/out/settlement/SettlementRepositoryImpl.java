package com.metro.afc.settlement.infrastructure.adapter.out.settlement;

import com.metro.afc.settlement.application.port.out.SettlementRepository;
import com.metro.afc.settlement.domain.CompanyShare;
import com.metro.afc.settlement.domain.ReconciliationLog;
import com.metro.afc.settlement.domain.Settlement;
import com.metro.afc.settlement.infrastructure.adapter.out.companyShare.CompanyShareJpaRepository;
import com.metro.afc.settlement.infrastructure.adapter.out.reconcilationLog.ReconciliationLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SettlementRepositoryImpl implements SettlementRepository {

    private final SettlementJpaRepository        jpa;
    private final CompanyShareJpaRepository shareJpa;
    private final ReconciliationLogJpaRepository logJpa;

    @Override
    public Settlement save(Settlement s) { return jpa.save(s); }

    @Override
    public CompanyShare saveShare(CompanyShare share) {
        return shareJpa.save(share);
    }

    @Override
    public ReconciliationLog saveLog(ReconciliationLog log) {
        return logJpa.save(log);
    }

    @Override
    public Optional<Settlement> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public boolean existsByPeriod(String period) {
        return jpa.existsByPeriod(period);
    }

    @Override
    public List<Settlement> findAll() { return jpa.findAll(); }

    @Override
    public List<CompanyShare> findSharesBySettlementId(UUID id) {
        return shareJpa.findBySettlementId(id);
    }

    @Override
    public List<ReconciliationLog> findLogsBySettlementId(UUID id) {
        return logJpa.findBySettlementId(id);
    }
}