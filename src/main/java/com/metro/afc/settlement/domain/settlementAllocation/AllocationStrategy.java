package com.metro.afc.settlement.domain.settlementAllocation;

import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.settlement.application.dto.settlement.v2.TicketRevenueData;
import com.metro.afc.shared.domain.valueobject.Money;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AllocationStrategy {
    String formulaCode();
    AllocationResult allocate(Map<UUID, Money> singleTripShares,
                              List<TicketRevenueData> monthlyData,
                              List<FareRule> activeRules);
}