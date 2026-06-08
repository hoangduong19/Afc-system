package com.metro.afc.fare.infrastructure.adapter.in;

import com.metro.afc.fare.application.dto.fareRuleDiscount.CreateFareDiscountRequest;
import com.metro.afc.fare.application.dto.fareRuleDiscount.FareDiscountResponse;
import com.metro.afc.fare.application.dto.fareRuleDiscount.UpdateFareDiscountRequest;
import com.metro.afc.fare.application.port.in.FareDiscountUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FareDiscountFacade {

    private final FareDiscountUseCase fareDiscountUseCase;

    public FareDiscountResponse create(CreateFareDiscountRequest req, UUID createdBy) {
        return FareDiscountResponse.from(fareDiscountUseCase.create(
                req.passengerType(), req.discountType(), req.discountValue(),
                req.effectiveFrom(), req.effectiveTo(), createdBy
        ));
    }

    public FareDiscountResponse update(UUID id, UpdateFareDiscountRequest req,
                                       UUID updatedBy) {
        return FareDiscountResponse.from(fareDiscountUseCase.update(
                id, req.discountType(), req.discountValue(),
                req.effectiveFrom(), req.effectiveTo(), updatedBy
        ));
    }

    public void disable(UUID id, UUID disabledBy) {
        fareDiscountUseCase.disable(id, disabledBy);
    }

    public FareDiscountResponse findById(UUID id) {
        return FareDiscountResponse.from(fareDiscountUseCase.findById(id));
    }

    public List<FareDiscountResponse> findAll() {
        return fareDiscountUseCase.findAll().stream()
                .map(FareDiscountResponse::from).toList();
    }

    public List<FareDiscountResponse> findAllActive() {
        return fareDiscountUseCase.findAllActive().stream()
                .map(FareDiscountResponse::from).toList();
    }
}