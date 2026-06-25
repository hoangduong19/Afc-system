package com.metro.afc.settlement.domain.settlementAllocation;

import com.metro.afc.shared.domain.valueobject.Money;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record AllocationResult(
        Map<UUID, Money> directShares,
        Map<UUID, Money> proportionalShares
) {
    public Map<UUID, Money> totalShares() {
        Map<UUID, Money> total = new HashMap<>(directShares);
        proportionalShares.forEach((k, v) ->
                total.merge(k, v, Money::add));
        return total;
    }
}