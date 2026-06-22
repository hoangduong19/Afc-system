package com.metro.afc.passenger.dto;

import com.metro.afc.fare.domain.model.enums.fareRule.FareMode;

import java.math.BigDecimal;
import java.util.List;

public record FarePriceResponse(
        FareMode mode,
        SingleTripPrice singleTrip,
        List<PassPriceItem> passPrices
) {
    public record SingleTripPrice(
            BigDecimal baseFare,
            BigDecimal ratePerKm,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {}

    public record PassPriceItem(
            String durationType,
            Integer durationMonths,
            String scope,           // null nếu METRO/ANY
            BigDecimal price
    ) {}
}