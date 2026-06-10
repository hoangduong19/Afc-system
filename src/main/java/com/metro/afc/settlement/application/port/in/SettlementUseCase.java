package com.metro.afc.settlement.application.port.in;

import com.metro.afc.settlement.domain.Settlement;

import java.util.List;
import java.util.UUID;

public interface SettlementUseCase {
    Settlement run(int month, int year, UUID ranBy);
    Settlement confirm(UUID settlementId, UUID confirmedBy);
    Settlement findById(UUID id);
    List<Settlement> findAll();
}