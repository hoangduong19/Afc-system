package com.metro.afc.settlement.infrastructure.adapter.out.companyShare;

import com.metro.afc.settlement.domain.CompanyShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyShareJpaRepository
        extends JpaRepository<CompanyShare, UUID> {
    List<CompanyShare> findBySettlementId(UUID settlementId);
}