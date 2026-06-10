package com.metro.afc.settlement.infrastructure.adapter.out.settlement;

import com.metro.afc.settlement.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SettlementJpaRepository
        extends JpaRepository<Settlement, UUID> {
    boolean existsByPeriod(String period);
}