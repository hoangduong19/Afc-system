package com.metro.afc.settlement.application.dto.settlement.v2;

import java.math.BigDecimal;

public record FareParams(
        BigDecimal openingPrice,
        BigDecimal pricePerKm
) {
    public static final FareParams METRO_DEFAULT =
            new FareParams(new BigDecimal("8000"), new BigDecimal("850"));
    public static final FareParams BUS_DEFAULT =
            new FareParams(new BigDecimal("3000"), new BigDecimal("450"));
}