package com.metro.afc.passenger;

import com.metro.afc.fare.application.port.out.FareDiscountRepository;
import com.metro.afc.fare.application.port.out.FareRuleRepository;
import com.metro.afc.fare.domain.model.FareRule;
import com.metro.afc.passenger.dto.DiscountResponse;
import com.metro.afc.passenger.dto.FarePriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FareCatalogService {

    private final FareRuleRepository fareRuleRepository;
    private final FareDiscountRepository fareDiscountRepository;

    public List<FarePriceResponse> getFarePrices() {
        return fareRuleRepository.findAllActive().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<DiscountResponse> getActiveDiscounts() {
        return fareDiscountRepository.findAllActive().stream()
                .map(d -> new DiscountResponse(
                        d.getPassengerType(),
                        d.getDiscountValue().getDiscountType().name(),
                        d.getDiscountValue().getValue(),
                        d.getEffectiveFrom(),
                        d.getEffectiveTo()
                ))
                .toList();
    }

    private FarePriceResponse toResponse(FareRule rule) {
        FarePriceResponse.SingleTripPrice singleTrip =
                new FarePriceResponse.SingleTripPrice(
                        rule.getBaseFare().getAmount(),
                        rule.getRatePerKm().getAmount(),
                        rule.getMinPrice().getAmount(),
                        rule.getMaxPrice().getAmount()
                );

        List<FarePriceResponse.PassPriceItem> passPrices = rule.getPassPrices().stream()
                .map(p -> new FarePriceResponse.PassPriceItem(
                        p.getDurationType().name(),
                        p.getDurationMonths(),
                        p.getScope() != null ? p.getScope().name() : null,
                        p.getPrice().getAmount()
                ))
                .toList();

        return new FarePriceResponse(rule.getMode(), singleTrip, passPrices);
    }
}