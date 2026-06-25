package com.metro.afc.settlement.domain.settlementAllocation;

import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;
import com.metro.afc.settlement.application.dto.settlement.v2.TicketRevenueData;
import com.metro.afc.settlement.application.dto.settlement.v2.TripContribution;
import com.metro.afc.shared.domain.valueobject.Money;
import com.metro.afc.shared.infrastructure.exception.BusinessRuleException;
import com.metro.afc.shared.infrastructure.exception.ErrorCode;
import com.metro.afc.ticket.domain.enums.PassScope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class Qd3316AllocationStrategy implements AllocationStrategy {

    private record FareParams(BigDecimal openingPrice, BigDecimal pricePerKm) {}

    @Override
    public String formulaCode() { return "QD3316_2025"; }

    @Override
    public AllocationResult allocate(
            Map<UUID, Money> singleTripShares,
            List<TicketRevenueData> monthlyTickets,
            List<FareRule> activeRules) {

        Map<FareMode, FareParams> fareParams = extractFareParams(activeRules);
        Map<UUID, Money> directMap       = new HashMap<>(singleTripShares);
        Map<UUID, Money> proportionalMap = new HashMap<>();

        for (TicketRevenueData ticket : monthlyTickets) {
            if (isSingleRoute(ticket)) allocateDirect(ticket, directMap);
            else                       allocateProportional(ticket, fareParams, proportionalMap);
        }

        return new AllocationResult(directMap, proportionalMap);
    }

    private Map<FareMode, FareParams> extractFareParams(List<FareRule> rules) {
        Map<FareMode, FareParams> params = new EnumMap<>(FareMode.class);
        rules.forEach(r -> params.put(
                r.getMode(),
                new FareParams(r.getBaseFare().getAmount(), r.getRatePerKm().getAmount())));
        params.putIfAbsent(FareMode.METRO, new FareParams(new BigDecimal("8000"), new BigDecimal("850")));
        params.putIfAbsent(FareMode.BUS,   new FareParams(new BigDecimal("3000"), new BigDecimal("450")));
        return params;
    }

    private boolean isSingleRoute(TicketRevenueData ticket) {
        return ticket.mode() == FareMode.METRO
                || ticket.scope() == PassScope.SINGLE_ROUTE;
    }

    private void allocateDirect(TicketRevenueData ticket, Map<UUID, Money> result) {
        if (ticket.contributions().isEmpty()) return;
        UUID operatorId = ticket.contributions().get(0).operatorId();
        result.merge(operatorId, ticket.ticketPrice(), Money::add);
    }

    private void allocateProportional(TicketRevenueData ticket,
                                      Map<FareMode, FareParams> fareParams,
                                      Map<UUID, Money> result) {
        Map<UUID, BigDecimal> weights = new LinkedHashMap<>();
        for (TripContribution tc : ticket.contributions()) {
            FareParams p = fareParams.get(tc.mode());
            if (p == null) throw new BusinessRuleException(ErrorCode.FARE_RULE_MISCONFIGURED);
            BigDecimal weight = p.openingPrice()
                    .multiply(BigDecimal.valueOf(tc.tripCount()))
                    .add(p.pricePerKm().multiply(tc.totalKm()));
            weights.merge(tc.operatorId(), weight, BigDecimal::add);
        }

        BigDecimal totalWeight = weights.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) return;

        weights.forEach((opId, w) -> {
            BigDecimal ratio = w.divide(totalWeight, 10, RoundingMode.HALF_UP);
            result.merge(opId, ticket.ticketPrice().multiply(ratio), Money::add);
        });
    }
}