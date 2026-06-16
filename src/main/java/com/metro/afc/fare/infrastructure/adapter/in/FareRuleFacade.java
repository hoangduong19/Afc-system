package com.metro.afc.fare.infrastructure.adapter.in;

import com.metro.afc.fare.application.dto.fareRule.*;
import com.metro.afc.fare.application.port.in.FareRuleUseCase;
import com.metro.afc.fare.domain.model.FarePassPrice;
import com.metro.afc.fare.domain.model.FareRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FareRuleFacade {

    private final FareRuleUseCase fareRuleUseCase;

    public FareRuleResponse create(CreateFareRuleRequest request, UUID createdBy) {
        FareRule fareRule = fareRuleUseCase.create(
                request.code(),
                request.mode(),
                request.baseFare(),
                request.ratePerKm(),
                request.minPrice(),
                request.maxPrice(),
                toPassPrices(request.passPrices()),
                request.effectiveFrom(),
                request.effectiveTo(),
                createdBy
        );
        return FareRuleResponse.from(fareRule);
    }

    public FareRuleResponse update(UUID id, UpdateFareRuleRequest request, UUID updatedBy) {
        FareRule fareRule = fareRuleUseCase.update(
                id,
                request.baseFare(),
                request.ratePerKm(),
                request.minPrice(),
                request.maxPrice(),
                toPassPrices(request.passPrices()),
                request.effectiveFrom(),
                request.effectiveTo(),
                request.reason(),
                updatedBy
        );
        return FareRuleResponse.from(fareRule);
    }

    public void disable(UUID id, DisableFareRuleRequest request, UUID disabledBy) {
        fareRuleUseCase.disable(id, request.reason(), disabledBy);
    }

    public FareRuleResponse findById(UUID id) {
        return FareRuleResponse.from(fareRuleUseCase.findById(id));
    }

    public List<FareRuleResponse> findAllActive() {
        return fareRuleUseCase.findAllActive()
                .stream()
                .map(FareRuleResponse::from)
                .toList();
    }

    public List<FareRuleResponse> findAll() {
        return fareRuleUseCase.findAll()
                .stream()
                .map(FareRuleResponse::from)
                .toList();
    }

    private List<FarePassPrice> toPassPrices(List<PassPriceEntry> entries) {
        return entries.stream()
                .map(e -> FarePassPrice.of(e.durationType(), e.durationMonths(),
                        e.scope(), e.amount()))
                .toList();
    }
}